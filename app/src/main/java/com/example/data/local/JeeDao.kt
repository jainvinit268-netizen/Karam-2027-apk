package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface JeeDao {
    // ---------------- Tests ----------------
    @Query("SELECT * FROM jee_tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<JeeTestEntity>>

    @Query("SELECT * FROM jee_tests WHERE testId = :testId LIMIT 1")
    suspend fun getTestById(testId: String): JeeTestEntity?

    @Query("SELECT * FROM jee_tests WHERE testId = :testId LIMIT 1")
    fun getTestFlowById(testId: String): Flow<JeeTestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: JeeTestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<JeeTestEntity>)

    @Update
    suspend fun updateTest(test: JeeTestEntity)

    @Query("DELETE FROM jee_tests WHERE testId = :testId")
    suspend fun deleteTestById(testId: String)

    @Query("DELETE FROM jee_tests WHERE isSample = 1 OR testId IN ('quizrr_qpt_02', 'jee_2025_jan_shift1', 'jee_2024_apr_shift2')")
    suspend fun deleteAllSampleTests()

    @Query("SELECT COUNT(*) FROM jee_tests")
    suspend fun getTestCount(): Int

    // ---------------- Questions ----------------
    @Query("SELECT * FROM jee_questions WHERE testId = :testId ORDER BY questionNumber ASC")
    fun getQuestionsForTest(testId: String): Flow<List<JeeQuestionEntity>>

    @Query("SELECT * FROM jee_questions WHERE testId = :testId ORDER BY questionNumber ASC")
    suspend fun getQuestionsForTestSync(testId: String): List<JeeQuestionEntity>

    @Query("SELECT * FROM jee_questions WHERE testId = :testId AND subject = :subject ORDER BY questionNumber ASC")
    fun getQuestionsForTestAndSubject(testId: String, subject: Subject): Flow<List<JeeQuestionEntity>>

    @Query("SELECT * FROM jee_questions WHERE questionEntityId = :questionEntityId LIMIT 1")
    suspend fun getQuestionById(questionEntityId: String): JeeQuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<JeeQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: JeeQuestionEntity)

    @Query("DELETE FROM jee_questions WHERE testId = :testId")
    suspend fun deleteQuestionsForTest(testId: String)

    // ---------------- Relational Queries ----------------
    @Transaction
    @Query("SELECT * FROM jee_tests WHERE testId = :testId LIMIT 1")
    fun getTestWithQuestions(testId: String): Flow<TestWithQuestions?>

    @Transaction
    @Query("SELECT * FROM jee_tests ORDER BY createdAt DESC")
    fun getAllTestsWithQuestions(): Flow<List<TestWithQuestions>>

    @Transaction
    @Query("SELECT * FROM jee_tests WHERE testId = :testId LIMIT 1")
    fun getTestWithAttempts(testId: String): Flow<TestWithAttempts?>

    // ---------------- Attempts ----------------
    @Query("SELECT * FROM jee_attempts ORDER BY attemptTimestamp DESC")
    fun getAllAttempts(): Flow<List<JeeAttemptEntity>>

    @Query("SELECT * FROM jee_attempts WHERE testId = :testId ORDER BY attemptTimestamp DESC")
    fun getAttemptsForTest(testId: String): Flow<List<JeeAttemptEntity>>

    @Query("SELECT * FROM jee_attempts WHERE attemptId = :attemptId LIMIT 1")
    suspend fun getAttemptById(attemptId: String): JeeAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: JeeAttemptEntity)

    @Query("DELETE FROM jee_attempts WHERE attemptId = :attemptId")
    suspend fun deleteAttemptById(attemptId: String)

    // ---------------- User Responses ----------------
    @Query("SELECT * FROM jee_user_responses WHERE attemptId = :attemptId ORDER BY questionNumber ASC")
    fun getUserResponsesForAttempt(attemptId: String): Flow<List<JeeUserResponseEntity>>

    @Query("SELECT * FROM jee_user_responses WHERE attemptId = :attemptId ORDER BY questionNumber ASC")
    suspend fun getUserResponsesForAttemptSync(attemptId: String): List<JeeUserResponseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserResponses(responses: List<JeeUserResponseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserResponse(response: JeeUserResponseEntity)

    @Transaction
    @Query("SELECT * FROM jee_attempts WHERE attemptId = :attemptId LIMIT 1")
    fun getAttemptWithUserResponses(attemptId: String): Flow<AttemptWithUserResponses?>
}

