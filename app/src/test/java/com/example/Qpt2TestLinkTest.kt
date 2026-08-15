package com.example

import com.example.data.local.JeeConverters
import com.example.data.sample.QuizrrPartTest02Data
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.Base64

class Qpt2TestLinkTest {
    @Test
    fun generateAndValidateQpt2KaramTestLink() {
        val questions = QuizrrPartTest02Data.getQuestions()
        assertEquals(75, questions.size)

        val questionsJson = JeeConverters().fromQuestionList(questions)
        val answerKeyJson = questions.joinToString(",", prefix = "[", postfix = "]") { q ->
            "\"${q.correctAnswer.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        }

        // Keep this JSON structure byte-for-byte compatible with TestLinkPayload.encode().
        val rootJson = buildString {
            append("{\"v\":1,")
            append("\"testId\":\"QPT2_LINK_20260815\",")
            append("\"title\":\"Q.P.T.-02\",")
            append("\"durationMinutes\":180,")
            append("\"questions\":")
            append(questionsJson)
            append(",\"answerKey\":")
            append(answerKeyJson)
            append("}")
        }

        // Android's Base64.URL_SAFE | NO_WRAP is equivalent to Java's URL encoder here.
        val token = Base64.getUrlEncoder().encodeToString(rootJson.toByteArray(StandardCharsets.UTF_8))
        val link = "karam://test/$token"

        assertTrue(link.startsWith("karam://test/"))
        assertTrue(token.isNotBlank())

        // Validate the same Base64 decode path used by TestLinkPayload.decode().
        val decodedJson = String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8)
        assertEquals(rootJson, decodedJson)
        assertTrue(decodedJson.contains("\"testId\":\"QPT2_LINK_20260815\""))
        assertTrue(decodedJson.contains("\"title\":\"Q.P.T.-02\""))
        assertTrue(decodedJson.contains("\"durationMinutes\":180"))
        assertTrue(decodedJson.contains("QPT2_MATH_01"))
        assertTrue(decodedJson.contains("QPT2_CHEM_25"))

        println("KARAM_TEST_LINK_START")
        println(link)
        println("KARAM_TEST_LINK_END")
    }
}
