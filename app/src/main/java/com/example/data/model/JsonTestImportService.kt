package com.example.data.model

import android.content.Context
import android.net.Uri
import org.json.JSONObject

/** Reads, validates and converts a generic JSON test file into the KARAM test-link payload. */
object JsonTestImportService {
    fun readAndValidate(context: Context, uri: Uri): TestLinkPayload {
        val text = JsonTestFileReader.read(context, uri)
        val parsed = GenericJsonTestImporter.parse(text)
        return TestLinkPayload(
            testId = parsed.testId,
            title = parsed.title,
            durationMinutes = parsed.durationMinutes,
            questionsJson = parsed.questions.toString(),
            answerKeyJson = parsed.answerKey.toString()
        )
    }
}
