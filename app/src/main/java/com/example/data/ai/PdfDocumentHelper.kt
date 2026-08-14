package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

object PdfDocumentHelper {
    private const val TAG = "PdfDocumentHelper"

    data class ExtractedDocument(
        val fileName: String,
        val extractedText: String,
        val pageCount: Int,
        val base64Images: List<String> = emptyList()
    )

    /**
     * Resolves human-readable file name from Android ContentResolver.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = ""
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex) ?: ""
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve display name from ContentResolver: ${e.message}")
        }

        if (name.isBlank()) {
            name = uri.lastPathSegment?.substringAfterLast("/") ?: "Uploaded_Document.pdf"
            if (name.startsWith("document:")) {
                name = "JEE_Question_Paper_${name.substringAfter("document:")}.pdf"
            }
        }
        return name
    }

    /**
     * Reads and extracts content from a user-uploaded PDF or text/image Uri.
     */
    suspend fun extractContentFromUri(
        context: Context,
        uri: Uri,
        maxPagesToRender: Int = 8
    ): ExtractedDocument = withContext(Dispatchers.IO) {
        val fileName = getFileNameFromUri(context, uri)
        val mimeType = context.contentResolver.getType(uri) ?: ""
        
        Log.d(TAG, "Processing Uri: $uri, type: $mimeType, fileName: $fileName")

        // 1. Check direct text if it's text/plain
        var directText = ""
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                val candidateText = String(bytes, Charsets.UTF_8)
                if (!candidateText.startsWith("%PDF") && candidateText.count { it.isLetterOrDigit() || it.isWhitespace() } > (candidateText.length * 0.6)) {
                    directText = candidateText
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct text reading check skipped: ${e.message}")
        }

        if (directText.isNotBlank()) {
            return@withContext ExtractedDocument(
                fileName = fileName,
                extractedText = directText,
                pageCount = 1
            )
        }

        // 2. Extract PDF content
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

            // Extract text tokens from PDF binary stream (including Flate streams)
            val rawPdfBytes = tempFile.readBytes()
            val extractedTokens = extractReadableTextFromPdfBytes(rawPdfBytes)
            if (extractedTokens.isNotBlank()) {
                textBuffer.append(extractedTokens)
            }

            // Render PDF pages as high-clarity Bitmaps for multimodal OCR & vision
            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount

            val pagesToProcess = minOf(pageCount, maxPagesToRender)
            for (i in 0 until pagesToProcess) {
                val page = renderer.openPage(i)
                // Optimize width to 1080px for fast generation & crisp math formula OCR
                val scale = if (page.width > 0) (1080f / page.width.toFloat()).coerceIn(1.0f, 2.0f) else 1.5f
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Fast JPEG compression
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
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
     * Handles both uncompressed text operators and FlateDecode compressed streams.
     */
    private fun extractReadableTextFromPdfBytes(bytes: ByteArray): String {
        val sb = StringBuilder()
        val text = String(bytes, Charsets.ISO_8859_1)

        // 1. Check BT ... ET blocks
        val btEtRegex = Regex("""BT(.*?)ET""", RegexOption.DOT_MATCHES_ALL)
        val matches = btEtRegex.findAll(text)

        var foundStructuredText = false
        for (match in matches) {
            val block = match.groupValues[1]
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

        // 2. Try decompressed Flate streams if structured text is sparse
        if (!foundStructuredText || sb.length < 100) {
            try {
                val streamStartTag = "stream\r\n".toByteArray(Charsets.ISO_8859_1)
                val streamStartTag2 = "stream\n".toByteArray(Charsets.ISO_8859_1)
                val streamEndTag = "endstream".toByteArray(Charsets.ISO_8859_1)

                var index = 0
                while (index < bytes.size - 20) {
                    var sStart = indexOf(bytes, streamStartTag, index)
                    var headerLen = streamStartTag.size
                    if (sStart == -1) {
                        sStart = indexOf(bytes, streamStartTag2, index)
                        headerLen = streamStartTag2.size
                    }
                    if (sStart == -1) break

                    val dataStart = sStart + headerLen
                    val sEnd = indexOf(bytes, streamEndTag, dataStart)
                    if (sEnd == -1) break

                    val streamLen = sEnd - dataStart
                    if (streamLen in 10..500000) {
                        try {
                            val inflater = Inflater(false)
                            val bais = ByteArrayInputStream(bytes, dataStart, streamLen)
                            val iis = InflaterInputStream(bais, inflater)
                            val unzipped = iis.readBytes()
                            val unzippedStr = String(unzipped, Charsets.ISO_8859_1)
                            
                            val innerMatches = btEtRegex.findAll(unzippedStr)
                            for (m in innerMatches) {
                                val parenMatches = Regex("""\((.*?)\)\s*T[jJ]""").findAll(m.groupValues[1])
                                for (pm in parenMatches) {
                                    val token = pm.groupValues[1]
                                    if (token.isNotBlank()) {
                                        sb.append(token).append(" ")
                                        foundStructuredText = true
                                    }
                                }
                                sb.append("\n")
                            }
                        } catch (_: Exception) {}
                    }
                    index = sEnd + streamEndTag.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Stream decompression skipped: ${e.message}")
            }
        }

        if (foundStructuredText && sb.length > 50) {
            return cleanExtractedPdfText(sb.toString())
        }

        // Fallback: extract continuous printable text chunks
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

    private fun indexOf(source: ByteArray, target: ByteArray, fromIndex: Int): Int {
        if (fromIndex >= source.size) return -1
        outer@ for (i in fromIndex..(source.size - target.size)) {
            for (j in target.indices) {
                if (source[i + j] != target[j]) continue@outer
            }
            return i
        }
        return -1
    }

    private fun cleanExtractedPdfText(raw: String): String {
        return raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("obj") && !it.startsWith("endobj") && !it.startsWith("/Font") }
            .joinToString("\n")
    }
}

