package com.example

import android.content.Context
import android.net.Uri
import com.example.data.model.DirectJsonTestImporter
import com.example.data.model.TestLinkPayload
import java.io.BufferedReader
import java.io.InputStreamReader

/** Reads a user-selected Gemini test JSON file entirely on-device. */
object DirectGeminiJsonImport {
    fun readPayload(context: Context, uri: Uri): TestLinkPayload {
        val json = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
        } ?: throw IllegalArgumentException("Could not open the selected JSON file.")
        require(json.isNotBlank()) { "The selected JSON file is empty." }
        return DirectJsonTestImporter.parse(json)
    }
}
