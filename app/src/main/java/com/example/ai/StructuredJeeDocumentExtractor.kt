package com.example.ai

import android.content.Context
import android.util.Log
import com.example.data.ai.AiKeyManager
import com.example.data.model.Difficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject
import com.example.data.model.BoundingRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Structure-first JEE extractor.
 *
 * The model is used as a vision/layout assistant. It must return the question
 * block geometry from the rendered source page. Subject is inherited from the
 * explicit section/header evidence reported by the model, never from the
 * semantic topic of the question.
 */
object StructuredJeeDocumentExtractor {
    private const val TAG = "StructuredJeeExtractor"
    private const val MODEL = "gemini-2.5-flash"
    private const val URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
    private const val PAGE_BATCH = 4

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    data class Result(
        val success: Boolean,
        val testTitle: String,
        val questions: List<QuestionItem>,
        val flaggedQuestions: List<Int> = emptyList(),
        val statusMessage: String = "",
        val errorMessage: String? = null
    )

    suspend fun extract(
        context: Context,
        questionText: String,
        answerKeyText: String,
        testTitle: String,
        pageImagesBase64: List<String>,
        totalPages: Int,
        onProgress: (ProcessingProgress) -> Unit
    ): Result = withContext(Dispatchers.IO) {
        val apiKey = AiKeyManager.getInstance(context).getActiveApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result(
                false, testTitle, emptyList(),
                errorMessage = "A Gemini API key is required for verified visual PDF extraction."
            )
        }
        if (pageImagesBase64.isEmpty()) {
            return@withContext Result(
                false, testTitle, emptyList(),
                errorMessage = "No rendered PDF pages were supplied. Refusing text-only extraction because original crop geometry is required."
            )
        }

        val byNumber = linkedMapOf<Int, QuestionItem>()
        val allWarnings = mutableListOf<String>()
        val batches = pageImagesBase64.chunked(PAGE_BATCH)

        try {
            batches.forEachIndexed { batchIndex, batch ->
                val firstPage = batchIndex * PAGE_BATCH
                val lastPage = firstPage + batch.size - 1
                onProgress(
                    ProcessingProgress(
                        step = ProcessingStep.DETECTING_QUESTIONS,
                        progressPercent = 25 + ((batchIndex * 45) / maxOf(1, batches.size)),
                        message = "Mapping question blocks on pages ${firstPage + 1}–${lastPage + 1} of $totalPages...",
                        pagesProcessed = lastPage + 1,
                        totalPages = totalPages
                    )
                )

                val responseText = callGemini(apiKey, buildExtractionPrompt(questionText, firstPage, lastPage), batch, firstPage)
                    ?: throw IllegalStateException("Gemini returned no structured extraction for pages ${firstPage + 1}–${lastPage + 1}.")

                val extracted = parseBatch(responseText, testTitle, firstPage, lastPage)
                if (extracted.isEmpty()) {
                    throw IllegalStateException("No question blocks detected in pages ${firstPage + 1}–${lastPage + 1}.")
                }

                for (q in extracted) {
                    val old = byNumber[q.questionNumber]
                    if (old == null) {
                        byNumber[q.questionNumber] = q
                    } else {
                        // Overlapping page batches may see the same question continuation.
                        // Keep the richer text/geometry and merge source regions.
                        val merged = if (q.questionText.length > old.questionText.length) q else old
                        byNumber[q.questionNumber] = merged.copy(
                            sourcePages = (old.sourcePages + q.sourcePages).distinct().sorted(),
                            boundingRegions = (old.boundingRegions + q.boundingRegions).distinctBy {
                                "${it.pageIndex}:${it.x}:${it.y}:${it.width}:${it.height}"
                            },
                            boundaryConfidence = minOf(old.boundaryConfidence, q.boundaryConfidence),
                            readingOrderConfidence = minOf(old.readingOrderConfidence, q.readingOrderConfidence),
                            subjectConfidence = minOf(old.subjectConfidence, q.subjectConfidence),
                            answerConfidence = minOf(old.answerConfidence, q.answerConfidence),
                            extractionWarnings = (old.extractionWarnings + q.extractionWarnings).distinct()
                        )
                    }
                }
            }

            val questions = byNumber.values.sortedBy { it.questionNumber }.toMutableList()
            val answerMap = parseAnswerKey(answerKeyText)
            val mapped = questions.map { q ->
                val answer = answerMap[q.questionNumber]
                if (answer.isNullOrBlank()) {
                    allWarnings += "Q${q.questionNumber}: official answer not mapped"
                    q.copy(
                        answerConfidence = 0f,
                        extractionWarnings = (q.extractionWarnings + "Official answer key mapping is missing or ambiguous.").distinct()
                    )
                } else {
                    q.copy(correctAnswer = answer, answerConfidence = 1f)
                }
            }

            val flagged = mapped.filter {
                it.boundaryConfidence < 0.80f ||
                    it.readingOrderConfidence < 0.80f ||
                    it.subjectConfidence < 0.80f ||
                    it.answerConfidence < 0.80f ||
                    it.extractionWarnings.isNotEmpty()
            }.map { it.questionNumber }.distinct().sorted()

            Result(
                success = mapped.isNotEmpty(),
                testTitle = testTitle,
                questions = mapped,
                flaggedQuestions = flagged,
                statusMessage = "Structure-first extraction completed: ${mapped.size} question blocks, ${flagged.size} flagged.",
                errorMessage = if (allWarnings.isEmpty()) null else allWarnings.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Verified extraction failed", e)
            Result(
                false,
                testTitle,
                emptyList(),
                statusMessage = "Extraction stopped; no unverified CBT was generated.",
                errorMessage = e.message ?: "Verified document extraction failed."
            )
        }
    }

