package com.example.ai

import android.content.Context
import android.util.Log
import com.example.data.ai.AiKeyManager
import com.example.data.ai.AiKeySource
import com.example.data.model.Difficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiJeeExtractor {
    private const val TAG = "GeminiJeeExtractor"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    data class ExtractionResult(
        val success: Boolean,
        val testTitle: String,
        val questions: List<QuestionItem>,
        val flaggedQuestions: List<Int> = emptyList(),
        val aiUsed: Boolean = false,
        val keySource: AiKeySource = AiKeySource.NONE,
        val statusMessage: String = "",
        val errorMessage: String? = null
    )

    suspend fun extractJeePaper(
        context: Context,
        questionPaperContent: String,
        answerKeyContent: String,
        testTitle: String = "JEE Main Paper 1"
    ): ExtractionResult = withContext(Dispatchers.IO) {
        val aiKeyManager = AiKeyManager.getInstance(context)
        val apiKey = aiKeyManager.getActiveApiKey()
        val keySource = aiKeyManager.getActiveKeySource()

        if (apiKey.isNullOrBlank()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local algorithmic extraction.")
            val fallbackResult = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
            return@withContext fallbackResult.copy(
                aiUsed = false,
                keySource = AiKeySource.NONE,
                statusMessage = "Gemini AI is not configured. Processed using local algorithmic parsing.",
                errorMessage = "Gemini AI is not configured. Tap 'Configure Gemini AI' to enable AI-powered deep parsing."
            )
        }

        try {
            val prompt = """
                You are a senior JEE Main examination expert and AI parser.
                Parse the provided Question Paper text/PDF extract and Official Answer Key text for a JEE Main Paper 1 (Physics, Chemistry, Mathematics).
                
                RULES:
                1. Identify all questions, categorized into PHYSICS, CHEMISTRY, and MATHEMATICS.
                2. Identify whether each question is MCQ (with 4 options A, B, C, D) or NUMERICAL.
                3. Match the Correct Answer strictly from the provided Answer Key for each question number.
                4. For each question, classify:
                   - Subject ("PHYSICS", "CHEMISTRY", "MATHEMATICS")
                   - Section ("Section A" for MCQ, "Section B" for Numerical)
                   - Question Type ("MCQ" or "NUMERICAL")
                   - Options (list of 4 strings for MCQ, empty list for NUMERICAL)
                   - Correct Answer (e.g. "A", "B", "C", "D" or integer/decimal string like "42", "2.5")
                   - Chapter name (e.g. "Rotational Motion", "Thermodynamics", "Definite Integration")
                   - Core Concept tested
                   - Difficulty ("EASY", "MEDIUM", "HARD")
                   - Best step-by-step JEE solution
                   - Ideal time in seconds (typically 45-120 seconds)
                   - YouTube search query for verified video solution (e.g. "JEE Main Physics Rotational Motion solid sphere rolling solution")
                   - Is Uncertain / Flagged (boolean: true if answer key or question text was ambiguous)

                Return ONLY a valid JSON object matching this schema:
                {
                   "testTitle": "$testTitle",
                   "questions": [
                      {
                         "id": "PHY_01",
                         "questionNumber": 1,
                         "subject": "PHYSICS",
                         "section": "Section A",
                         "type": "MCQ",
                         "questionText": "...",
                         "options": ["(A) ...", "(B) ...", "(C) ...", "(D) ..."],
                         "correctAnswer": "B",
                         "chapter": "...",
                         "concept": "...",
                         "difficulty": "MEDIUM",
                         "solutionText": "...",
                         "idealTimeSeconds": 90,
                         "youtubeSearchQuery": "...",
                         "isUncertain": false
                      }
                   ]
                }

                QUESTION PAPER TEXT:
                $questionPaperContent

                OFFICIAL ANSWER KEY TEXT:
                $answerKeyContent
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.2)
                    put("topP", 0.95)
                    val responseFormat = JSONObject().apply {
                        put("mimeType", "application/json")
                    }
                    put("responseFormat", responseFormat)
                }
                put("generationConfig", genConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                val code = response.code
                val errNotice = when (code) {
                    400, 403 -> "Invalid or unauthorized Gemini API key (HTTP $code)."
                    429 -> "Gemini API rate limit / quota exceeded (HTTP 429)."
                    else -> "Gemini API error (HTTP $code)."
                }
                Log.e(TAG, "Gemini API error: $code $responseBody")
                val fallback = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
                return@withContext fallback.copy(
                    aiUsed = false,
                    keySource = keySource,
                    statusMessage = "$errNotice Processed via local fallback.",
                    errorMessage = errNotice
                )
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                val fallback = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
                return@withContext fallback.copy(
                    aiUsed = false,
                    keySource = keySource,
                    statusMessage = "Gemini returned empty response. Processed via local fallback.",
                    errorMessage = "Empty AI response"
                )
            }

            val result = parseQuestionsFromJson(text, testTitle)
            result.copy(
                aiUsed = true,
                keySource = keySource,
                statusMessage = "Successfully generated CBT Test using Gemini AI (${result.questions.size} questions extracted)."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini extraction", e)
            val fallback = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
            fallback.copy(
                aiUsed = false,
                keySource = keySource,
                statusMessage = "Network error connecting to Gemini. Processed via local fallback.",
                errorMessage = e.localizedMessage
            )
        }
    }

    private fun parseQuestionsFromJson(jsonStr: String, fallbackTitle: String): ExtractionResult {
        return try {
            val root = JSONObject(jsonStr)
            val parsedTitle = root.optString("testTitle", fallbackTitle)
            val questionsArray = root.optJSONArray("questions") ?: JSONArray()
            val questions = mutableListOf<QuestionItem>()
            val flagged = mutableListOf<Int>()

            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val qNum = qObj.optInt("questionNumber", i + 1)
                val subjStr = qObj.optString("subject", "PHYSICS").uppercase()
                val subject = when {
                    subjStr.contains("PHY") -> Subject.PHYSICS
                    subjStr.contains("CHE") -> Subject.CHEMISTRY
                    else -> Subject.MATHEMATICS
                }
                val typeStr = qObj.optString("type", "MCQ").uppercase()
                val type = if (typeStr.contains("NUM")) QuestionType.NUMERICAL else QuestionType.MCQ
                val section = qObj.optString("section", if (type == QuestionType.MCQ) "Section A" else "Section B")
                val text = qObj.optString("questionText", "Question $qNum")
                val ans = qObj.optString("correctAnswer", if (type == QuestionType.MCQ) "A" else "0")
                val chapter = qObj.optString("chapter", "General ${subject.displayName}")
                val concept = qObj.optString("concept", "Core Concept")
                val diffStr = qObj.optString("difficulty", "MEDIUM").uppercase()
                val diff = when {
                    diffStr.contains("EASY") -> Difficulty.EASY
                    diffStr.contains("HARD") -> Difficulty.HARD
                    else -> Difficulty.MEDIUM
                }
                val solution = qObj.optString("solutionText", "Step-by-step JEE solution for Question $qNum.")
                val idealTime = qObj.optInt("idealTimeSeconds", 90)
                val yt = qObj.optString("youtubeSearchQuery", "JEE Main ${subject.displayName} $chapter solution")
                val isUncertain = qObj.optBoolean("isUncertain", false)

                val options = mutableListOf<String>()
                val optArray = qObj.optJSONArray("options")
                if (optArray != null) {
                    for (j in 0 until optArray.length()) {
                        options.add(optArray.getString(j))
                    }
                }

                if (isUncertain) {
                    flagged.add(qNum)
                }

                questions.add(
                    QuestionItem(
                        id = "${subject.name.take(3)}_${qNum}",
                        questionNumber = qNum,
                        subject = subject,
                        section = section,
                        type = type,
                        questionText = text,
                        options = options,
                        correctAnswer = ans,
                        chapter = chapter,
                        concept = concept,
                        difficulty = diff,
                        solutionText = solution,
                        idealTimeSeconds = idealTime,
                        youtubeSearchQuery = yt
                    )
                )
            }

            ExtractionResult(
                success = questions.isNotEmpty(),
                testTitle = parsedTitle,
                questions = questions,
                flaggedQuestions = flagged
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON response from Gemini", e)
            ExtractionResult(
                success = false,
                testTitle = fallbackTitle,
                questions = emptyList(),
                errorMessage = e.message
            )
        }
    }

    private fun parseAlgorithmicFallback(
        questionPaperContent: String,
        answerKeyContent: String,
        testTitle: String
    ): ExtractionResult {
        val answerMap = mutableMapOf<Int, String>()
        val answerLines = answerKeyContent.lines()
        val ansRegex = Regex("""(?:Q|Question)?\s*(\d+)[\s*:\.\-]+([A-Da-d0-9\.\-]+)""")

        for (line in answerLines) {
            val match = ansRegex.find(line.trim())
            if (match != null) {
                val qNum = match.groupValues[1].toIntOrNull()
                val ans = match.groupValues[2].trim().uppercase()
                if (qNum != null && ans.isNotBlank()) {
                    answerMap[qNum] = ans
                }
            }
        }

        val questions = mutableListOf<QuestionItem>()
        val flagged = mutableListOf<Int>()

        var currentSubject = Subject.PHYSICS
        val qBlockRegex = Regex("""(?:Q|Question)?\s*(\d+)[\.\:](.*?)(?=(?:(?:Q|Question)?\s*\d+[\.\:])|$)""", RegexOption.DOT_MATCHES_ALL)

        val matches = qBlockRegex.findAll(questionPaperContent).toList()

        if (matches.isNotEmpty()) {
            for (m in matches) {
                val qNum = m.groupValues[1].toIntOrNull() ?: (questions.size + 1)
                val fullText = m.groupValues[2].trim()

                currentSubject = when {
                    qNum <= 25 -> Subject.PHYSICS
                    qNum <= 50 -> Subject.CHEMISTRY
                    else -> Subject.MATHEMATICS
                }

                val isNumerical = qNum in listOf(21, 22, 23, 24, 25, 46, 47, 48, 49, 50, 71, 72, 73, 74, 75) || !fullText.contains("(A)")
                val type = if (isNumerical) QuestionType.NUMERICAL else QuestionType.MCQ
                val section = if (isNumerical) "Section B (Numerical)" else "Section A (MCQ)"

                val options = mutableListOf<String>()
                var questionBody = fullText
                if (!isNumerical) {
                    val optRegex = Regex("""\(([A-D])\)\s*([^(\n]+)""")
                    val optMatches = optRegex.findAll(fullText).toList()
                    if (optMatches.size >= 2) {
                        questionBody = fullText.substring(0, optMatches[0].range.first).trim()
                        for (opt in optMatches) {
                            options.add("(${opt.groupValues[1]}) ${opt.groupValues[2].trim()}")
                        }
                    } else {
                        options.addAll(listOf("(A) Option A", "(B) Option B", "(C) Option C", "(D) Option D"))
                    }
                }

                val ans = answerMap[qNum] ?: if (isNumerical) "0" else "A"
                if (answerMap[qNum] == null) {
                    flagged.add(qNum)
                }

                val chapter = when (currentSubject) {
                    Subject.PHYSICS -> "Physics High-Yield Core"
                    Subject.CHEMISTRY -> "Chemistry High-Yield Core"
                    Subject.MATHEMATICS -> "Mathematics High-Yield Core"
                }

                questions.add(
                    QuestionItem(
                        id = "${currentSubject.name.take(3)}_$qNum",
                        questionNumber = qNum,
                        subject = currentSubject,
                        section = section,
                        type = type,
                        questionText = if (questionBody.isNotBlank()) questionBody else "Question $qNum",
                        options = options,
                        correctAnswer = ans,
                        chapter = chapter,
                        concept = "JEE Standard Concept",
                        difficulty = Difficulty.MEDIUM,
                        solutionText = "Official solution: Correct answer is $ans based on fundamental principles.",
                        idealTimeSeconds = if (isNumerical) 120 else 75,
                        youtubeSearchQuery = "JEE Main ${currentSubject.displayName} Question $qNum solution"
                    )
                )
            }
        }

        return ExtractionResult(
            success = questions.isNotEmpty(),
            testTitle = testTitle,
            questions = questions,
            flaggedQuestions = flagged
        )
    }
}
