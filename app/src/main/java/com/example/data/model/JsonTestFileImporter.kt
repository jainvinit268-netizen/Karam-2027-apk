package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/** Parses a portable JEE CBT JSON file into the same saved-test payload used by KARAM links. */
object JsonTestFileImporter {
    data class ParsedTest(
        val payload: TestLinkPayload,
        val questionsJson: String
    )

    fun parse(jsonText: String): ParsedTest {
        require(jsonText.isNotBlank()) { "JSON file is empty." }
        val root = JSONObject(jsonText.trim())
        val testId = root.optString("testId").trim().ifBlank {
            "JSON_${System.currentTimeMillis()}"
        }
        val title = root.optString("title", "Imported JEE CBT").trim().ifBlank { "Imported JEE CBT" }
        val duration = root.optInt("durationMinutes", 180).coerceIn(1, 600)
        val questions = root.optJSONArray("questions")
            ?: throw IllegalArgumentException("JSON must contain a 'questions' array.")
        require(questions.length() > 0) { "JSON contains no questions." }

        val normalizedQuestions = JSONArray()
        for (i in 0 until questions.length()) {
            val q = questions.optJSONObject(i)
                ?: throw IllegalArgumentException("Question ${i + 1} is not a JSON object.")
            require(q.optString("questionId").isNotBlank()) { "Question ${i + 1} is missing questionId." }
            require(q.optInt("questionNumber", -1) >= 0) { "Question ${i + 1} is missing questionNumber." }
            require(q.optString("subject").isNotBlank()) { "Question ${i + 1} is missing subject." }
            require(q.optString("questionType").isNotBlank()) { "Question ${i + 1} is missing questionType." }
            require(q.optString("questionText").isNotBlank()) { "Question ${i + 1} is missing questionText." }
            require(q.optString("correctAnswer").isNotBlank()) { "Question ${i + 1} is missing correctAnswer." }
            if (q.optString("questionType").equals("MCQ", ignoreCase = true)) {
                val options = q.optJSONArray("options")
                require(options != null && options.length() >= 2) { "MCQ question ${q.optInt("questionNumber")} must contain options." }
            }
            normalizedQuestions.put(q)
        }

        val answerKey = root.optJSONArray("answerKey") ?: JSONArray()
        val payload = TestLinkPayload(
            testId = testId,
            title = title,
            durationMinutes = duration,
            questionsJson = normalizedQuestions.toString(),
            answerKeyJson = answerKey.toString()
        )
        return ParsedTest(payload, normalizedQuestions.toString())
    }
}
