package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiJeeExtractor
import com.example.data.ai.AiKeyManager
import com.example.data.ai.AiKeySource
import com.example.data.auth.AuthState
import com.example.data.auth.GoogleAuthManager
import com.example.data.auth.UserProfile
import com.example.data.local.JeeAttemptEntity
import com.example.data.local.JeeConverters
import com.example.data.local.JeeDatabase
import com.example.data.local.JeeTestEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.model.Difficulty
import com.example.data.model.MistakeType
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.StudentResponse
import com.example.data.model.Subject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: JeeDatabase
    private val converters = JeeConverters()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JeeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JEE CBT Prep", appName)
    }

    @Test
    fun `room schema persists independent test, questions and user responses`() = runBlocking {
        val testId = "test_jee_2025_01"
        val test = JeeTestEntity(
            testId = testId,
            title = "JEE Main 2025 Jan 24 Shift 1",
            sourcePdfName = "jee_2025.pdf",
            totalQuestions = 1,
            durationMinutes = 180
        )

        val question = QuestionItem(
            id = "q_phy_1",
            questionNumber = 1,
            subject = Subject.PHYSICS,
            section = "Section A",
            type = QuestionType.MCQ,
            questionText = "A body of mass 2 kg moves with velocity 3 m/s. Find kinetic energy.",
            options = listOf("6 J", "9 J", "18 J", "3 J"),
            correctAnswer = "B",
            chapter = "Work Energy and Power",
            concept = "Kinetic Energy",
            difficulty = Difficulty.EASY,
            solutionText = "KE = 0.5 * m * v^2 = 0.5 * 2 * 9 = 9 J",
            idealTimeSeconds = 60
        )

        // Insert Test as independent entity
        db.jeeDao().insertTest(test)
        val loadedTest = db.jeeDao().getTestById(testId)
        assertNotNull(loadedTest)
        assertEquals("JEE Main 2025 Jan 24 Shift 1", loadedTest?.title)

        // Insert Question linked to test
        val questionEntity = question.toEntity(testId, converters)
        db.jeeDao().insertQuestion(questionEntity)

        val loadedQuestions = db.jeeDao().getQuestionsForTestSync(testId)
        assertEquals(1, loadedQuestions.size)
        assertEquals("Kinetic Energy", loadedQuestions[0].concept)
        assertEquals("B", loadedQuestions[0].correctAnswer)

        // Verify domain mapping
        val domainQuestion = loadedQuestions[0].toDomain(converters)
        assertEquals(question.questionText, domainQuestion.questionText)
        assertEquals(4, domainQuestion.options.size)

        // Verify relational query
        val testWithQuestions = db.jeeDao().getTestWithQuestions(testId).first()
        assertNotNull(testWithQuestions)
        assertEquals(1, testWithQuestions?.questions?.size)

        // Insert User Response linked to test and attempt (insert parent attempt first for foreign key integrity)
        val attemptId = "attempt_101"
        val attempt = JeeAttemptEntity(
            attemptId = attemptId,
            testId = testId,
            testTitle = test.title,
            responsesJson = "{}",
            forensicReportJson = "{}"
        )
        db.jeeDao().insertAttempt(attempt)

        val response = StudentResponse(
            questionId = question.id,
            selectedOption = "B",
            status = QuestionStatus.ANSWERED,
            timeSpentSeconds = 45,
            isCorrect = true,
            marksAwarded = 4,
            mistakeCategory = MistakeType.NONE
        )

        val responseEntity = response.toEntity(attemptId, testId, question.questionNumber, question.subject)
        db.jeeDao().insertUserResponse(responseEntity)

        val loadedResponses = db.jeeDao().getUserResponsesForAttemptSync(attemptId)
        assertEquals(1, loadedResponses.size)
        assertEquals("B", loadedResponses[0].selectedOption)
        assertEquals(true, loadedResponses[0].isCorrect)
        assertEquals(4, loadedResponses[0].marksAwarded)
    }

    @Test
    fun `ai key manager saves, clears and masks custom api keys securely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val aiKeyManager = AiKeyManager.getInstance(context)

        // Clear initially
        aiKeyManager.clearUserApiKey()
        assertEquals(null, aiKeyManager.getUserCustomKey())

        // Save custom key
        aiKeyManager.saveUserApiKey("AIzaSyDummyKeyForTesting12345678")
        assertEquals("AIzaSyDummyKeyForTesting12345678", aiKeyManager.getUserCustomKey())
        assertEquals(AiKeySource.USER_KEY, aiKeyManager.getActiveKeySource())
        assertTrue(aiKeyManager.configState.value.isConfigured)
        assertTrue(aiKeyManager.configState.value.maskedKey.contains("••••"))

        // Clear custom key
        aiKeyManager.clearUserApiKey()
        assertEquals(null, aiKeyManager.getUserCustomKey())
    }

    @Test
    fun `gemini jee extractor processes paper with answer key alignment safely`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val qPaper = """
            [PHYSICS]
            Q1. What is the unit of force? (A) Newton (B) Joule (C) Watt (D) Pascal
            Q21. Find velocity if acceleration is 2 and time is 5.
        """.trimIndent()
        val ansKey = "Q1: A\nQ21: 10"

        val result = GeminiJeeExtractor.extractJeePaper(
            context = context,
            questionPaperContent = qPaper,
            answerKeyContent = ansKey,
            testTitle = "Mock Test"
        )

        assertTrue(result.questions.isNotEmpty())
        assertEquals("A", result.questions.firstOrNull { it.questionNumber == 1 }?.correctAnswer)
    }

    @Test
    fun `google auth manager handles sign out and state cleanly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authManager = GoogleAuthManager.getInstance(context)

        val signOutResult = authManager.signOut()
        assertTrue(signOutResult.isSuccess)
        assertEquals(AuthState.Unauthenticated, authManager.authState.value)
        assertEquals(null, authManager.currentUser.value)
    }
}