    private fun buildExtractionPrompt(
        nativeText: String,
        firstPage: Int,
        lastPage: Int
    ): String = buildString {
        appendLine("You are a document-layout extraction engine for JEE CBT generation.")
        appendLine("The attached images are ORIGINAL rendered PDF pages. Page indices are absolute and zero-based.")
        appendLine("This is a STRUCTURE task, not a question-solving task.")
        appendLine()
        appendLine("HARD RULES:")
        appendLine("1. First identify page regions, columns, section/subject headers, and question blocks.")
        appendLine("2. A question block starts at its question number and ends immediately before the next independent question. Include the full body, options, diagrams, graphs and tables belonging to it.")
        appendLine("3. Never return a fragment, options-only crop, diagram-only crop, or neighbouring question content.")
        appendLine("4. For multi-column pages determine reading order from layout and question numbering before assigning question identity.")
        appendLine("5. SUBJECT MUST COME FROM EXPLICIT DOCUMENT STRUCTURE: subject header, section header, paper structure, or an unambiguous inherited section. Never infer subject from what the question appears to be about.")
        appendLine("6. SECTION MUST COME FROM DOCUMENT STRUCTURE. Do not invent Section A/B from the topic.")
        appendLine("7. MCQ requires actual visible options. NUMERICAL must have no MCQ options and must match the paper's numerical-answer section/layout.")
        appendLine("8. Bounding boxes are normalized to the FULL SOURCE PAGE: x,y,width,height each in [0,1]. They must include the complete question block and exclude unrelated neighbouring questions.")
        appendLine("9. If a question continues onto another page, return regions for every page but keep one question number.")
        appendLine("10. If any field is uncertain, lower the corresponding confidence and add a warning. Never guess silently.")
        appendLine("11. Return only questions whose block starts on pages $firstPage..$lastPage. A continuation from another page may be represented only as an additional bounding region of the same question.")
        appendLine()
        appendLine("RETURN JSON ONLY:")
        appendLine("{\"questions\":[{")
        appendLine("\"questionNumber\":37,")
        appendLine("\"subject\":\"PHYSICS\",")
        appendLine("\"subjectEvidence\":\"PHYSICS\",")
        appendLine("\"section\":\"Section B\",")
        appendLine("\"sectionEvidence\":\"SECTION B: NUMERICAL VALUE\",")
        appendLine("\"type\":\"NUMERICAL\",")
        appendLine("\"questionText\":\"complete visible question text\",")
        appendLine("\"options\":[],")
        appendLine("\"sourceRegions\":[{\"pageIndex\":6,\"x\":0.08,\"y\":0.31,\"width\":0.84,\"height\":0.22}],")
        appendLine("\"boundaryConfidence\":0.98,")
        appendLine("\"readingOrderConfidence\":0.98,")
        appendLine("\"subjectConfidence\":0.99,")
        appendLine("\"warnings\":[]")
        appendLine("}]}")
        if (nativeText.isNotBlank()) {
            appendLine()
            appendLine("NATIVE TEXT IS ONLY A CROSS-CHECK. IT IS NOT THE SOURCE OF QUESTION GEOMETRY:")
            appendLine(nativeText.take(12000))
        }
    }

