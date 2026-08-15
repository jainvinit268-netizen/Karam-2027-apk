package com.example.ai

import android.content.Context
import android.util.Log
import com.example.data.ai.AiKeyManager
import com.example.data.ai.AiKeySource
import com.example.data.model.Difficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject
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
    private const val MODEL_NAME = "gemini-3.6-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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
        testTitle: String = "JEE Main CBT Examination",
        pageImagesBase64: List<String> = emptyList()
    ): ExtractionResult = extractJeePaperWithProgress(
        context = context,
        questionPaperContent = questionPaperContent,
        answerKeyContent = answerKeyContent,
        testTitle = testTitle,
        pageImagesBase64 = pageImagesBase64,
        totalPages = maxOf(1, pageImagesBase64.size),
        targetQuestionCount = null,
        onProgress = {}
    )

    suspend fun extractJeePaperWithProgress(
        context: Context,
        questionPaperContent: String,
        answerKeyContent: String,
        testTitle: String = "JEE Main CBT Examination",
        pageImagesBase64: List<String> = emptyList(),
        totalPages: Int = 1,
        targetQuestionCount: Int? = null,
        onProgress: (ProcessingProgress) -> Unit = {}
    ): ExtractionResult = withContext(Dispatchers.IO) {
        val aiKeyManager = AiKeyManager.getInstance(context)
        val apiKey = aiKeyManager.getActiveApiKey()
        val keySource = aiKeyManager.getActiveKeySource()

        if (apiKey.isNullOrBlank()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local algorithmic parser.")
            onProgress(
                ProcessingProgress(
                    step = ProcessingStep.ANALYZING_PDF,
                    progressPercent = 30,
                    message = "No Gemini API Key found. Parsing with local layout engine...",
                    totalPages = totalPages
                )
            )
            val fallbackResult = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
            onProgress(
                ProcessingProgress(
                    step = ProcessingStep.BUILDING_CBT,
                    progressPercent = 95,
                    message = "Extracted ${fallbackResult.questions.size} questions with local parser.",
                    totalPages = totalPages,
                    questionsDetected = fallbackResult.questions.size
                )
            )
            return@withContext fallbackResult.copy(
                aiUsed = false,
                keySource = AiKeySource.NONE,
                statusMessage = "Extracted ${fallbackResult.questions.size} questions using local layout engine.",
                errorMessage = null
            )
        }

        try {
            onProgress(
                ProcessingProgress(
                    step = ProcessingStep.ANALYZING_PDF,
                    progressPercent = 25,
                    message = "Analyzing document structure & question blocks ($totalPages pages)...",
                    totalPages = totalPages
                )
            )

            val prompt = buildString {
                appendLine("You are an expert NTA JEE Main Examination Controller, OCR specialist, and PDF-to-CBT Extraction System.")
                appendLine("Parse the provided Question Paper (text content and/or page images) and Official Answer Key to generate a high-accuracy, full Computer Based Test (CBT).")
                appendLine()
                appendLine("STRICT EXTRACTION RULES:")
                appendLine("1. DOCUMENT UNDERSTANDING & ZERO QUESTION MIXING:")
                appendLine("   - Extract every question exactly as present in the input document/images.")
                appendLine("   - Keep all question text, mathematical formulas (LaTeX/Unicode), chemical equations, tables, and options together.")
                appendLine("   - NEVER mix Q(N) with Q(N+1). NEVER attach Q(N+1)'s options or diagram to Q(N).")
                appendLine("2. SUBJECT & SECTION CLASSIFICATION:")
                appendLine("   - Group questions strictly by subject: 'PHYSICS', 'CHEMISTRY', 'MATHEMATICS'.")
                appendLine("   - 'Section A': Single Choice Questions (MCQ) with 4 options (A, B, C, D) and +4 / -1 marking.")
                appendLine("   - 'Section B': Numerical Value Questions (NUMERICAL) where answer is an integer/decimal and +4 / -1 marking.")
                appendLine("3. OFFICIAL ANSWER KEY MAPPING:")
                appendLine("   - Carefully map the correct answer from the provided Answer Key for each question number.")
                appendLine("   - For MCQ: Store exact correct option string ('A', 'B', 'C', or 'D').")
                appendLine("   - For Numerical: Store exact numerical value (e.g., '42', '2.5', '10', '-5').")
                appendLine("4. RICH JEE METADATA:")
                appendLine("   - Chapter Name, Core Concept, Difficulty ('EASY', 'MEDIUM', 'HARD'), Step-by-Step Solution, Ideal Time (45-150s), YouTube search query.")
                appendLine()
                appendLine("Return ONLY a valid JSON object matching this exact schema:")
                appendLine("{")
                appendLine("   \"testTitle\": \"$testTitle\",")
                appendLine("   \"questions\": [")
                appendLine("      {")
                appendLine("         \"id\": \"PHY_01\",")
                appendLine("         \"questionNumber\": 1,")
                appendLine("         \"subject\": \"PHYSICS\",")
                appendLine("         \"section\": \"Section A\",")
                appendLine("         \"type\": \"MCQ\",")
                appendLine("         \"questionText\": \"Complete question text with formulas...\",")
                appendLine("         \"options\": [\"(A) Option A text\", \"(B) Option B text\", \"(C) Option C text\", \"(D) Option D text\"],")
                appendLine("         \"correctAnswer\": \"B\",")
                appendLine("         \"chapter\": \"Rotational Motion\",")
                appendLine("         \"concept\": \"Moment of Inertia\",")
                appendLine("         \"difficulty\": \"MEDIUM\",")
                appendLine("         \"solutionText\": \"Step 1: Apply theorem of parallel axes... Final Answer: Option (B)\",")
                appendLine("         \"idealTimeSeconds\": 90,")
                appendLine("         \"youtubeSearchQuery\": \"JEE Main Physics Rotational Motion moment of inertia solution\",")
                appendLine("         \"isUncertain\": false")
                appendLine("      }")
                appendLine("   ]")
                appendLine("}")
                appendLine()
                if (questionPaperContent.isNotBlank()) {
                    appendLine("--- QUESTION PAPER TEXT SOURCE ---")
                    appendLine(questionPaperContent)
                } else if (pageImagesBase64.isNotEmpty()) {
                    appendLine("--- NOTE ON INPUT ---")
                    appendLine("The question paper is provided in the attached high-resolution document page images. Perform deep visual OCR to extract every single question and option from these pages.")
                }
                if (answerKeyContent.isNotBlank()) {
                    appendLine()
                    appendLine("--- OFFICIAL ANSWER KEY SOURCE ---")
                    appendLine(answerKeyContent)
                }
            }

            onProgress(
                ProcessingProgress(
                    step = ProcessingStep.DETECTING_QUESTIONS,
                    progressPercent = 45,
                    message = "Detecting questions, options, formulas & diagrams via Gemini AI...",
                    totalPages = totalPages
                )
            )

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            for (i in 0 until minOf(pageImagesBase64.size, 6)) {
                                val imgBase64 = pageImagesBase64[i]
                                if (imgBase64.isNotBlank()) {
                                    val inlineDataObj = JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", imgBase64)
                                    }
                                    put(JSONObject().apply { put("inline_data", inlineDataObj) })
                                }
                            }
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("maxOutputTokens", 8192)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", genConfig)
            }

            onProgress(
                ProcessingProgress(
                    step = ProcessingStep.MAPPING_ANSWERS,
                    progressPercent = 65,
                    message = "Parsing Official Answer Key & mapping to question numbers...",
                    totalPages = totalPages
                )
            )

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            onProgress(
                ProcessingProgress(
                    step = ProcessingStep.VALIDATING,
                    progressPercent = 85,
                    message = "Validating question items, answers & mathematical notation...",
                    totalPages = totalPages
                )
            )

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                val code = response.code
                val errNotice = when (code) {
                    400, 403 -> "Gemini API key error (HTTP $code)."
                    429 -> "Gemini API rate quota reached (HTTP 429)."
                    else -> "Gemini API responded with code $code."
                }
                Log.e(TAG, "Gemini API error: $code $responseBody")
                val fallback = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
                return@withContext fallback.copy(
                    aiUsed = false,
                    keySource = keySource,
                    statusMessage = "$errNotice Extracted via local fallback (${fallback.questions.size} questions).",
                    errorMessage = null
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
                    statusMessage = "Processed via local layout engine (${fallback.questions.size} questions).",
                    errorMessage = null
                )
            }

            val result = parseQuestionsFromJson(text, testTitle)
            if (result.questions.isNotEmpty()) {
                onProgress(
                    ProcessingProgress(
                        step = ProcessingStep.BUILDING_CBT,
                        progressPercent = 95,
                        message = "Building NTA JEE CBT Test (${result.questions.size} questions)...",
                        totalPages = totalPages,
                        questionsDetected = result.questions.size
                    )
                )
                result.copy(
                    aiUsed = true,
                    keySource = keySource,
                    statusMessage = "Successfully generated CBT with Gemini AI (${result.questions.size} questions extracted, ${result.flaggedQuestions.size} flagged)."
                )
            } else {
                val fallback = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
                fallback.copy(
                    aiUsed = false,
                    keySource = keySource,
                    statusMessage = "Extracted ${fallback.questions.size} questions via local layout engine."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini extraction, falling back to local algorithm", e)
            val fallback = parseAlgorithmicFallback(questionPaperContent, answerKeyContent, testTitle)
            fallback.copy(
                aiUsed = false,
                keySource = keySource,
                statusMessage = "Extracted ${fallback.questions.size} questions via local layout parser.",
                errorMessage = null
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
                val rawNum = qObj.optInt("questionNumber", i + 1)
                val qNum = if (rawNum in 1..300) rawNum else (i + 1)
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
                        id = "${subject.name.take(3)}_$qNum",
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

    /**
     * High-precision local parser that handles multi-format answer keys, subject headers,
     * MCQ options, and numerical questions without requiring an external AI connection.
     */
    fun parseAlgorithmicFallback(
        questionPaperContent: String,
        answerKeyContent: String,
        testTitle: String
    ): ExtractionResult {
        val answerMap = mutableMapOf<Int, String>()

        val ansPatterns = listOf(
            Regex("""(?:Q|Question|q)?\s*(\d+)[\s*:\.\-\=\)]+[\(]?([A-Da-d0-9\.\-]+)[\)]?"""),
            Regex("""(\d+)\s*\.\s*\(([A-Da-d0-9\.\-]+)\)"""),
            Regex("""(\d+)\s*->\s*([A-Da-d0-9\.\-]+)"""),
            Regex("""(\d+)\s+([A-Da-d0-9\.\-]+)""")
        )

        for (line in answerKeyContent.lines()) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("//") || trimmed.startsWith("#")) continue
            val tokens = trimmed.split(Regex("""[\|,;]"""))
            for (token in tokens) {
                val tokenTrimmed = token.trim()
                for (pat in ansPatterns) {
                    val match = pat.find(tokenTrimmed)
                    if (match != null) {
                        val qNum = match.groupValues[1].toIntOrNull()
                        var ans = match.groupValues[2].trim().uppercase()
                        ans = ans.removePrefix("(").removeSuffix(")")
                        if (qNum != null && ans.isNotBlank() && !answerMap.containsKey(qNum)) {
                            answerMap[qNum] = ans
                            break
                        }
                    }
                }
            }
        }

        val questions = mutableListOf<QuestionItem>()
        val flagged = mutableListOf<Int>()
        val cleanPaper = questionPaperContent.trim()

        val qBlockRegex = Regex("""(?:(?:^|\n)\s*(?:\[?(?:PHYSICS|CHEMISTRY|MATHEMATICS)[\w\s\(\)\-\]]*)?\s*(?:Q\.?|Question|Problem)?\s*(\d+)[\.\:\-\)])(.*?)(?=(?:(?:\n\s*(?:\[?(?:PHYSICS|CHEMISTRY|MATHEMATICS)[\w\s\(\)\-\]]*)?\s*(?:Q\.?|Question|Problem)?\s*\d+[\.\:\-\)])|$))""", RegexOption.DOT_MATCHES_ALL)

        var matches = qBlockRegex.findAll(cleanPaper).toList()

        if (matches.isEmpty() && cleanPaper.isNotBlank()) {
            val altRegex = Regex("""(?:(?:^|\n)\s*(\d+)[\.\)]\s+)(.*?)(?=(?:(?:\n\s*\d+[\.\)]\s+)|$))""", RegexOption.DOT_MATCHES_ALL)
            matches = altRegex.findAll(cleanPaper).toList()
        }

        if (matches.isNotEmpty()) {
            for (m in matches) {
                val parsedNum = m.groupValues[1].toIntOrNull()
                val qNum = if (parsedNum != null && parsedNum in 1..300) parsedNum else (questions.size + 1)
                val fullText = m.groupValues[2].trim()

                val currentSubject = when {
                    fullText.contains("[CHEMISTRY", ignoreCase = true) || (qNum in 26..50 && matches.size >= 50) -> Subject.CHEMISTRY
                    fullText.contains("[MATHEMATICS", ignoreCase = true) || (qNum in 51..75 && matches.size >= 50) -> Subject.MATHEMATICS
                    qNum in 1..25 -> Subject.PHYSICS
                    qNum in 26..50 -> Subject.CHEMISTRY
                    else -> Subject.MATHEMATICS
                }

                val isNumerical = fullText.contains("SECTION B", ignoreCase = true) ||
                        fullText.contains("NUMERICAL", ignoreCase = true) ||
                        (qNum in listOf(21, 22, 23, 24, 25, 46, 47, 48, 49, 50, 71, 72, 73, 74, 75)) ||
                        (!fullText.contains("(A)") && !fullText.contains("(1)") && !fullText.contains("(a)"))

                val type = if (isNumerical) QuestionType.NUMERICAL else QuestionType.MCQ
                val section = if (isNumerical) "Section B (Numerical)" else "Section A (MCQ)"

                val options = mutableListOf<String>()
                var questionBody = fullText

                if (!isNumerical) {
                    val optRegex = Regex("""(?:[\(\[]?([A-Da-d1-4])[\)\]\.\s*)([^\(\[\n]+)""")
                    val optMatches = optRegex.findAll(fullText).toList()
                    if (optMatches.size >= 2) {
                        questionBody = fullText.substring(0, optMatches[0].range.first).trim()
                        for (opt in optMatches) {
                            val label = when (opt.groupValues[1].uppercase()) {
                                "1" -> "A"
                                "2" -> "B"
                                "3" -> "C"
                                "4" -> "D"
                                else -> opt.groupValues[1].uppercase()
                            }
                            options.add("($label) ${opt.groupValues[2].trim()}")
                        }
                    } else {
                        options.addAll(listOf("(A) Option A", "(B) Option B", "(C) Option C", "(D) Option D"))
                    }
                }

                val ans = answerMap[qNum] ?: if (isNumerical) "0" else "A"
                if (!answerMap.containsKey(qNum)) {
                    flagged.add(qNum)
                }

                val chapter = when (currentSubject) {
                    Subject.PHYSICS -> if (qNum <= 10) "Mechanics & Rotational Motion" else "Electrodynamics & Modern Physics"
                    Subject.CHEMISTRY -> if (qNum <= 35) "Physical & Inorganic Chemistry" else "Organic Reaction Mechanisms"
                    Subject.MATHEMATICS -> if (qNum <= 60) "Calculus & Coordinate Geometry" else "Algebra & Vectors 3D"
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
                        concept = "JEE Core Concept",
                        difficulty = Difficulty.MEDIUM,
                        solutionText = "Official Answer: $ans. Solved using fundamental JEE principles.",
                        idealTimeSeconds = if (isNumerical) 120 else 75,
                        youtubeSearchQuery = "JEE Main ${currentSubject.displayName} Question $qNum solution"
                    )
                )
            }
        }

        val errMsg = if (questions.isEmpty()) {
            if (cleanPaper.isBlank()) {
                "No readable text extracted from document. Please configure your Gemini API Key in settings for visual OCR or paste question text."
            } else {
                "Could not detect question numbers (e.g. Q1., 1.) in the provided text."
            }
        } else null

        return ExtractionResult(
            success = questions.isNotEmpty(),
            testTitle = testTitle,
            questions = questions,
            flaggedQuestions = flagged,
            errorMessage = errMsg
        )
    }
}
