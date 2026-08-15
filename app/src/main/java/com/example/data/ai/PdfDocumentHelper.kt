package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import com.example.ai.PdfSourceType
import com.example.ai.ProcessingStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

object PdfDocumentHelper {
    private const val TAG = "PdfDocumentHelper"

    data class ExtractedDocument(
        val fileName: String,
        val extractedText: String,
        val pageCount: Int,
        val base64Images: List<String> = emptyList(),
        val pdfType: PdfSourceType = PdfSourceType.NATIVE_TEXT
    )

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = ""
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) name = cursor.getString(nameIndex) ?: ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve display name: ${e.message}")
        }
        if (name.isBlank()) {
            name = uri.lastPathSegment?.substringAfterLast("/") ?: "Uploaded_Document.pdf"
            if (name.startsWith("document:")) name = "JEE_Question_Paper_${name.substringAfter("document:")}.pdf"
        }
        return name
    }

    /**
     * Reads the whole PDF and renders EVERY source page at a moderate resolution.
     * The rendered pages are the vision source for structure detection; the cropper
     * later re-renders the original PDF at higher resolution for the final visual.
     */
    suspend fun extractContentFromUri(
        context: Context,
        uri: Uri,
        maxPagesToRender: Int = Int.MAX_VALUE,
        onProgress: (step: ProcessingStep, percent: Int, message: String, pagesDone: Int, totalPages: Int) -> Unit = { _, _, _, _, _ -> }
    ): ExtractedDocument = withContext(Dispatchers.IO) {
        val fileName = getFileNameFromUri(context, uri)
        onProgress(ProcessingStep.ANALYZING_PDF, 5, "Analysing document format for $fileName...", 0, 0)

        var directText = ""
        try {
            (if (uri.scheme == "file") FileInputStream(File(uri.path ?: return@withContext ExtractedDocument(fileName, "", 0, emptyList(), PdfSourceType.SCANNED_IMAGE))) else context.contentResolver.openInputStream(uri))?.use { stream ->
                val bytes = stream.readBytes()
                val candidateText = String(bytes, Charsets.UTF_8)
                if (!candidateText.startsWith("%PDF") && candidateText.count { it.isLetterOrDigit() || it.isWhitespace() } > candidateText.length * 0.6) {
                    directText = candidateText
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct text check skipped: ${e.message}")
        }
        if (directText.isNotBlank()) {
            onProgress(ProcessingStep.ANALYZING_PDF, 15, "Direct text extracted", 1, 1)
            return@withContext ExtractedDocument(fileName, directText, 1, emptyList(), PdfSourceType.NATIVE_TEXT)
        }

        val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.pdf")
        var pageCount = 0
        val base64Images = mutableListOf<String>()
        val textBuffer = StringBuilder()
        var detectedPdfType = PdfSourceType.NATIVE_TEXT

        try {
            (if (uri.scheme == "file") FileInputStream(File(uri.path ?: return@withContext ExtractedDocument(fileName, "", 0, emptyList(), PdfSourceType.SCANNED_IMAGE))) else context.contentResolver.openInputStream(uri))?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            }

            onProgress(ProcessingStep.DETECTING_PDF_TYPE, 12, "Detecting PDF type...", 0, 0)
            val rawPdfBytes = tempFile.readBytes()
            val extractedTokens = extractReadableTextFromPdfBytes(rawPdfBytes)
            if (extractedTokens.isNotBlank()) textBuffer.append(extractedTokens)

            detectedPdfType = when {
                textBuffer.length > 200 && textBuffer.count { it.isLetterOrDigit() } > 100 -> PdfSourceType.NATIVE_TEXT
                textBuffer.length > 50 -> PdfSourceType.MIXED
                else -> PdfSourceType.SCANNED_IMAGE
            }

            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount
            onProgress(ProcessingStep.RENDERING_PAGES, 18, "Rendering all $pageCount source pages...", 0, pageCount)

            val pagesToProcess = minOf(pageCount, maxPagesToRender)
            for (i in 0 until pagesToProcess) {
                val page = renderer.openPage(i)
                val targetWidth = 1000
                val scale = if (page.width > 0) (targetWidth.toFloat() / page.width.toFloat()).coerceIn(0.8f, 1.6f) else 1f
                val width = (page.width * scale).toInt().coerceAtLeast(1)
                val height = (page.height * scale).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 72, outputStream)
                base64Images += Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                bitmap.recycle()

                val percent = 18 + ((i + 1) * 35 / maxOf(1, pagesToProcess))
                onProgress(ProcessingStep.RENDERING_PAGES, percent, "Rendered page ${i + 1} of $pageCount", i + 1, pageCount)
            }
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing PDF: ${e.message}", e)
        } finally {
            runCatching { tempFile.delete() }
        }

        ExtractedDocument(
            fileName = fileName,
            extractedText = textBuffer.toString().trim(),
            pageCount = pageCount,
            base64Images = base64Images,
            pdfType = detectedPdfType
        )
    }

    private fun extractReadableTextFromPdfBytes(bytes: ByteArray): String {
        val sb = StringBuilder()
        val text = String(bytes, Charsets.ISO_8859_1)
        val btEtRegex = Regex("""BT(.*?)ET""", RegexOption.DOT_MATCHES_ALL)
        val matches = btEtRegex.findAll(text)
        var foundStructuredText = false
        for (match in matches.take(500)) {
            val block = match.groupValues[1]
            val parenRegex = Regex("""\((.*?)\)\s*T[jJ]""")
            for (pm in parenRegex.findAll(block)) {
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

        if (!foundStructuredText || sb.length < 100) {
            try {
                val streamStartTag = "stream\r\n".toByteArray(Charsets.ISO_8859_1)
                val streamStartTag2 = "stream\n".toByteArray(Charsets.ISO_8859_1)
                val streamEndTag = "endstream".toByteArray(Charsets.ISO_8859_1)
                var index = 0
                var decompressedStreams = 0
                while (index < bytes.size - 20 && decompressedStreams < 60) {
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
                    if (streamLen in 10..300000) {
                        try {
                            val inflater = Inflater(false)
                            val bais = ByteArrayInputStream(bytes, dataStart, streamLen)
                            val iis = InflaterInputStream(bais, inflater)
                            val unzippedStr = String(iis.readBytes(), Charsets.ISO_8859_1)
                            for (m in btEtRegex.findAll(unzippedStr).take(100)) {
                                for (pm in Regex("""\((.*?)\)\s*T[jJ]""").findAll(m.groupValues[1])) {
                                    val token = pm.groupValues[1]
                                    if (token.isNotBlank()) {
                                        sb.append(token).append(" ")
                                        foundStructuredText = true
                                    }
                                }
                                sb.append("\n")
                            }
                            decompressedStreams++
                        } catch (_: Exception) {}
                    }
                    index = sEnd + streamEndTag.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Stream decompression skipped: ${e.message}")
            }
        }

        if (foundStructuredText && sb.length > 50) return cleanExtractedPdfText(sb.toString())

        val asciiRegex = Regex("""[A-Za-z0-9\s\.\,\:\;\(\)\+\-\*\/\=\>\<\?\!\@\#\$\%\^\&\_\{\}\[\]\~]{4,}""")
        val fallbackSb = StringBuilder()
        for (m in asciiRegex.findAll(text).take(2000)) {
            val segment = m.value.trim()
            if (segment.length >= 4 && !segment.startsWith("stream") && !segment.startsWith("endstream") && !segment.startsWith("xref")) fallbackSb.append(segment).append("\n")
        }
        return cleanExtractedPdfText(fallbackSb.toString())
    }

    private fun indexOf(source: ByteArray, target: ByteArray, fromIndex: Int): Int {
        if (fromIndex >= source.size) return -1
        outer@ for (i in fromIndex..(source.size - target.size)) {
            for (j in target.indices) if (source[i + j] != target[j]) continue@outer
            return i
        }
        return -1
    }

    private fun cleanExtractedPdfText(raw: String): String = raw.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.startsWith("obj") && !it.startsWith("endobj") && !it.startsWith("/Font") }
        .joinToString("\n")
}
