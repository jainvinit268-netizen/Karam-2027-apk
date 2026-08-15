package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses a generic JEE CBT JSON export. This is intentionally provider-agnostic:
 * Gemini, Claude, ChatGPT, a website, or a local script can produce the JSON.
 */
object GenericJsonTestImporter {
    data class ParsedTest(
        val testId: String,
        val title: String,
        val durationMinutes: Int,
        val questions: JSONArray,
        val answerKey: JSONArray
    )

    fun parse(text: String): ParsedTest {
        val root = JSONObject(text.trim())
        val questions = root.optJSONArray("questions")
            ?: throw IllegalArgumentException("JSON must contain a questions array.")
        require(questions.length() > 0) { "The JSON contains no questions." }

        val seenIds = mutableSetOf<String>()
        for (i in 0 until questions.length()) {
            val q = questions.getJSONObject(i)
            val id = q.optString("questionId").trim()
            require(id.isNotEmpty()) { "Question ${i + 1} is missing questionId." }
            require(seenIds.add(id)) { "Duplicate questionId: $id" }
            require(q.optString("questionText").trim().isNotEmpty()) { "Question $id has empty questionText." }
            require(q.optString("subject").trim().isNotEmpty()) { "Question $id is missing subject." }
            require(q.optString("questionType").trim().isNotEmpty()) { "Question $id is missing questionType." }
            if (q.optString("questionType").equals("MCQ", ignoreCase = true)) {
                require(q.optJSONArray("options") != null) { "MCQ $id is missing options." }
            }
        }

        val id = root.optString("testId").ifBlank { "KARAM_${System.currentTimeMillis()}" }
        val title = root.optString("title").ifBlank { "Imported JEE CBT" }
        val duration = root.optInt("durationMinutes", 180).coerceIn(1, 600)
        val answers = root.optJSONArray("answerKey") ?: JSONArray()

        return ParsedTest(id, title, duration, questions, answers)
    }
}
