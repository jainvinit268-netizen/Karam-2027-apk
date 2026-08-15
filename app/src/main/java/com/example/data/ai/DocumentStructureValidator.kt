package com.example.data.ai

import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject

/**
 * Deterministic gate between extraction and CBT generation.
 * It never decides a subject from semantic content. It validates the structural
 * metadata supplied by the document extraction layer and blocks unsafe output.
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

        questions.zipWithNext().forEach { (a, b) ->
            if (a.questionNumber >= b.questionNumber) {
                warnings += "Question reading order is not strictly increasing at Q${a.questionNumber} → Q${b.questionNumber}"
            }
        }

        val invalidGeometry = questions.filter { q ->
            q.boundingRegions.isEmpty() || q.boundingRegions.any {
                it.pageIndex < 0 ||
                    it.width <= 0f || it.height <= 0f ||
                    it.x < 0f || it.y < 0f ||
                    it.x + it.width > 1.001f || it.y + it.height > 1.001f ||
                    it.width * it.height < 0.0025f
            }
        }.map { it.questionNumber }.distinct().sorted()
        invalidGeometry.forEach { warnings += "Q$it has missing/invalid/tiny original-PDF bounding geometry" }

        val lowConfidence = questions.filter {
            it.boundaryConfidence < 0.80f ||
                it.readingOrderConfidence < 0.80f ||
                it.subjectConfidence < 0.80f ||
                it.answerConfidence < 0.80f
        }.map { it.questionNumber }.distinct().sorted()
        lowConfidence.forEach { warnings += "Q$it has low extraction confidence" }

        questions.forEach { q ->
            if (q.questionText.length < 12) warnings += "Q${q.questionNumber} has incomplete/empty question text"
            if (q.sourcePages.isEmpty()) warnings += "Q${q.questionNumber} has no source page"
            if (q.type == QuestionType.MCQ && q.options.size < 2) warnings += "Q${q.questionNumber} is MCQ but has fewer than 2 options"
            if (q.type == QuestionType.NUMERICAL && q.options.isNotEmpty()) warnings += "Q${q.questionNumber} is Numerical but contains options"
            if (q.subject !in setOf(Subject.PHYSICS, Subject.CHEMISTRY, Subject.MATHEMATICS)) warnings += "Q${q.questionNumber} has an invalid subject"
            if (q.section.isBlank() || q.section.equals("Uncertain", ignoreCase = true)) warnings += "Q${q.questionNumber} has no verified section"
            if (q.extractionWarnings.any { it.contains("subject", ignoreCase = true) || it.contains("section", ignoreCase = true) }) {
                warnings += "Q${q.questionNumber} has unresolved subject/section evidence"
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
