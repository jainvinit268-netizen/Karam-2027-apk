package com.example.data.ai

import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject

/**
 * Deterministic gate between extraction and CBT generation.
 *
 * It deliberately does not infer a subject from question semantics. It only
 * validates the structural metadata supplied by the document extraction layer.
 * A failing gate must prevent a confidently wrong CBT from being generated.
 */
object DocumentStructureValidator {
    data class Report(
        val valid: Boolean,
        val warnings: List<String>,
        val missingQuestionNumbers: List<Int>,
        val duplicateQuestionNumbers: List<Int>,
        val invalidGeometryQuestions: List<Int>,
        val lowConfidenceQuestions: List<Int>
    )

    fun validate(
        questions: List<QuestionItem>,
        expectedQuestionCount: Int? = null
    ): Report {
        val warnings = mutableListOf<String>()
        val duplicateNumbers = questions
            .groupingBy { it.questionNumber }
            .eachCount()
            .filterValues { it > 1 }
            .keys
            .sorted()

        duplicateNumbers.forEach { warnings += "Duplicate question number: Q$it" }

        val ordered = questions.map { it.questionNumber }.distinct().sorted()
        val missing = if (expectedQuestionCount != null && expectedQuestionCount > 0) {
            (1..expectedQuestionCount).filter { it !in ordered }
        } else emptyList()
        missing.forEach { warnings += "Missing question number: Q$it" }

        val invalidGeometry = questions.filter { q ->
            q.boundingRegions.isEmpty() || q.boundingRegions.any {
                it.pageIndex < 0 ||
                    it.width <= 0f || it.height <= 0f ||
                    it.x < 0f || it.y < 0f ||
                    it.x + it.width > 1.001f || it.y + it.height > 1.001f
            }
        }.map { it.questionNumber }.distinct().sorted()
        invalidGeometry.forEach { warnings += "Q$it has no valid original-PDF bounding region" }

        val lowConfidence = questions.filter {
            it.boundaryConfidence < 0.80f ||
                it.readingOrderConfidence < 0.80f ||
                it.subjectConfidence < 0.80f ||
                it.answerConfidence < 0.80f
        }.map { it.questionNumber }.distinct().sorted()
        lowConfidence.forEach { warnings += "Q$it has low extraction confidence" }

        questions.forEach { q ->
            if (q.type == QuestionType.MCQ && q.options.size < 2) {
                warnings += "Q${q.questionNumber} is marked MCQ but has fewer than 2 options"
            }
            if (q.type == QuestionType.NUMERICAL && q.options.isNotEmpty()) {
                warnings += "Q${q.questionNumber} is Numerical but contains options"
            }
            if (q.subject !in setOf(Subject.PHYSICS, Subject.CHEMISTRY, Subject.MATHEMATICS)) {
                warnings += "Q${q.questionNumber} has an invalid subject"
            }
        }

        return Report(
            valid = warnings.isEmpty(),
            warnings = warnings.distinct(),
            missingQuestionNumbers = missing,
            duplicateQuestionNumbers = duplicateNumbers,
            invalidGeometryQuestions = invalidGeometry,
            lowConfidenceQuestions = lowConfidence
        )
    }
}
