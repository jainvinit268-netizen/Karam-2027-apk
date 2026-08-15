package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/** Parses a complete Gemini-generated test JSON document into the existing KARAM payload model. */
object DirectJsonTestImporter {
    fun parse(json: String): TestLinkPayload {
        val root = JSONObject(json.trim())
        val testId = root.optString("testId").trim()
        require(testId.isNotBlank()) { "Missing testId." }
        val title = root.optString("title", "Imported JEE CBT").ifBlank { "Imported JEE CBT" }
        val duration = root.optInt("durationMinutes", 180).coerceIn(1, 600)
        val questions = root.optJSONArray("questions") ?: throw IllegalArgumentException("Missing questions array.")
        require(questions.length() > 0) { "The JSON contains no questions." }

        // Keep the complete question objects intact so the existing repository parser
        // remains the single source of truth for CBT rendering and local persistence.
        val answerKey = root.optJSONArray("answerKey") ?: JSONArray()
        return TestLinkPayload(
            testId = testId,
            title = title,
            durationMinutes = duration,
            questionsJson = questions.toString(),
            answerKeyJson = answerKey.toString()
        )
    }
}
