package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PdfDocumentHelper {
    private const val TAG = "PdfDocumentHelper"

    data class ExtractedDocument(
        val fileName: String,
        val extractedText: String,
        val pageCount: Int,
        val base64Images: List<String> = emptyList()
    )

    /**
     * Reads and extracts content from a user-uploaded PDF or text/image Uri.
     */
    suspend fun extractContentFromUri(
        context: Context,
        uri: Uri,
        maxPagesToRender: Int = 10
    ): ExtractedDocument = withContext(Dispatchers.IO) {
        val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Document.pdf"
        val mimeType = context.contentResolver.getType(uri) ?: ""
        
        Log.d(TAG, "Processing Uri: $uri, type: $mimeType, fileName: $fileName")

        // 1. Try reading direct text if it's text/plain or readable text
        var directText = ""
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                val candidateText = String(bytes, Charsets.UTF_8)
                // If it contains legible alphanumeric text and is not raw binary PDF
                if (!candidateText.startsWith("%PDF") && candidateText.count { it.isLetterOrDigit() || it.isWhitespace() } > (candidateText.length * 0.7)) {
                    directText = candidateText
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct text reading failed: ${e.message}")
        }

        if (directText.isNotBlank()) {
            return@withContext ExtractedDocument(
                fileName = fileName,
                extractedText = directText,
                pageCount = 1
            )
        }

        // 2. If it's a PDF, extract via PdfRenderer & native text stream
        val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.pdf")
        var pageCount = 0
        val base64Images = mutableListOf<String>()
        val textBuffer = StringBuilder()

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Extract embedded text tokens from PDF binary stream if possible
            val rawPdfBytes = tempFile.readBytes()
            val extractedTokens = extractReadableTextFromPdfBytes(rawPdfBytes)
            if (extractedTokens.isNotBlank()) {
                textBuffer.append(extractedTokens)
            }

            // Render PDF pages as high-resolution Bitmaps for vision/multimodal layout understanding
            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount

            val pagesToProcess = minOf(pageCount, maxPagesToRender)
            for (i in 0 until pagesToProcess) {
                val page = renderer.openPage(i)
                // 1.5x scaling for clear OCR/diagram resolution
                val width = (page.width * 1.5).toInt()
                val height = (page.height * 1.5).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Compress bitmap to JPEG Base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                base64Images.add(base64)
                bitmap.recycle()
            }
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering PDF: ${e.message}", e)
        } finally {
            try { tempFile.delete() } catch (_: Exception) {}
        }

        val finalText = textBuffer.toString().trim()
        ExtractedDocument(
            fileName = fileName,
            extractedText = finalText,
            pageCount = pageCount,
            base64Images = base64Images
        )
    }

    /**
     * Extracts readable text strings and stream chunks from a PDF file.
     */
    private fun extractReadableTextFromPdfBytes(bytes: ByteArray): String {
        val sb = StringBuilder()
        val text = String(bytes, Charsets.ISO_8859_1)

        // Find standard PDF BT ... ET (Begin Text ... End Text) blocks
        val btEtRegex = Regex("""BT(.*?)ET""", RegexOption.DOT_MATCHES_ALL)
        val matches = btEtRegex.findAll(text)

        var foundStructuredText = false
        for (match in matches) {
            val block = match.groupValues[1]
            // Extract text in parentheses (Tj or TJ operators)
            val parenRegex = Regex("""\((.*?)\)\s*T[jJ]""")
            val parenMatches = parenRegex.findAll(block)
            for (pm in parenMatches) {
                val token = pm.groupValues[1]
                    .replace("\\(", "(")
                    .replace("\\)", ")")
                    .replace("\\n", "\n")
                    .replace("\\r", "")
                if (token.isNotBlank()) {
                    sb.append(token).append(" ")
                    foundStructuredText = true
                }
            }
            sb.append("\n")
        }

        if (foundStructuredText && sb.length > 50) {
            return cleanExtractedPdfText(sb.toString())
        }

        // Fallback: extract continuous printable ASCII chunks of length >= 3
        val asciiRegex = Regex("""[A-Za-z0-9\s\.\,\:\;\(\)\+\-\*\/\=\>\<\?\!\@\#\$\%\^\&\_\{\}\[\]\~]{4,}""")
        val asciiMatches = asciiRegex.findAll(text)
        val fallbackSb = StringBuilder()
        for (m in asciiMatches) {
            val segment = m.value.trim()
            if (segment.length >= 4 && !segment.startsWith("stream") && !segment.startsWith("endstream") && !segment.startsWith("xref")) {
                fallbackSb.append(segment).append("\n")
            }
        }

        return cleanExtractedPdfText(fallbackSb.toString())
    }

    private fun cleanExtractedPdfText(raw: String): String {
        return raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("obj") && !it.startsWith("endobj") && !it.startsWith("/Font") }
            .joinToString("\n")
    }
}
