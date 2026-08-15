package com.example.ai

import android.content.Context
import com.example.data.ai.AiConnectionStatus
import com.example.data.ai.AiKeyManager
import com.example.data.ai.DocumentProcessingGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAIProvider(
    private val context: Context,
    private val aiKeyManager: AiKeyManager = AiKeyManager.getInstance(context)
) : AIProvider {

    override val name: String = "Gemini API (Google AI Studio)"
    override val providerType: AIProviderType = AIProviderType.GEMINI_API

    override suspend fun isConfigured(): Boolean = !aiKeyManager.getActiveApiKey().isNullOrBlank()

    override suspend fun testConnection(): AiConnectionStatus = aiKeyManager.testConnection()

    override suspend fun processDocument(
        params: DocumentProcessingParams,
        onProgress: (ProcessingProgress) -> Unit
    ): DocumentProcessingResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        if (!isConfigured()) {
            return@withContext DocumentProcessingResult(
                success = false,
                testTitle = params.testTitle,
                questions = emptyList(),
                providerUsed = AIProviderType.GEMINI_API,
                errorMessage = "Verified PDF extraction requires a configured Gemini API key."
            )
        }

        val raw = StructuredJeeDocumentExtractor.extract(
            context = context,
            questionText = params.questionPaperText,
            answerKeyText = params.answerKeyText,
            testTitle = params.testTitle,
            pageImagesBase64 = params.pageImagesBase64,
            totalPages = params.totalPages,
            onProgress = onProgress
        )

        val elapsed = (System.currentTimeMillis() - startTime) / 1000
        val result = DocumentProcessingResult(
            success = raw.success,
            testTitle = raw.testTitle,
            questions = raw.questions,
            flaggedQuestions = raw.flaggedQuestions,
            providerUsed = AIProviderType.GEMINI_API,
            statusMessage = raw.statusMessage,
            errorMessage = raw.errorMessage,
            elapsedSeconds = elapsed,
            diagramsCount = raw.questions.count { it.boundingRegions.isNotEmpty() },
            validatedCount = raw.questions.count { it.questionText.length >= 12 && it.boundingRegions.isNotEmpty() && it.correctAnswer.isNotBlank() },
            ocrWarningsCount = raw.flaggedQuestions.size,
            pdfType = params.pdfType
        )

        onProgress(
            ProcessingProgress(
                step = ProcessingStep.VALIDATING,
                progressPercent = 94,
                message = "Running deterministic document-structure safety gate...",
                totalPages = params.totalPages,
                questionsDetected = result.questions.size,
                pdfType = params.pdfType
            )
        )

        DocumentProcessingGate.apply(result, params.targetQuestionCount)
    }
}
