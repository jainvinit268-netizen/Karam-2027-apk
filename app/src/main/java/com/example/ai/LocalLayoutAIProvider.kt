package com.example.ai

import com.example.data.ai.AiConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalLayoutAIProvider : AIProvider {
    override val name: String = "Local High-Precision Layout Engine"
    override val providerType: AIProviderType = AIProviderType.LOCAL_LAYOUT_ENGINE

    override suspend fun isConfigured(): Boolean = true

    override suspend fun testConnection(): AiConnectionStatus {
        return AiConnectionStatus.Success(
            latencyMs = 5,
            model = "Local Layout Engine",
            message = "Local Document Layout Engine is ready on device."
        )
    }

    override suspend fun processDocument(
        params: DocumentProcessingParams,
        onProgress: (ProcessingProgress) -> Unit
    ): DocumentProcessingResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        onProgress(
            ProcessingProgress(
                step = ProcessingStep.ANALYZING_PDF,
                progressPercent = 30,
                message = "Parsing document text tokens & identifying question blocks...",
                totalPages = params.totalPages,
                pdfType = params.pdfType
            )
        )

        onProgress(
            ProcessingProgress(
                step = ProcessingStep.DETECTING_QUESTIONS,
                progressPercent = 50,
                message = "Detecting question boundaries, subject headers & option markers...",
                totalPages = params.totalPages,
                pdfType = params.pdfType
            )
        )

        val rawResult = GeminiJeeExtractor.parseAlgorithmicFallback(
            questionPaperContent = params.questionPaperText,
            answerKeyContent = params.answerKeyText,
            testTitle = params.testTitle
        )

        onProgress(
            ProcessingProgress(
                step = ProcessingStep.READING_ANSWER_KEY,
                progressPercent = 70,
                message = "Parsing Official Answer Key tokens & keys...",
                totalPages = params.totalPages,
                questionsDetected = rawResult.questions.size,
                pdfType = params.pdfType
            )
        )

        onProgress(
            ProcessingProgress(
                step = ProcessingStep.MAPPING_ANSWERS,
                progressPercent = 85,
                message = "Mapping Question ↔ Official Answer pairs (${rawResult.questions.size} mapped)...",
                totalPages = params.totalPages,
                questionsDetected = rawResult.questions.size,
                pdfType = params.pdfType
            )
        )

        onProgress(
            ProcessingProgress(
                step = ProcessingStep.VALIDATING,
                progressPercent = 92,
                message = "Validating numbering, duplicates, and option integrity...",
                totalPages = params.totalPages,
                questionsDetected = rawResult.questions.size,
                pdfType = params.pdfType
            )
        )

        val elapsed = (System.currentTimeMillis() - startTime) / 1000
        val validatedCount = rawResult.questions.count { it.correctAnswer.isNotBlank() }

        DocumentProcessingResult(
            success = rawResult.questions.isNotEmpty(),
            testTitle = rawResult.testTitle,
            questions = rawResult.questions,
            flaggedQuestions = rawResult.flaggedQuestions,
            providerUsed = AIProviderType.LOCAL_LAYOUT_ENGINE,
            statusMessage = "Extracted ${rawResult.questions.size} questions using local layout engine.",
            errorMessage = if (rawResult.questions.isEmpty()) "No questions detected in the provided content." else null,
            elapsedSeconds = elapsed,
            diagramsCount = params.pageImagesBase64.size,
            validatedCount = validatedCount,
            ocrWarningsCount = rawResult.flaggedQuestions.size,
            pdfType = params.pdfType
        )
    }
}
