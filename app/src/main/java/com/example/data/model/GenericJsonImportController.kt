package com.example.data.model

import android.content.Context
import android.net.Uri

/** Small adapter kept separate from Gemini/PDF extraction so any JSON producer works. */
object GenericJsonImportController {
    suspend fun import(context: Context, uri: Uri): String {
        val payload = JsonTestImportService.readAndValidate(context, uri)
        val token = payload.encode()
        return "https://karam-2027.test/t/${java.net.URLEncoder.encode(token, Charsets.UTF_8.name())}"
    }
}
