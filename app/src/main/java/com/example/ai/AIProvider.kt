package com.example.ai

import com.example.data.ai.AiConnectionStatus
import com.example.data.model.QuestionItem

enum class AIProviderType {
    GEMINI_API,
    LOCAL_LAYOUT_ENGINE
}

enum class PdfSourceType(val displayName: String) {
    NATIVE_TEXT("Native Text PDF (High Speed)"),
    SCANNED_IMAGE("Scanned / Image-Only PDF"),
    MIXED("Mixed Text & Graphical PDF")
}

data class ProcessingProgress(
    val step: ProcessingStep,
    val progressPercent: Int,
    val message: String,
    val pagesProcessed: Int = 0,
    val totalPages: Int = 0,
    val questionsDetected: Int = 0,
    val estimatedRemainingSeconds: Int? = null,
    val pdfType: PdfSourceType? = null
)

enum class ProcessingStep(val displayName: String) {
    UPLOADING("Uploading & Initializing"),
    ANALYZING_PDF("Analysing Document Structure"),
    DETECTING_PDF_TYPE("Detecting PDF Type (Native / Scanned)"),
    RENDERING_PAGES("Rendering High-Resolution Pages"),
    DETECTING_QUESTIONS("Detecting Question Boundaries"),
    CREATING_QUESTION_CROPS("Creating Original Question Crops"),
    READING_ANSWER_KEY("Processing Official Answer Key"),
    MAPPING_ANSWERS("Mapping Question ↔ Answer Pairs"),
    VALIDATING("Validating Accuracy & Numbering"),
    BUILDING_CBT("Building NTA JEE CBT Test"),
    COMPLETE("CBT Generation Complete")
}

data class DocumentProcessingParams(
    val testTitle: String,
    val questionPaperText: String,
    val answerKeyText: String,
    val pageImagesBase64: List<String> = emptyList(),
    val totalPages: Int = 1,
    val durationMinutes: Int = 180,
    val targetQuestionCount: Int? = null,
    val pdfType: PdfSourceType = PdfSourceType.NATIVE_TEXT
)

data class DocumentProcessingResult(
    val success: Boolean,
    val testTitle: String,
    val questions: List<QuestionItem>,
    val flaggedQuestions: List<Int> = emptyList(),
    val providerUsed: AIProviderType,
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val elapsedSeconds: Long = 0,
    val diagramsCount: Int = 0,
    val validatedCount: Int = 0,
    val ocrWarningsCount: Int = 0,
    val pdfType: PdfSourceType = PdfSourceType.NATIVE_TEXT
)

interface AIProvider {
    val name: String
    val providerType: AIProviderType
    suspend fun isConfigured(): Boolean
    suspend fun testConnection(): AiConnectionStatus
    suspend fun processDocument(
        params: DocumentProcessingParams,
        onProgress: (ProcessingProgress) -> Unit
    ): DocumentProcessingResult
}

