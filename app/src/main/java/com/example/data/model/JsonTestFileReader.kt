package com.example.data.model

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object JsonTestFileReader {
    fun read(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
        } ?: throw IllegalArgumentException("Could not open JSON file.")
    }
}
