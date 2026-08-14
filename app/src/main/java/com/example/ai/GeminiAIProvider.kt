package com.example.ai

import android.content.Context
import com.example.data.ai.AiConnectionStatus
import com.example.data.ai.AiKeyManager
import com.example.data.ai.AiKeySource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAIProvider(
    private val context: Context,
    private val aiKeyManager: AiKeyManager = AiKeyManager.getInstance(context)
) : AIProvider {

    override val name: String = "Gemini API (Google AI Studio)"
    override val providerType: AIProviderType = AIProviderType.GEMINI_API

    override suspend fun isConfigured(): Boolean {
        return !aiKeyManager.getActiveApiKey().isNullOrBlank()
    }

    override suspend fun testConnection(): AiConnectionStatus {
        return aiKeyManager.testConnection()
    }

    override suspend fun processDocument(
        params: DocumentProcessingParams,
        onProgress: (ProcessingProgress) -> Unit
    ): DocumentProcessingResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val apiKey = aiKeyManager.getActiveApiKey()

        if (apiKey.isNullOrBlank()) {
            onProgress(
                ProcessingProgress(
                    step = ProcessingStep.ANALYZING_PDF,
                    progressPercent = 30,
                    message = "No Gemini API Key detected. Using high-precision local layout parser...",
                    totalPages = params.totalPages
                )
            )
            val fallbackProvider = LocalLayoutAIProvider()
            return@withContext fallbackProvider.processDocument(params, onProgress)
        }

        // Delegate to enhanced GeminiJeeExtractor with live progress callbacks
        val result = GeminiJeeExtractor.extractJeePaperWithProgress(
            context = context,
            questionPaperContent = params.questionPaperText,
            answerKeyContent = params.answerKeyText,
            testTitle = params.testTitle,
            pageImagesBase64 = params.pageImagesBase64,
            totalPages = params.totalPages,
            targetQuestionCount = params.targetQuestionCount,
            onProgress = onProgress
        )

        val elapsed = (System.currentTimeMillis() - startTime) / 1000
        val validatedCount = result.questions.count { it.correctAnswer.isNotBlank() && it.questionText.length > 10 }

        DocumentProcessingResult(
            success = result.success,
            testTitle = result.testTitle,
            questions = result.questions,
            flaggedQuestions = result.flaggedQuestions,
            providerUsed = if (result.aiUsed) AIProviderType.GEMINI_API else AIProviderType.LOCAL_LAYOUT_ENGINE,
            statusMessage = result.statusMessage,
            errorMessage = result.errorMessage,
            elapsedSeconds = elapsed,
            diagramsCount = params.pageImagesBase64.size,
            validatedCount = validatedCount,
            ocrWarningsCount = result.flaggedQuestions.size
        )
    }
}
