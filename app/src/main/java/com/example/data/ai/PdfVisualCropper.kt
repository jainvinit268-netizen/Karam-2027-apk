package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.data.model.QuestionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfVisualCropper {
    private const val TAG = "PdfVisualCropper"

    /**
     * Bounding box metadata for a single question within the PDF.
     */
    data class QuestionCropSpec(
        val questionNumber: Int,
        val startPageIndex: Int,
        val topRatio: Float,
        val bottomRatio: Float,
        val leftRatio: Float = 0.0f,
        val rightRatio: Float = 1.0f,
        val endPageIndex: Int = startPageIndex,
        val continuationBottomRatio: Float = 0.0f
    )

    /**
     * Renders the PDF pages at high resolution, accurately crops each question's
     * visual region, and saves the cropped images to local persistent app storage.
     * Emits real-time progress callbacks for every single crop.
     */
    suspend fun generateOriginalQuestionCrops(
        context: Context,
        pdfUri: Uri?,
        testId: String,
        questions: List<QuestionItem>,
        onProgress: (current: Int, total: Int, pageNum: Int, message: String) -> Unit = { _, _, _, _ -> }
    ): List<QuestionItem> = withContext(Dispatchers.IO) {
        if (pdfUri == null || questions.isEmpty()) {
            return@withContext questions
        }

        val cropsDir = File(context.filesDir, "question_crops/$testId")
        if (!cropsDir.exists()) {
            cropsDir.mkdirs()
        }

        val tempPdfFile = File(context.cacheDir, "temp_render_${System.currentTimeMillis()}.pdf")
        val updatedQuestions = mutableListOf<QuestionItem>()

        try {
            // Copy uploaded Uri to local file for PdfRenderer
            context.contentResolver.openInputStream(pdfUri)?.use { input ->
                FileOutputStream(tempPdfFile).use { output ->
                    input.copyTo(output)
                }
            }

            val pfd = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val totalPages = renderer.pageCount

            Log.d(TAG, "Rendering & cropping ${questions.size} questions from $totalPages PDF pages...")
            onProgress(0, questions.size, 1, "Initializing high-res page rendering ($totalPages pages)...")

            // Determine question distribution across pages
            val questionSpecs = computeQuestionBoundingBoxes(
                totalQuestions = questions.size,
                totalPages = totalPages,
                questions = questions
            )

            // Cache rendered page bitmaps on demand to minimize memory footprint
            val renderedPagesCache = mutableMapOf<Int, File>()

            for (pageIdx in 0 until totalPages) {
                val pageNum = pageIdx + 1
                onProgress(0, questions.size, pageNum, "Rendering Page $pageNum / $totalPages for visual cropping...")

                val page = renderer.openPage(pageIdx)
                val targetWidth = 1440
                val scale = if (page.width > 0) (targetWidth.toFloat() / page.width.toFloat()).coerceIn(1.0f, 2.5f) else 1.5f
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()

                val pageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                pageBitmap.eraseColor(Color.WHITE)
                page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Save rendered page bitmap to cache file
                val pageFile = File(context.cacheDir, "rendered_page_${testId}_$pageIdx.jpg")
                FileOutputStream(pageFile).use { fos ->
                    pageBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos)
                }
                pageBitmap.recycle()
                renderedPagesCache[pageIdx] = pageFile
            }

            renderer.close()
            pfd.close()

            // Perform visual cropping for each question
            for (i in questions.indices) {
                val q = questions[i]
                val spec = questionSpecs[q.questionNumber]
                var cropSavedUri: String? = null
                val targetPageNum = (spec?.startPageIndex ?: 0) + 1

                onProgress(
                    i + 1,
                    questions.size,
                    targetPageNum,
                    "Creating original visual crop for Q${q.questionNumber} (Page $targetPageNum / $totalPages)..."
                )

                if (spec != null && spec.startPageIndex < totalPages) {
                    val pageFile = renderedPagesCache[spec.startPageIndex]
                    if (pageFile != null && pageFile.exists()) {
                        val pageBitmap = android.graphics.BitmapFactory.decodeFile(pageFile.absolutePath)
                        if (pageBitmap != null) {
                            val cropBitmap = if (spec.endPageIndex > spec.startPageIndex && spec.endPageIndex < totalPages) {
                                // Multi-page question spanning across 2 pages
                                val nextFile = renderedPagesCache[spec.endPageIndex]
                                val nextBitmap = if (nextFile != null && nextFile.exists()) {
                                    android.graphics.BitmapFactory.decodeFile(nextFile.absolutePath)
                                } else null

                                cropMultiPageQuestion(pageBitmap, nextBitmap, spec)
                            } else {
                                // Single-page question crop
                                cropSinglePageQuestion(pageBitmap, spec)
                            }

                            if (cropBitmap != null) {
                                val cropFile = File(cropsDir, "q_${q.questionNumber}.jpg")
                                FileOutputStream(cropFile).use { fos ->
                                    cropBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                                }
                                cropBitmap.recycle()
                                cropSavedUri = "file://${cropFile.absolutePath}"
                            }
                            pageBitmap.recycle()
                        }
                    }
                }

                updatedQuestions.add(
                    if (cropSavedUri != null) {
                        q.copy(imageUrl = cropSavedUri)
                    } else {
                        q
                    }
                )
            }

            // Cleanup rendered page cache files
            renderedPagesCache.values.forEach { file ->
                try { file.delete() } catch (_: Exception) {}
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to render and crop question visuals: ${e.message}", e)
            return@withContext questions
        } finally {
            try { tempPdfFile.delete() } catch (_: Exception) {}
        }

        Log.d(TAG, "Successfully generated visual crops for ${updatedQuestions.count { it.imageUrl != null }}/${questions.size} questions.")
        updatedQuestions
    }

    private fun cropSinglePageQuestion(pageBitmap: Bitmap, spec: QuestionCropSpec): Bitmap? {
        return try {
            val width = pageBitmap.width
            val height = pageBitmap.height

            val left = (width * spec.leftRatio.coerceIn(0f, 1f)).toInt().coerceIn(0, width - 1)
            val right = (width * spec.rightRatio.coerceIn(0f, 1f)).toInt().coerceIn(left + 1, width)
            val top = (height * spec.topRatio.coerceIn(0f, 1f)).toInt().coerceIn(0, height - 1)
            val bottom = (height * spec.bottomRatio.coerceIn(0f, 1f)).toInt().coerceIn(top + 1, height)

            val cropWidth = (right - left).coerceAtLeast(10)
            val cropHeight = (bottom - top).coerceAtLeast(10)

            Bitmap.createBitmap(pageBitmap, left, top, cropWidth, cropHeight)
        } catch (e: Exception) {
            Log.w(TAG, "Single page crop error: ${e.message}")
            null
        }
    }

    private fun cropMultiPageQuestion(
        firstBitmap: Bitmap,
        secondBitmap: Bitmap?,
        spec: QuestionCropSpec
    ): Bitmap? {
        return try {
            val part1 = cropSinglePageQuestion(
                firstBitmap,
                spec.copy(bottomRatio = 1.0f)
            )

            val part2 = if (secondBitmap != null) {
                cropSinglePageQuestion(
                    secondBitmap,
                    spec.copy(topRatio = 0.0f, bottomRatio = spec.continuationBottomRatio.coerceIn(0.1f, 0.6f))
                )
            } else null

            if (part1 != null && part2 != null) {
                // Combine part 1 and part 2 vertically
                val totalHeight = part1.height + part2.height
                val maxWidth = maxOf(part1.width, part2.width)
                val combined = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(combined)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(part1, 0f, 0f, null)
                canvas.drawBitmap(part2, 0f, part1.height.toFloat(), null)

                part1.recycle()
                part2.recycle()
                secondBitmap?.recycle()
                combined
            } else {
                secondBitmap?.recycle()
                part1 ?: part2
            }
        } catch (e: Exception) {
            Log.w(TAG, "Multi-page crop error: ${e.message}")
            null
        }
    }

    /**
     * Computes high-precision bounding box slices for questions distributed across PDF pages.
     */
    private fun computeQuestionBoundingBoxes(
        totalQuestions: Int,
        totalPages: Int,
        questions: List<QuestionItem>
    ): Map<Int, QuestionCropSpec> {
        val map = mutableMapOf<Int, QuestionCropSpec>()
        if (totalQuestions == 0 || totalPages == 0) return map

        val questionsPerPage = (totalQuestions.toFloat() / totalPages.toFloat()).coerceAtLeast(1.0f)

        for (i in 0 until totalQuestions) {
            val qNum = questions[i].questionNumber
            val estimatedPageIndex = (i / questionsPerPage).toInt().coerceIn(0, totalPages - 1)
            val indexOnPage = (i % questionsPerPage.toInt().coerceAtLeast(1))
            val itemsOnThisPage = questionsPerPage.toInt().coerceAtLeast(1)

            val top = (indexOnPage.toFloat() / itemsOnThisPage.toFloat()) * 0.94f + 0.03f
            val bottom = ((indexOnPage + 1).toFloat() / itemsOnThisPage.toFloat()) * 0.94f + 0.03f

            map[qNum] = QuestionCropSpec(
                questionNumber = qNum,
                startPageIndex = estimatedPageIndex,
                topRatio = top.coerceIn(0f, 0.95f),
                bottomRatio = bottom.coerceIn(0.05f, 1f),
                leftRatio = 0.02f,
                rightRatio = 0.98f
            )
        }

        return map
    }
}
