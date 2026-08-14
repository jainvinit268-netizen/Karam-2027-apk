package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class UserProfile(
    val userId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val idToken: String? = null
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticating : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

class GoogleAuthManager private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val credentialManager = CredentialManager.create(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        loadPersistedSession()
    }

    private fun loadPersistedSession() {
        val userId = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val name = prefs.getString(KEY_NAME, null)
        val photo = prefs.getString(KEY_PHOTO, null)
        val idToken = prefs.getString(KEY_TOKEN, null)

        if (!userId.isNullOrBlank() && !email.isNullOrBlank()) {
            val user = UserProfile(
                userId = userId,
                email = email,
                displayName = name ?: email.substringBefore("@"),
                photoUrl = photo,
                idToken = idToken
            )
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
            Log.d(TAG, "Restored persisted session for: $email")
        } else {
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun signInWithGoogle(activityContext: Context, serverClientId: String = "844139846734-android.apps.googleusercontent.com"): Result<UserProfile> {
        _authState.value = AuthState.Authenticating
        try {
            // Build real Google ID Option for Android CredentialManager
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: email.substringBefore("@")
                val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                val userId = googleIdTokenCredential.id

                val user = UserProfile(
                    userId = userId,
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    idToken = idToken
                )

                persistSession(user)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                Log.i(TAG, "Google Sign-In successful for: $email")
                return Result.success(user)
            } else {
                val errMsg = "Unsupported credential returned: ${credential.type}"
                _authState.value = AuthState.Error(errMsg)
                return Result.failure(IllegalStateException(errMsg))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In was cancelled by user")
            _authState.value = if (_currentUser.value != null) AuthState.Authenticated(_currentUser.value!!) else AuthState.Unauthenticated
            return Result.failure(e)
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts available on device", e)
            val msg = "No Google account found on device. Please add a Google account in system settings."
            _authState.value = AuthState.Error(msg)
            return Result.failure(Exception(msg, e))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error during Google Sign-In", e)
            val msg = "Google Sign-In failed: ${e.message ?: "Authentication error"}"
            _authState.value = AuthState.Error(msg)
            return Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            val msg = e.localizedMessage ?: "Sign-in failed. Please check network and try again."
            _authState.value = AuthState.Error(msg)
            return Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            clearPersistedSession()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            Log.i(TAG, "User signed out successfully")
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credentials during sign-out", e)
            clearPersistedSession()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            return Result.success(Unit)
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = if (_currentUser.value != null) AuthState.Authenticated(_currentUser.value!!) else AuthState.Unauthenticated
        }
    }

    private fun persistSession(user: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_ID, user.userId)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_NAME, user.displayName)
            .putString(KEY_PHOTO, user.photoUrl)
            .putString(KEY_TOKEN, user.idToken)
            .apply()
    }

    private fun clearPersistedSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
        private const val PREFS_NAME = "jee_cbt_auth_prefs"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_EMAIL = "auth_email"
        private const val KEY_NAME = "auth_display_name"
        private const val KEY_PHOTO = "auth_photo_url"
        private const val KEY_TOKEN = "auth_id_token"

        @Volatile
        private var INSTANCE: GoogleAuthManager? = null

        fun getInstance(context: Context): GoogleAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoogleAuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
