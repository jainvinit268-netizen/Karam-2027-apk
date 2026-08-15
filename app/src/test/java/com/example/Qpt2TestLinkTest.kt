package com.example

import com.example.data.local.JeeConverters
import com.example.data.model.TestLinkCodec
import com.example.data.model.TestLinkPayload
import com.example.data.sample.QuizrrPartTest02Data
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Qpt2TestLinkTest {
    @Test
    fun generateAndValidateQpt2KaramTestLink() {
        val questions = QuizrrPartTest02Data.getQuestions()
        assertEquals(75, questions.size)

        val questionsJson = JeeConverters().fromQuestionList(questions)
        val payload = TestLinkPayload(
            testId = "QPT2_LINK_20260815",
            title = "Q.P.T.-02",
            durationMinutes = 180,
            questionsJson = questionsJson,
            answerKeyJson = questions.joinToString(",") { it.correctAnswer }
        )

        val link = TestLinkCodec.build(payload)
        assertTrue(link.startsWith("karam://test/"))

        val token = android.net.Uri.parse(link).pathSegments.last()
        val decoded = TestLinkPayload.decode(token)
        assertEquals(75, decoded.questionsJson.let { org.json.JSONArray(it).length() })
        assertEquals("Q.P.T.-02", decoded.title)
        assertEquals(180, decoded.durationMinutes)

        println("KARAM_TEST_LINK_START")
        println(link)
        println("KARAM_TEST_LINK_END")
    }
}