    private fun callGemini(
        apiKey: String,
        prompt: String,
        images: List<String>,
        absoluteFirstPage: Int
    ): String? {
        val parts = JSONArray()
        parts.put(JSONObject().apply { put("text", prompt) })
        images.forEachIndexed { index, base64 ->
            if (base64.isNotBlank()) {
                parts.put(JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", "image/jpeg")
                        put("data", base64)
                    })
                })
                parts.put(JSONObject().apply {
                    put("text", "The previous image is absolute PDF page ${absoluteFirstPage + index}. Keep this page identity exact.")
                })
            }
        }

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply { put("parts", parts) })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.0)
                put("topP", 0.8)
                put("maxOutputTokens", 12000)
                put("responseFormat", JSONObject().apply { put("mimeType", "application/json") })
            })
        }

        val request = Request.Builder()
            .url("$URL?key=$apiKey")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini HTTP ${response.code}: ${responseBody?.take(300)}")
            }
            val root = JSONObject(responseBody ?: return null)
            return root.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
        }
    }

    private fun parseBatch(jsonText: String, title: String, firstPage: Int, lastPage: Int): List<QuestionItem> {
        val root = JSONObject(jsonText)
        val array = root.optJSONArray("questions") ?: return emptyList()
        val out = mutableListOf<QuestionItem>()

        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            val qNum = o.optInt("questionNumber", -1)
            if (qNum !in 1..300) continue

            val subject = when (o.optString("subject").uppercase()) {
                "PHYSICS" -> Subject.PHYSICS
                "CHEMISTRY" -> Subject.CHEMISTRY
                "MATHEMATICS", "MATHS", "MATH" -> Subject.MATHEMATICS
                else -> null
            } ?: continue

            val type = when (o.optString("type").uppercase()) {
                "NUMERICAL", "NUMERICAL_VALUE", "NAT" -> QuestionType.NUMERICAL
                "MCQ", "SINGLE_CHOICE" -> QuestionType.MCQ
                else -> continue
            }

            val section = o.optString("section").trim()
            val subjectEvidence = o.optString("subjectEvidence").trim()
            val sectionEvidence = o.optString("sectionEvidence").trim()
            val text = o.optString("questionText").trim()
            val options = mutableListOf<String>()
            o.optJSONArray("options")?.let { opts ->
                for (j in 0 until opts.length()) options += opts.optString(j)
            }

            val regions = mutableListOf<BoundingRegion>()
            o.optJSONArray("sourceRegions")?.let { rs ->
                for (j in 0 until rs.length()) {
                    val r = rs.optJSONObject(j) ?: continue
                    val p = r.optInt("pageIndex", -1)
                    val x = r.optDouble("x", -1.0).toFloat()
                    val y = r.optDouble("y", -1.0).toFloat()
                    val w = r.optDouble("width", -1.0).toFloat()
                    val h = r.optDouble("height", -1.0).toFloat()
                    if (p in firstPage..lastPage || p >= 0) {
                        regions += BoundingRegion(p, x, y, w, h)
                    }
                }
            }

            val warnings = mutableListOf<String>()
            warnings += o.optJSONArray("warnings")?.let { a ->
                (0 until a.length()).map { a.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList()
            if (subjectEvidence.isBlank()) warnings += "No explicit subject-header evidence returned."
            if (sectionEvidence.isBlank()) warnings += "No explicit section evidence returned."
            if (regions.isEmpty()) warnings += "No source geometry returned."
            if (type == QuestionType.MCQ && options.size < 2) warnings += "MCQ has fewer than two visible options."
            if (type == QuestionType.NUMERICAL && options.isNotEmpty()) warnings += "Numerical question contains visible options."
            if (text.length < 12) warnings += "Question text appears incomplete."

            val chapter = "Not classified during structural extraction"
            out += QuestionItem(
                id = "${subject.shortCode}_$qNum",
                questionNumber = qNum,
                subject = subject,
                section = section.ifBlank { "Uncertain" },
                type = type,
                questionText = text,
                options = options,
                correctAnswer = "",
                chapter = chapter,
                concept = "Not classified during structural extraction",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Solution not generated during document extraction.",
                idealTimeSeconds = if (type == QuestionType.NUMERICAL) 120 else 90,
                youtubeSearchQuery = "JEE ${subject.displayName} Question $qNum solution",
                sourcePages = regions.map { it.pageIndex }.distinct().sorted(),
                boundingRegions = regions,
                boundaryConfidence = o.optDouble("boundaryConfidence", 0.0).toFloat(),
                readingOrderConfidence = o.optDouble("readingOrderConfidence", 0.0).toFloat(),
                subjectConfidence = o.optDouble("subjectConfidence", 0.0).toFloat(),
                answerConfidence = 0f,
                extractionWarnings = warnings.distinct()
            )
        }
        return out
    }

    private fun parseAnswerKey(content: String): Map<Int, String> {
        if (content.isBlank()) return emptyMap()
        val result = mutableMapOf<Int, String>()
        val tokenRegex = Regex("""(?:Q(?:uestion)?\s*)?(\d{1,3})\s*(?:[:.\-=>]|\s)\s*[\[(]?([A-Da-d]|-?\d+(?:\.\d+)?)\]?)""")
        for (match in tokenRegex.findAll(content)) {
            val number = match.groupValues[1].toIntOrNull() ?: continue
            val answer = match.groupValues[2].uppercase().trim()
            if (number in 1..300 && answer.isNotBlank()) result.putIfAbsent(number, answer)
        }
        return result
    }
}
