package com.example.ai

import com.example.data.ai.AiConnectionStatus
import com.example.data.model.QuestionItem

enum class AIProviderType {
    GEMINI_API,
    LOCAL_LAYOUT_ENGINE
}

data class ProcessingProgress(
    val step: ProcessingStep,
    val progressPercent: Int,
    val message: String,
    val pagesProcessed: Int = 0,
    val totalPages: Int = 0,
    val questionsDetected: Int = 0,
    val estimatedRemainingSeconds: Int? = null
)

enum class ProcessingStep(val displayName: String) {
    READING_PDF("Reading & Extracting Document"),
    DETECTING_LAYOUT("Analyzing Document Layout"),
    DETECTING_QUESTIONS("Detecting Question Boundaries"),
    EXTRACTING_OPTIONS("Extracting Options & Formulas"),
    READING_ANSWER_KEY("Reading Official Answer Key"),
    MAPPING_ANSWERS("Mapping Question ↔ Answer"),
    VALIDATING("Validating Test Structure"),
    BUILDING_CBT("Building NTA JEE CBT Test"),
    COMPLETE("Processing Complete")
}

data class DocumentProcessingParams(
    val testTitle: String,
    val questionPaperText: String,
    val answerKeyText: String,
    val pageImagesBase64: List<String> = emptyList(),
    val totalPages: Int = 1,
    val durationMinutes: Int = 180,
    val targetQuestionCount: Int? = null
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
    val ocrWarningsCount: Int = 0
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
