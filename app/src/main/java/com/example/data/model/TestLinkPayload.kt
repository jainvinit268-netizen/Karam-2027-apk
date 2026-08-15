package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/** A portable, self-contained saved-test payload for KARAM test links. */
data class TestLinkPayload(
    val testId: String,
    val title: String,
    val durationMinutes: Int,
    val questionsJson: String,
    val answerKeyJson: String = ""
) {
    fun encode(): String {
        val root = JSONObject()
            .put("v", 1)
            .put("testId", testId)
            .put("title", title)
            .put("durationMinutes", durationMinutes)
            .put("questions", JSONArray(questionsJson))
            .put("answerKey", if (answerKeyJson.isBlank()) JSONArray() else JSONArray(answerKeyJson))
        return android.util.Base64.encodeToString(root.toString().toByteArray(Charsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
    }

    companion object {
        fun decode(token: String): TestLinkPayload {
            val raw = String(android.util.Base64.decode(token, android.util.Base64.URL_SAFE), Charsets.UTF_8)
            val root = JSONObject(raw)
            return TestLinkPayload(
                testId = root.getString("testId"),
                title = root.getString("title"),
                durationMinutes = root.optInt("durationMinutes", 180),
                questionsJson = root.optJSONArray("questions")?.toString() ?: "[]",
                answerKeyJson = root.optJSONArray("answerKey")?.toString() ?: "[]"
            )
        }
    }
}
