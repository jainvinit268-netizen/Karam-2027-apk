package com.example

import com.example.data.sample.QuizrrPartTest02Data
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

class Qpt2TestLinkTest {
    private fun q(s: String): String = buildString {
        append('"')
        s.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
        append('"')
    }

    @Test
    fun generateAndValidateQpt2KaramTestLink() {
        val questions = QuizrrPartTest02Data.getQuestions()
        assertEquals(75, questions.size)

        // Compact payload: only fields required by QuestionItem are included.
        // Optional QuestionItem fields use their Kotlin defaults when imported.
        val questionsJson = questions.joinToString(",", prefix = "[", postfix = "]") { item ->
            buildString {
                append("{\"id\":${q(item.id)},")
                append("\"questionNumber\":${item.questionNumber},")
                append("\"subject\":${q(item.subject.name)},")
                append("\"type\":${q(item.type.name)},")
                append("\"questionText\":${q(item.questionText)},")
                append("\"options\":[")
                append(item.options.joinToString(",") { q(it) })
                append("],")
                append("\"correctAnswer\":${q(item.correctAnswer)},")
                append("\"chapter\":${q("Q.P.T.-02")},")
                append("\"concept\":${q("Imported QPT-02")},")
                append("\"solutionText\":${q("Correct answer: ${item.correctAnswer}")}")
                append("}")
            }
        }

        val rootJson = buildString {
            append("{\"v\":1,")
            append("\"testId\":\"QPT2_LINK_20260815\",")
            append("\"title\":\"Q.P.T.-02\",")
            append("\"durationMinutes\":180,")
            append("\"questions\":")
            append(questionsJson)
            append("}")
        }

        // Android's Base64.URL_SAFE | NO_WRAP is equivalent to Java's URL encoder here.
        val token = Base64.getUrlEncoder().encodeToString(rootJson.toByteArray(StandardCharsets.UTF_8))
        val link = "karam://test/$token"

        assertTrue(link.startsWith("karam://test/"))
        assertTrue(token.isNotBlank())
        assertTrue(rootJson.contains("QPT2_MATH_01"))
        assertTrue(rootJson.contains("QPT2_CHEM_25"))

        val decodedJson = String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8)
        assertEquals(rootJson, decodedJson)

        File("/tmp/qpt2-test-link.txt").writeText(link)
        println("KARAM_TEST_LINK_START")
        println(link)
        println("KARAM_TEST_LINK_END")
        println("KARAM_TEST_LINK_LENGTH=${link.length}")
    }
}
