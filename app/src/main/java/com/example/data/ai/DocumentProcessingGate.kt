package com.example.data.ai

import com.example.ai.AIProviderType
import com.example.ai.DocumentProcessingResult
import com.example.data.model.QuestionItem

/**
 * Final deterministic safety gate before a QuestionItem list can reach the CBT DB.
 * A structurally invalid extraction is an error, not a partial success.
 */
object DocumentProcessingGate {
    fun apply(
        result: DocumentProcessingResult,
        expectedQuestionCount: Int? = null
    ): DocumentProcessingResult {
        if (!result.success || result.questions.isEmpty()) return result

        val report = DocumentStructureValidator.validate(result.questions, expectedQuestionCount)
        if (report.valid) return result

        val focused = report.warnings.take(12).joinToString("\n")
        return result.copy(
            success = false,
            questions = emptyList(),
            flaggedQuestions = report.lowConfidenceQuestions,
            statusMessage = "Extraction stopped by document validation gate.",
            errorMessage = "The source could not be proven structurally safe. No CBT was generated.\n$focused",
            validatedCount = 0,
            ocrWarningsCount = report.warnings.size
        )
    }

    fun validateQuestions(questions: List<QuestionItem>, expectedQuestionCount: Int? = null) =
        DocumentStructureValidator.validate(questions, expectedQuestionCount)
}
