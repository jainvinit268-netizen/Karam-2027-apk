package com.example.data.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiKeySource {
    USER_KEY,
    ENV_SECRET,
    NONE
}

sealed class AiConnectionStatus {
    object NotTested : AiConnectionStatus()
    object Testing : AiConnectionStatus()
    data class Success(val latencyMs: Long, val model: String, val message: String) : AiConnectionStatus()
    data class Error(val message: String, val httpCode: Int? = null, val details: String? = null) : AiConnectionStatus()
}

data class AiConfigState(
    val source: AiKeySource = AiKeySource.NONE,
    val isConfigured: Boolean = false,
    val maskedKey: String = "",
    val hasUserCustomKey: Boolean = false,
    val hasEnvSecret: Boolean = false,
    val connectionStatus: AiConnectionStatus = AiConnectionStatus.NotTested
)

class AiKeyManager private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _configState = MutableStateFlow(computeConfigState(AiConnectionStatus.NotTested))
    val configState: StateFlow<AiConfigState> = _configState.asStateFlow()

    fun getUserCustomKey(): String? {
        val key = prefs.getString(KEY_USER_GEMINI_KEY, null)?.trim()
        return if (key.isNullOrBlank()) null else key
    }

    fun getEnvSecretKey(): String? {
        val key = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
        return if (key.isBlank() || key == "MY_GEMINI_API_KEY" || key == "null") null else key
    }

    fun getActiveApiKey(): String? = getUserCustomKey() ?: getEnvSecretKey()

    fun getActiveKeySource(): AiKeySource {
        return when {
            getUserCustomKey() != null -> AiKeySource.USER_KEY
            getEnvSecretKey() != null -> AiKeySource.ENV_SECRET
            else -> AiKeySource.NONE
        }
    }

    fun saveUserApiKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            clearUserApiKey()
            return
        }
        prefs.edit().putString(KEY_USER_GEMINI_KEY, trimmed).apply()
        _configState.value = computeConfigState(AiConnectionStatus.NotTested)
        Log.i(TAG, "Custom Gemini API key saved by user")
    }

    fun clearUserApiKey() {
        prefs.edit().remove(KEY_USER_GEMINI_KEY).apply()
        _configState.value = computeConfigState(AiConnectionStatus.NotTested)
        Log.i(TAG, "Custom Gemini API key removed by user")
    }

    private fun computeConfigState(status: AiConnectionStatus): AiConfigState {
        val userKey = getUserCustomKey()
        val envKey = getEnvSecretKey()
        val activeKey = userKey ?: envKey
        val source = when {
            userKey != null -> AiKeySource.USER_KEY
            envKey != null -> AiKeySource.ENV_SECRET
            else -> AiKeySource.NONE
        }
        val masked = when {
            userKey != null -> maskKey(userKey)
            envKey != null -> "Server-side Secret (${maskKey(envKey)})"
            else -> "Not Configured"
        }
        return AiConfigState(
            source = source,
            isConfigured = activeKey != null,
            maskedKey = masked,
            hasUserCustomKey = userKey != null,
            hasEnvSecret = envKey != null,
            connectionStatus = status
        )
    }

    private fun maskKey(key: String): String {
        if (key.length <= 8) return "••••••••"
        return "${key.take(4)}••••${key.takeLast(4)}"
    }

    suspend fun testConnection(overrideKey: String? = null): AiConnectionStatus = withContext(Dispatchers.IO) {
        val keyToTest = overrideKey?.trim()?.takeIf { it.isNotEmpty() } ?: getActiveApiKey()
        if (keyToTest.isNullOrBlank()) {
            val status = AiConnectionStatus.Error(
                message = "No Gemini API key available to test. Please provide an API key.",
                httpCode = null
            )
            _configState.value = computeConfigState(status)
            return@withContext status
        }

        _configState.value = computeConfigState(AiConnectionStatus.Testing)
        val startTime = System.currentTimeMillis()
        try {
            val testUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$keyToTest"
            val testPayload = JSONObject().apply {
                val contents = JSONArray().apply {
                    val content = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", "Respond with the single word: OK") })
                        }
                        put("parts", parts)
                    }
                    put(content)
                }
                put("contents", contents)
                val config = JSONObject().apply {
                    put("maxOutputTokens", 5)
                    put("responseMimeType", "text/plain")
                }
                put("generationConfig", config)
            }

            val request = Request.Builder()
                .url(testUrl)
                .post(testPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()
            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""

            val status = if (response.isSuccessful) {
                AiConnectionStatus.Success(
                    latencyMs = latency,
                    model = "gemini-3.6-flash",
                    message = "Connected successfully (${latency}ms latency)"
                )
            } else {
                val code = response.code
                val friendlyMessage = when (code) {
                    400, 403 -> "Invalid or unauthorized Gemini API key. Please check your key permissions in Google AI Studio."
                    404 -> "Gemini 3.6 Flash endpoint is unavailable for this key/project."
                    429 -> "Gemini API quota exceeded (HTTP 429)."
                    500, 503 -> "Google Gemini service is temporarily unavailable. Try again in a moment."
                    else -> "Connection failed with HTTP $code: ${response.message}"
                }
                AiConnectionStatus.Error(
                    message = friendlyMessage,
                    httpCode = code,
                    details = responseBody.take(300)
                )
            }
            _configState.value = computeConfigState(status)
            return@withContext status
        } catch (e: Exception) {
            Log.e(TAG, "Error testing Gemini connection", e)
            val status = AiConnectionStatus.Error(
                message = "Network error connecting to Gemini API: ${e.localizedMessage ?: "Unknown error"}. Check internet connection.",
                httpCode = null,
                details = e.message
            )
            _configState.value = computeConfigState(status)
            return@withContext status
        }
    }

    fun resetTestStatus() {
        _configState.value = computeConfigState(AiConnectionStatus.NotTested)
    }

    companion object {
        private const val TAG = "AiKeyManager"
        private const val PREFS_NAME = "jee_ai_config_prefs"
        private const val KEY_USER_GEMINI_KEY = "user_gemini_api_key"

        @Volatile
        private var INSTANCE: AiKeyManager? = null

        fun getInstance(context: Context): AiKeyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiKeyManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
