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
        val leftRatio: Float = 0.02f,
        val rightRatio: Float = 0.98f,
        val endPageIndex: Int = startPageIndex,
        val continuationBottomRatio: Float = 0.0f
    )

    data class ColumnBounds(
        val leftRatio: Float,
        val rightRatio: Float
    )

    data class DetectedQuestionRegion(
        val pageIndex: Int,
        val columnIndex: Int,
        val topRatio: Float,
        val bottomRatio: Float,
        val leftRatio: Float,
        val rightRatio: Float
    )

    /**
     * Renders the PDF pages at high resolution, accurately crops each question's
     * visual region individually, and saves the cropped images to local persistent app storage.
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

            Log.d(TAG, "Rendering & cropping ${questions.size} questions across $totalPages PDF pages...")
            onProgress(0, questions.size, 1, "Initializing high-res page rendering ($totalPages pages)...")

            // Cache rendered page bitmaps on demand to minimize memory footprint
            val renderedPagesCache = mutableMapOf<Int, File>()
            val detectedRegionsAcrossPdf = mutableListOf<DetectedQuestionRegion>()

            // Phase 1: Render each page at 1440px width and detect column layout and question whitespace anchors
            for (pageIdx in 0 until totalPages) {
                val pageNum = pageIdx + 1
                onProgress(0, questions.size, pageNum, "Rendering Page $pageNum / $totalPages for individual question cropping...")

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

                // Analyze layout: Detect single vs multi-column and distinct question bounding regions on this page
                val regionsOnPage = analyzePageLayoutAndQuestionRegions(pageBitmap, pageIdx)
                detectedRegionsAcrossPdf.addAll(regionsOnPage)

                pageBitmap.recycle()
                renderedPagesCache[pageIdx] = pageFile
            }

            renderer.close()
            pfd.close()

            // Phase 2: Map detected regions to the question objects
            val questionSpecs = mapDetectedRegionsToQuestions(
                questions = questions,
                totalPages = totalPages,
                detectedRegions = detectedRegionsAcrossPdf
            )

            // Phase 3: Perform individual visual cropping for each question
            for (i in questions.indices) {
                val q = questions[i]
                val spec = questionSpecs[q.questionNumber]
                var cropSavedUri: String? = null
                val targetPageNum = (spec?.startPageIndex ?: 0) + 1

                onProgress(
                    i + 1,
                    questions.size,
                    targetPageNum,
                    "Creating individual visual crop for Q${q.questionNumber} (Page $targetPageNum / $totalPages)..."
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
                                // Single individual question crop
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

    /**
     * Analyzes page bitmap to detect:
     * 1. Multi-column vs Single column layout (e.g. 2-column JEE paper with left and right columns)
     * 2. Horizontal whitespace separators and question boundaries within each column
     */
    private fun analyzePageLayoutAndQuestionRegions(
        bitmap: Bitmap,
        pageIndex: Int
    ): List<DetectedQuestionRegion> {
        val width = bitmap.width
        val height = bitmap.height
        val regions = mutableListOf<DetectedQuestionRegion>()

        if (width <= 0 || height <= 0) return regions

        // 1. Detect Columns (Check for 2-column layout with center gutter)
        val columns = detectColumns(bitmap)

        // 2. For each column, detect question blocks from top to bottom
        columns.forEachIndexed { colIdx, col ->
            val colLeftPx = (col.leftRatio * width).toInt().coerceIn(0, width - 1)
            val colRightPx = (col.rightRatio * width).toInt().coerceIn(colLeftPx + 1, width)
            val colWidth = colRightPx - colLeftPx

            if (colWidth > 50) {
                val blockYs = detectHorizontalSeparatorsInColumn(bitmap, colLeftPx, colRightPx, height)
                for (b in blockYs) {
                    regions.add(
                        DetectedQuestionRegion(
                            pageIndex = pageIndex,
                            columnIndex = colIdx,
                            topRatio = b.first,
                            bottomRatio = b.second,
                            leftRatio = col.leftRatio,
                            rightRatio = col.rightRatio
                        )
                    )
                }
            }
        }

        return regions
    }

    /**
     * Detects if the page has 1 or 2 columns by measuring pixel ink density in the center gutter.
     */
    private fun detectColumns(bitmap: Bitmap): List<ColumnBounds> {
        val width = bitmap.width
        val height = bitmap.height

        val midStart = (width * 0.44f).toInt()
        val midEnd = (width * 0.56f).toInt()
        val sampleRows = 40
        var darkInCenter = 0
        var darkInLeft = 0
        var darkInRight = 0

        val leftStart = (width * 0.15f).toInt()
        val leftEnd = (width * 0.35f).toInt()
        val rightStart = (width * 0.65f).toInt()
        val rightEnd = (width * 0.85f).toInt()

        val stepY = maxOf(1, height / sampleRows)

        for (y in 0 until height step stepY) {
            for (x in midStart..midEnd step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (isDarkPixel(pixel)) darkInCenter++
            }
            for (x in leftStart..leftEnd step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (isDarkPixel(pixel)) darkInLeft++
            }
            for (x in rightStart..rightEnd step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (isDarkPixel(pixel)) darkInRight++
            }
        }

        // If left and right have content, but center is mostly white space or vertical line divider, it's 2 columns
        val isTwoColumns = (darkInLeft > 15 && darkInRight > 15) && (darkInCenter < (darkInLeft + darkInRight) * 0.18f)

        return if (isTwoColumns) {
            listOf(
                ColumnBounds(leftRatio = 0.02f, rightRatio = 0.49f),
                ColumnBounds(leftRatio = 0.51f, rightRatio = 0.98f)
            )
        } else {
            listOf(
                ColumnBounds(leftRatio = 0.02f, rightRatio = 0.98f)
            )
        }
    }

    /**
     * Finds horizontal whitespace separators and question boundaries within a specific column.
     */
    private fun detectHorizontalSeparatorsInColumn(
        bitmap: Bitmap,
        leftPx: Int,
        rightPx: Int,
        height: Int
    ): List<Pair<Float, Float>> {
        val blocks = mutableListOf<Pair<Float, Float>>()

        // Content vertical margins (exclude top header and bottom footer)
        val startY = (height * 0.04f).toInt()
        val endY = (height * 0.96f).toInt()
        val totalScanHeight = endY - startY
        if (totalScanHeight <= 0) return blocks

        val stepY = 3
        val rowDarkCount = IntArray((totalScanHeight / stepY) + 1)
        val colWidth = rightPx - leftPx

        for (i in rowDarkCount.indices) {
            val y = (startY + i * stepY).coerceIn(0, height - 1)
            var count = 0
            for (x in leftPx until rightPx step 6) {
                if (isDarkPixel(bitmap.getPixel(x, y))) {
                    count++
                }
            }
            rowDarkCount[i] = count
        }

        // Identify prominent whitespace bands (gaps between questions or horizontal rules)
        val threshold = maxOf(1, (colWidth / 6) / 25)
        var inContent = false
        var currentBlockStartRatio = 0.04f
        val minBlockHeightRatio = 0.08f // Minimum height ratio for a valid question

        for (i in rowDarkCount.indices) {
            val hasContent = rowDarkCount[i] > threshold
            val currentRatio = (startY + i * stepY).toFloat() / height.toFloat()

            if (hasContent && !inContent) {
                inContent = true
                currentBlockStartRatio = maxOf(0.03f, currentRatio - 0.012f)
            } else if (!hasContent && inContent) {
                // Check if whitespace band continues for at least 8-12px
                val isLongWhitespace = (i + 3 < rowDarkCount.size) &&
                        (rowDarkCount[i + 1] <= threshold) &&
                        (rowDarkCount[i + 2] <= threshold)

                if (isLongWhitespace) {
                    val blockEndRatio = minOf(0.97f, currentRatio + 0.012f)
                    if (blockEndRatio - currentBlockStartRatio >= minBlockHeightRatio) {
                        blocks.add(Pair(currentBlockStartRatio, blockEndRatio))
                        inContent = false
                    }
                }
            }
        }

        if (inContent) {
            val blockEndRatio = 0.96f
            if (blockEndRatio - currentBlockStartRatio >= minBlockHeightRatio) {
                blocks.add(Pair(currentBlockStartRatio, blockEndRatio))
            }
        }

        // If no distinct gaps detected, provide clean fallback segments for this column
        if (blocks.isEmpty()) {
            blocks.add(Pair(0.04f, 0.96f))
        }

        return blocks
    }

    private fun isDarkPixel(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        // Greyscale luminance check (< 180 is considered ink/content)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
        return luminance < 185
    }

    /**
     * Maps the detected individual question regions to the target questions in reading order.
     */
    private fun mapDetectedRegionsToQuestions(
        questions: List<QuestionItem>,
        totalPages: Int,
        detectedRegions: List<DetectedQuestionRegion>
    ): Map<Int, QuestionCropSpec> {
        val map = mutableMapOf<Int, QuestionCropSpec>()
        val totalQuestions = questions.size
        if (totalQuestions == 0 || totalPages == 0) return map

        if (detectedRegions.isNotEmpty()) {
            // Distribute regions sequentially across questions in reading order
            for (i in 0 until totalQuestions) {
                val q = questions[i]
                val regionIndex = if (detectedRegions.size >= totalQuestions) {
                    // 1-to-1 or close match
                    ((i.toFloat() / totalQuestions.toFloat()) * detectedRegions.size).toInt().coerceIn(0, detectedRegions.size - 1)
                } else {
                    // More questions than detected macro blocks -> slice within macro blocks
                    i.coerceIn(0, detectedRegions.size - 1)
                }

                val reg = detectedRegions[regionIndex]
                map[q.questionNumber] = QuestionCropSpec(
                    questionNumber = q.questionNumber,
                    startPageIndex = reg.pageIndex,
                    topRatio = reg.topRatio.coerceIn(0.02f, 0.92f),
                    bottomRatio = reg.bottomRatio.coerceIn(reg.topRatio + 0.05f, 0.98f),
                    leftRatio = reg.leftRatio,
                    rightRatio = reg.rightRatio
                )
            }
        } else {
            // Fallback: 2-column or 1-column proportional layout across all pages
            val questionsPerPage = (totalQuestions.toFloat() / totalPages.toFloat()).coerceAtLeast(1.0f)
            for (i in 0 until totalQuestions) {
                val q = questions[i]
                val pageIdx = (i / questionsPerPage).toInt().coerceIn(0, totalPages - 1)
                val idxOnPage = (i % questionsPerPage.toInt().coerceAtLeast(1))
                val countOnPage = questionsPerPage.toInt().coerceAtLeast(1)

                val top = (idxOnPage.toFloat() / countOnPage.toFloat()) * 0.90f + 0.04f
                val bottom = ((idxOnPage + 1).toFloat() / countOnPage.toFloat()) * 0.90f + 0.04f

                map[q.questionNumber] = QuestionCropSpec(
                    questionNumber = q.questionNumber,
                    startPageIndex = pageIdx,
                    topRatio = top.coerceIn(0.02f, 0.92f),
                    bottomRatio = bottom.coerceIn(0.08f, 0.98f),
                    leftRatio = 0.02f,
                    rightRatio = 0.98f
                )
            }
        }

        return map
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
}
