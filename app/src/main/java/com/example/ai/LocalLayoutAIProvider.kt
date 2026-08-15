package com.example.ai

import com.example.data.ai.AiConnectionStatus
import com.example.data.ai.DocumentProcessingGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalLayoutAIProvider : AIProvider {
    override val name: String = "Local High-Precision Layout Engine"
    override val providerType: AIProviderType = AIProviderType.LOCAL_LAYOUT_ENGINE

    override suspend fun isConfigured(): Boolean = true

    override suspend fun testConnection(): AiConnectionStatus = AiConnectionStatus.Success(
        latencyMs = 5,
        model = "Local Layout Engine",
        message = "Local Document Layout Engine is ready on device."
    )

    override suspend fun processDocument(
        params: DocumentProcessingParams,
        onProgress: (ProcessingProgress) -> Unit
    ): DocumentProcessingResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        onProgress(ProcessingProgress(ProcessingStep.ANALYZING_PDF, 30, "Parsing document structure...", totalPages = params.totalPages, pdfType = params.pdfType))
        onProgress(ProcessingProgress(ProcessingStep.DETECTING_QUESTIONS, 50, "Detecting question boundaries...", totalPages = params.totalPages, pdfType = params.pdfType))

        val rawResult = GeminiJeeExtractor.parseAlgorithmicFallback(
            questionPaperContent = params.questionPaperText,
            answerKeyContent = params.answerKeyText,
            testTitle = params.testTitle
        )

        val elapsed = (System.currentTimeMillis() - startTime) / 1000
        val result = DocumentProcessingResult(
            success = rawResult.success,
            testTitle = rawResult.testTitle,
            questions = rawResult.questions,
            flaggedQuestions = rawResult.flaggedQuestions,
            providerUsed = AIProviderType.LOCAL_LAYOUT_ENGINE,
            statusMessage = "Local parser completed; running safety validation.",
            errorMessage = rawResult.errorMessage,
            elapsedSeconds = elapsed,
            diagramsCount = 0,
            validatedCount = 0,
            ocrWarningsCount = rawResult.flaggedQuestions.size,
            pdfType = params.pdfType
        )

        onProgress(ProcessingProgress(ProcessingStep.VALIDATING, 92, "Running deterministic document-structure safety gate...", totalPages = params.totalPages, questionsDetected = result.questions.size, pdfType = params.pdfType))
        DocumentProcessingGate.apply(result, params.targetQuestionCount)
    }
}
