package com.example.data.repository

import android.content.Context
import com.example.data.local.AttemptWithUserResponses
import com.example.data.local.JeeAttemptEntity
import com.example.data.local.JeeConverters
import com.example.data.local.JeeDatabase
import com.example.data.local.JeeQuestionEntity
import com.example.data.local.JeeTestEntity
import com.example.data.local.JeeUserResponseEntity
import com.example.data.local.TestWithAttempts
import com.example.data.local.TestWithQuestions
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.model.Difficulty
import com.example.data.model.ForensicReport
import com.example.data.model.MistakeType
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.StudentResponse
import com.example.data.model.Subject
import com.example.data.model.SubjectAnalysis
import com.example.data.sample.SampleJeePapers
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class JeeRepository(context: Context) {
    private val database = JeeDatabase.getDatabase(context)
    private val dao = database.jeeDao()
    private val converters = JeeConverters()

    val allTests: Flow<List<JeeTestEntity>> = dao.getAllTests()
    val allAttempts: Flow<List<JeeAttemptEntity>> = dao.getAllAttempts()

    suspend fun getTestById(testId: String): JeeTestEntity? = dao.getTestById(testId)

    fun getTestFlowById(testId: String): Flow<JeeTestEntity?> = dao.getTestFlowById(testId)

    suspend fun getAttemptById(attemptId: String): JeeAttemptEntity? = dao.getAttemptById(attemptId)

    fun getAttemptsForTest(testId: String): Flow<List<JeeAttemptEntity>> = dao.getAttemptsForTest(testId)

    fun getTestWithQuestions(testId: String): Flow<TestWithQuestions?> = dao.getTestWithQuestions(testId)

    fun getAllTestsWithQuestions(): Flow<List<TestWithQuestions>> = dao.getAllTestsWithQuestions()

    fun getTestWithAttempts(testId: String): Flow<TestWithAttempts?> = dao.getTestWithAttempts(testId)

    fun getQuestionsForTest(testId: String): Flow<List<JeeQuestionEntity>> = dao.getQuestionsForTest(testId)

    fun getUserResponsesForAttempt(attemptId: String): Flow<List<JeeUserResponseEntity>> = dao.getUserResponsesForAttempt(attemptId)

    fun getAttemptWithUserResponses(attemptId: String): Flow<AttemptWithUserResponses?> = dao.getAttemptWithUserResponses(attemptId)

    suspend fun insertTest(test: JeeTestEntity, questions: List<QuestionItem> = emptyList()) {
        dao.insertTest(test)
        val questionsToInsert = if (questions.isNotEmpty()) {
            questions
        } else {
            parseQuestions(test.questionsJson)
        }
        if (questionsToInsert.isNotEmpty()) {
            val questionEntities = questionsToInsert.map { it.toEntity(test.testId, converters) }
            dao.insertQuestions(questionEntities)
        }
    }

    suspend fun deleteTest(testId: String) {
        dao.deleteQuestionsForTest(testId)
        dao.deleteTestById(testId)
    }

    suspend fun initializeDefaultTestsIfEmpty() {
        // Clean start: purge all legacy sample/demo tests from the database.
        // The test library starts completely empty for the user to upload real JEE papers.
        dao.deleteAllSampleTests()
    }

    fun parseQuestions(json: String): List<QuestionItem> {
        return converters.toQuestionList(json)
    }

    fun parseResponses(json: String): Map<String, StudentResponse> {
        return converters.toResponseMap(json)
    }

    fun parseForensicReport(json: String): ForensicReport? {
        return converters.toForensicReport(json)
    }

    suspend fun evaluateAndSaveAttempt(
        test: JeeTestEntity,
        questions: List<QuestionItem>,
        rawResponses: Map<String, StudentResponse>,
        durationSeconds: Long
    ): Pair<JeeAttemptEntity, ForensicReport> {
        val evaluatedResponses = mutableMapOf<String, StudentResponse>()
        var totalScore = 0
        var totalAttempted = 0
        var correctCount = 0
        var incorrectCount = 0
        var negativeMarksLost = 0

        // Per subject tracking
        val subjectStats = mutableMapOf<Subject, SubjectAccumulator>()
        Subject.values().forEach {
            subjectStats[it] = SubjectAccumulator()
        }

        var sillyCount = 0
        var conceptualCount = 0
        var calculationCount = 0
        var wrongApproachCount = 0
        var timeTrapsCount = 0

        val shouldHaveAttempted = mutableListOf<Int>()
        val shouldHaveSkipped = mutableListOf<Int>()
        val chapterLeakScores = mutableMapOf<String, Int>()

        for (q in questions) {
            val userResp = rawResponses[q.id] ?: StudentResponse(questionId = q.id, status = QuestionStatus.NOT_VISITED)
            val subAcc = subjectStats[q.subject] ?: SubjectAccumulator()
            subAcc.totalQuestions++

            val isEvaluatedStatus = userResp.status == QuestionStatus.ANSWERED ||
                    userResp.status == QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW

            val givenAns = if (q.type == QuestionType.MCQ) userResp.selectedOption else userResp.numericalAnswer

            var marks = 0
            var isCorrect: Boolean? = null
            var mistakeType = MistakeType.NONE

            if (isEvaluatedStatus && !givenAns.isNullOrBlank()) {
                totalAttempted++
                subAcc.attemptedCount++
                subAcc.totalTimeSeconds += userResp.timeSpentSeconds

                val isMatch = if (q.type == QuestionType.MCQ) {
                    givenAns.trim().equals(q.correctAnswer.trim(), ignoreCase = true)
                } else {
                    // Numerical match allowing decimal rounding
                    val givenNum = givenAns.trim().toDoubleOrNull()
                    val correctNum = q.correctAnswer.trim().toDoubleOrNull()
                    if (givenNum != null && correctNum != null) {
                        Math.abs(givenNum - correctNum) < 0.05
                    } else {
                        givenAns.trim().equals(q.correctAnswer.trim(), ignoreCase = true)
                    }
                }

                if (isMatch) {
                    marks = 4
                    isCorrect = true
                    correctCount++
                    subAcc.correctCount++
                    subAcc.score += 4
                } else {
                    marks = -1
                    isCorrect = false
                    incorrectCount++
                    subAcc.incorrectCount++
                    subAcc.score -= 1
                    negativeMarksLost += 1
                    subAcc.negativeMarksLost += 1

                    // Categorize mistake intelligently based on time spent & difficulty
                    mistakeType = when {
                        userResp.timeSpentSeconds < 45 -> MistakeType.SILLY_MISTAKE
                        userResp.timeSpentSeconds > 180 -> MistakeType.TIME_TRAP
                        q.difficulty == Difficulty.HARD -> MistakeType.WRONG_APPROACH
                        q.type == QuestionType.NUMERICAL -> MistakeType.CALCULATION_MISTAKE
                        else -> MistakeType.CONCEPTUAL_MISTAKE
                    }

                    when (mistakeType) {
                        MistakeType.SILLY_MISTAKE -> sillyCount++
                        MistakeType.CONCEPTUAL_MISTAKE -> conceptualCount++
                        MistakeType.CALCULATION_MISTAKE -> calculationCount++
                        MistakeType.WRONG_APPROACH -> wrongApproachCount++
                        MistakeType.TIME_TRAP -> timeTrapsCount++
                        else -> {}
                    }

                    // Chapter leak tracking (5 marks lost: +4 missed and -1 deducted)
                    chapterLeakScores[q.chapter] = (chapterLeakScores[q.chapter] ?: 0) + 5

                    if (q.difficulty == Difficulty.HARD || userResp.timeSpentSeconds > 180) {
                        shouldHaveSkipped.add(q.questionNumber)
                    }
                }
            } else {
                subAcc.unattemptedCount++
                // Unattempted: Check if it was an Easy or Medium question that should have been attempted
                if (q.difficulty == Difficulty.EASY || (q.difficulty == Difficulty.MEDIUM && userResp.timeSpentSeconds < 20)) {
                    shouldHaveAttempted.add(q.questionNumber)
                }
            }

            totalScore += marks
            evaluatedResponses[q.id] = userResp.copy(
                isCorrect = isCorrect,
                marksAwarded = marks,
                mistakeCategory = mistakeType
            )
        }

        val subjectAnalyses = Subject.values().map { subj ->
            val acc = subjectStats[subj] ?: SubjectAccumulator()
            val accuracy = if (acc.attemptedCount > 0) (acc.correctCount.toFloat() / acc.attemptedCount.toFloat()) * 100f else 0f
            val avgTime = if (acc.attemptedCount > 0) acc.totalTimeSeconds / acc.attemptedCount else 0
            SubjectAnalysis(
                subject = subj,
                totalQuestions = acc.totalQuestions,
                attemptedCount = acc.attemptedCount,
                correctCount = acc.correctCount,
                incorrectCount = acc.incorrectCount,
                unattemptedCount = acc.unattemptedCount,
                score = acc.score,
                maxScore = acc.totalQuestions * 4,
                accuracy = accuracy,
                totalTimeSeconds = acc.totalTimeSeconds,
                avgTimePerQuestionSeconds = avgTime,
                negativeMarksLost = acc.negativeMarksLost
            )
        }

        val accuracyPercentage = if (totalAttempted > 0) (correctCount.toFloat() / totalAttempted.toFloat()) * 100f else 0f
        val maxPossible = questions.size * 4

        // Estimate JEE percentile dynamically based on standard score benchmarks
        val estimatedPercentile = when {
            totalScore >= 260 -> 99.95f
            totalScore >= 230 -> 99.50f
            totalScore >= 200 -> 99.00f
            totalScore >= 170 -> 98.00f
            totalScore >= 140 -> 96.00f
            totalScore >= 110 -> 92.50f
            totalScore >= 80 -> 85.00f
            totalScore >= 50 -> 70.00f
            else -> Math.max(10.0f, (totalScore.toFloat() / maxPossible) * 100f)
        }

        val topLeaks = chapterLeakScores.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { "${it.key} (-${it.value} marks)" }

        val actionPlan = mutableListOf<String>()
        if (negativeMarksLost > 5) {
            actionPlan.add("Reduce aggressive guesswork: you lost $negativeMarksLost marks strictly on negative marking.")
        }
        if (sillyCount > 0) {
            actionPlan.add("Cross-check units and sign conventions on rapid solves ($sillyCount silly mistakes detected).")
        }
        if (shouldHaveAttempted.isNotEmpty()) {
            actionPlan.add("Scan all 3 subjects during round 1: Q${shouldHaveAttempted.take(3).joinToString(", ")} were easy scoring opportunities left untouched.")
        }
        if (topLeaks.isNotEmpty()) {
            actionPlan.add("Prioritize revising: ${topLeaks.joinToString(", ")}.")
        }

        val nextTestStrategy = when {
            accuracyPercentage < 70 -> "Focus strictly on selection accuracy: attempt only 100% verified concepts and leave doubtful numerical questions to protect marks."
            totalAttempted < 40 -> "Increase speed in Chemistry to free up 15-20 minutes for high-scoring Physics and Mathematics problems."
            else -> "Maintain high attempt rhythm while avoiding time-traps (>3 mins on a single question). Execute a 2-round strategy."
        }

        val forensicReport = ForensicReport(
            totalScore = totalScore,
            maxPossibleScore = maxPossible,
            estimatedPercentile = estimatedPercentile,
            totalAttempted = totalAttempted,
            totalQuestions = questions.size,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            unattemptedCount = questions.size - totalAttempted,
            accuracyPercentage = accuracyPercentage,
            totalNegativeMarksLost = negativeMarksLost,
            subjectAnalyses = subjectAnalyses,
            sillyMistakesCount = sillyCount,
            conceptualMistakesCount = conceptualCount,
            calculationMistakesCount = calculationCount,
            wrongApproachCount = wrongApproachCount,
            timeTrapsCount = timeTrapsCount,
            shouldHaveAttemptedQuestions = shouldHaveAttempted,
            shouldHaveSkippedQuestions = shouldHaveSkipped,
            topLeakChapters = topLeaks,
            revisionActionPlan = actionPlan,
            nextTestStrategy = nextTestStrategy
        )

        val attemptId = UUID.randomUUID().toString()
        val attemptEntity = JeeAttemptEntity(
            attemptId = attemptId,
            testId = test.testId,
            testTitle = test.title,
            attemptTimestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            totalScore = totalScore,
            physicsScore = subjectStats[Subject.PHYSICS]?.score ?: 0,
            chemistryScore = subjectStats[Subject.CHEMISTRY]?.score ?: 0,
            mathsScore = subjectStats[Subject.MATHEMATICS]?.score ?: 0,
            totalAttempted = totalAttempted,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            accuracy = accuracyPercentage,
            responsesJson = converters.fromResponseMap(evaluatedResponses),
            forensicReportJson = converters.fromForensicReport(forensicReport)
        )

        dao.insertAttempt(attemptEntity)

        // Store granular user response entities
        val userResponseEntities = evaluatedResponses.values.map { resp ->
            val correspondingQ = questions.find { it.id == resp.questionId }
            resp.toEntity(
                attemptId = attemptId,
                testId = test.testId,
                qNum = correspondingQ?.questionNumber ?: 0,
                subj = correspondingQ?.subject ?: Subject.PHYSICS
            )
        }
        if (userResponseEntities.isNotEmpty()) {
            dao.insertUserResponses(userResponseEntities)
        }

        // Update test completion & best score
        val newBest = maxOf(test.bestScore ?: totalScore, totalScore)
        dao.updateTest(
            test.copy(
                isCompleted = true,
                bestScore = newBest,
                lastAttemptDate = System.currentTimeMillis()
            )
        )

        return Pair(attemptEntity, forensicReport)
    }

    private class SubjectAccumulator {
        var totalQuestions = 0
        var attemptedCount = 0
        var correctCount = 0
        var incorrectCount = 0
        var unattemptedCount = 0
        var score = 0
        var totalTimeSeconds = 0
        var negativeMarksLost = 0
    }
}

