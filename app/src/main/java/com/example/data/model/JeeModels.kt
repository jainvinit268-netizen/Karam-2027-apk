package com.example.data.model

import com.squareup.moshi.JsonClass

enum class Subject(val displayName: String, val shortCode: String) {
    PHYSICS("Physics", "PHY"),
    CHEMISTRY("Chemistry", "CHE"),
    MATHEMATICS("Mathematics", "MAT")
}

enum class QuestionType {
    MCQ,
    NUMERICAL
}

enum class QuestionStatus {
    NOT_VISITED,
    NOT_ANSWERED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED_FOR_REVIEW
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class MistakeType(val label: String, val description: String) {
    NONE("No Mistake", "Answered correctly"),
    SILLY_MISTAKE("Silly Mistake", "Misread question, calculation sign error or unit slip"),
    CONCEPTUAL_MISTAKE("Conceptual Mistake", "Formula misconception or theoretical flaw"),
    CALCULATION_MISTAKE("Calculation Mistake", "Algebraic or arithmetic error during final step"),
    WRONG_APPROACH("Wrong Approach", "Overcomplicated strategy or incorrect formula choice"),
    TIME_TRAP("Time Trap", "Spent excessive time and failed or left incomplete")
}

@JsonClass(generateAdapter = true)
data class QuestionItem(
    val id: String,
    val questionNumber: Int,
    val subject: Subject,
    val section: String = "Section A", // "Section A" (MCQ) or "Section B" (Numerical)
    val type: QuestionType,
    val questionText: String,
    val imageUrl: String? = null,
    val options: List<String> = emptyList(), // For MCQ: A, B, C, D
    val correctAnswer: String, // e.g. "B" or "45"
    val chapter: String,
    val concept: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val solutionText: String,
    val idealTimeSeconds: Int = 120,
    val youtubeSearchQuery: String = ""
)

@JsonClass(generateAdapter = true)
data class StudentResponse(
    val questionId: String,
    val selectedOption: String? = null,
    val numericalAnswer: String? = null,
    val status: QuestionStatus = QuestionStatus.NOT_VISITED,
    val timeSpentSeconds: Int = 0,
    val isCorrect: Boolean? = null,
    val marksAwarded: Int = 0,
    val mistakeCategory: MistakeType = MistakeType.NONE,
    val userNote: String? = null
)

@JsonClass(generateAdapter = true)
data class SubjectAnalysis(
    val subject: Subject,
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val score: Int,
    val maxScore: Int,
    val accuracy: Float,
    val totalTimeSeconds: Int,
    val avgTimePerQuestionSeconds: Int,
    val negativeMarksLost: Int
)

@JsonClass(generateAdapter = true)
data class ForensicReport(
    val totalScore: Int,
    val maxPossibleScore: Int,
    val estimatedPercentile: Float,
    val totalAttempted: Int,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val accuracyPercentage: Float,
    val totalNegativeMarksLost: Int,
    val subjectAnalyses: List<SubjectAnalysis>,
    val sillyMistakesCount: Int,
    val conceptualMistakesCount: Int,
    val calculationMistakesCount: Int,
    val wrongApproachCount: Int,
    val timeTrapsCount: Int,
    val shouldHaveAttemptedQuestions: List<Int>, // Question numbers that were easy but unattempted
    val shouldHaveSkippedQuestions: List<Int>, // Question numbers that wasted time and got wrong
    val topLeakChapters: List<String>,
    val revisionActionPlan: List<String>,
    val nextTestStrategy: String
)
