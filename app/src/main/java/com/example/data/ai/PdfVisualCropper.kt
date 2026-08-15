package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.data.model.BoundingRegion
import com.example.data.model.QuestionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

/**
 * Creates question visuals ONLY from original PDF page geometry.
 *
 * Important: this class deliberately refuses to guess a question's crop from
 * its ordinal position. A wrong crop is worse than no crop. The extraction
 * layer must provide source page + bounding regions first.
 */
object PdfVisualCropper {
    private const val TAG = "PdfVisualCropper"

    suspend fun generateOriginalQuestionCrops(
        context: Context,
        pdfUri: Uri?,
        testId: String,
        questions: List<QuestionItem>,
        onProgress: (current: Int, total: Int, pageNum: Int, message: String) -> Unit = { _, _, _, _ -> }
    ): List<QuestionItem> = withContext(Dispatchers.IO) {
        if (pdfUri == null || questions.isEmpty()) return@withContext questions

        val cropsDir = File(context.filesDir, "question_crops/$testId")
        cropsDir.mkdirs()
        val tempPdf = File(context.cacheDir, "crop_$testId.pdf")

        try {
            (if (pdfUri.scheme == "file") FileInputStream(File(pdfUri.path ?: return@withContext questions)) else context.contentResolver.openInputStream(pdfUri))?.use { input ->
                FileOutputStream(tempPdf).use { output -> input.copyTo(output) }
            } ?: return@withContext questions

            val pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val pageFiles = mutableMapOf<Int, File>()

            // Render every page because a valid question may continue across pages.
            for (pageIndex in 0 until renderer.pageCount) {
                val page = renderer.openPage(pageIndex)
                val targetWidth = 1800
                val scale = if (page.width > 0) {
                    (targetWidth.toFloat() / page.width.toFloat()).coerceIn(1f, 3f)
                } else 1.5f
                val width = (page.width * scale).toInt().coerceAtLeast(1)
                val height = (page.height * scale).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val file = File(context.cacheDir, "${testId}_page_$pageIndex.jpg")
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) }
                bitmap.recycle()
                pageFiles[pageIndex] = file
            }
            renderer.close()
            pfd.close()

            val output = mutableListOf<QuestionItem>()
            questions.forEachIndexed { index, question ->
                onProgress(index + 1, questions.size, question.sourcePages.firstOrNull()?.plus(1) ?: 0,
                    "Creating original visual crop for Q${question.questionNumber}...")

                // No geometry = no crop. Never substitute page/ordinal heuristics.
                if (question.boundingRegions.isEmpty()) {
                    output += question.copy(
                        imageUrl = null,
                        extractionWarnings = (question.extractionWarnings + "No verified PDF bounding region; visual crop withheld.").distinct()
                    )
                    return@forEachIndexed
                }

                val validRegions = question.boundingRegions.filter {
                    it.pageIndex in pageFiles.keys && it.width > 0f && it.height > 0f &&
                        it.x >= 0f && it.y >= 0f && it.x + it.width <= 1.01f && it.y + it.height <= 1.01f
                }

                if (validRegions.isEmpty()) {
                    output += question.copy(
                        imageUrl = null,
                        extractionWarnings = (question.extractionWarnings + "Verified bounding region was invalid; visual crop withheld.").distinct()
                    )
                    return@forEachIndexed
                }

                val crop = renderRegions(pageFiles, validRegions)
                if (crop == null) {
                    output += question.copy(
                        imageUrl = null,
                        extractionWarnings = (question.extractionWarnings + "Original PDF crop could not be rendered.").distinct()
                    )
                    return@forEachIndexed
                }

                val cropFile = File(cropsDir, "q_${question.questionNumber}.jpg")
                FileOutputStream(cropFile).use { crop.compress(Bitmap.CompressFormat.JPEG, 94, it) }
                crop.recycle()
                output += question.copy(imageUrl = "file://${cropFile.absolutePath}")
            }

            pageFiles.values.forEach { runCatching { it.delete() } }
            Log.d(TAG, "Generated verified original-PDF crops: ${output.count { it.imageUrl != null }}/${questions.size}")
            output
        } catch (e: Exception) {
            Log.e(TAG, "Original PDF crop generation failed", e)
            questions
        } finally {
            runCatching { tempPdf.delete() }
        }
    }

    private fun renderRegions(pageFiles: Map<Int, File>, regions: List<BoundingRegion>): Bitmap? {
        val bitmaps = mutableListOf<Bitmap>()
        try {
            for (region in regions.sortedWith(compareBy<BoundingRegion> { it.pageIndex }.thenBy { it.y })) {
                val page = android.graphics.BitmapFactory.decodeFile(pageFiles[region.pageIndex]?.absolutePath ?: return null)
                    ?: return null
                val left = (region.x * page.width).toInt().coerceIn(0, page.width - 1)
                val top = (region.y * page.height).toInt().coerceIn(0, page.height - 1)
                val right = ((region.x + region.width) * page.width).toInt().coerceIn(left + 1, page.width)
                val bottom = ((region.y + region.height) * page.height).toInt().coerceIn(top + 1, page.height)
                val crop = Bitmap.createBitmap(page, left, top, right - left, bottom - top)
                page.recycle()
                bitmaps += crop
            }

            if (bitmaps.size == 1) return bitmaps.first()
            val width = bitmaps.maxOf { it.width }
            val height = bitmaps.sumOf { it.height }
            val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            combined.eraseColor(Color.WHITE)
            val canvas = android.graphics.Canvas(combined)
            var y = 0f
            bitmaps.forEach {
                canvas.drawBitmap(it, 0f, y, null)
                y += it.height
                it.recycle()
            }
            return combined
        } catch (e: Exception) {
            bitmaps.forEach { runCatching { it.recycle() } }
            return null
        }
    }
}
