package com.example

import android.content.Context
import android.net.Uri
import com.example.data.local.JeeTestEntity
import com.example.data.repository.JeeRepository
import org.json.JSONArray
import org.json.JSONObject

/**
 * Imports a JEE test from TWO independent JSON files:
 * 1) question JSON
 * 2) answer-key JSON
 *
 * The two files are mapped locally by questionId/questionNumber/order and the
 * resulting canonical QuestionItem list is saved directly to the KARAM Test Library.
 */
object PairedJsonTestImporter {
    suspend fun import(context: Context, questionUri: Uri, answerKeyUri: Uri): String {
        val questionRoot = JSONObject(readText(context, questionUri).trim())
        val answerRoot = JSONObject(readText(context, answerKeyUri).trim())

        val sourceQuestions = extractQuestions(questionRoot)
        require(sourceQuestions.length() > 0) { "Question JSON contains no questions." }

        val answers = extractAnswers(answerRoot)
        val canonical = JSONArray()

        for (i in 0 until sourceQuestions.length()) {
            val q = sourceQuestions.getJSONObject(i)
            val number = q.optInt("questionNumber", i + 1)
            val id = firstNonBlank(q.optString("questionId"), q.optString("id"), "Q$number")
            val answer = findAnswer(answers, id, number, i)
                ?: throw IllegalArgumentException("Answer key is missing an answer for question $number ($id).")

            canonical.put(toCanonicalQuestion(q, id, number, answer))
        }

        val repository = JeeRepository(context)
        val questions = repository.parseQuestions(canonical.toString())
        require(questions.isNotEmpty()) { "Question JSON could not be converted into JEE questions." }

        val testId = firstNonBlank(
            questionRoot.optString("testId"),
            "KARAM_${System.currentTimeMillis()}"
        )
        val title = firstNonBlank(questionRoot.optString("title"), "Imported JEE CBT Test")
        val duration = questionRoot.optInt("durationMinutes", 180).coerceIn(1, 600)

        val test = JeeTestEntity(
            testId = testId,
            title = title,
            sourcePdfName = "JSON_QUESTIONS_+_ANSWER_KEY",
            totalQuestions = questions.size,
            durationMinutes = duration,
            createdAt = System.currentTimeMillis(),
            questionsJson = canonical.toString(),
            isSample = false,
            physicsQuestionsCount = questions.count { it.subject.name == "PHYSICS" },
            chemistryQuestionsCount = questions.count { it.subject.name == "CHEMISTRY" },
            mathsQuestionsCount = questions.count { it.subject.name == "MATHEMATICS" },
            tags = "JSON Questions + Answer Key"
        )

        repository.insertTest(test, questions)
        return test.testId
    }

    private fun toCanonicalQuestion(q: JSONObject, id: String, number: Int, answer: String): JSONObject {
        val typeRaw = firstNonBlank(q.optString("questionType"), q.optString("type"), "MCQ")
        val type = if (typeRaw.equals("Numerical", true) || typeRaw.equals("NUMERICAL", true) || typeRaw.equals("integer", true)) "NUMERICAL" else "MCQ"

        val options = q.optJSONArray("options") ?: JSONArray()
        val solution = q.optJSONObject("solution")
        val solutionText = firstNonBlank(
            q.optString("solutionText"),
            solution?.optString("bestSolution") ?: "",
            "Solution not provided in source JSON."
        )

        val out = JSONObject()
        out.put("id", id)
        out.put("questionNumber", number)
        out.put("subject", normalizeSubject(firstNonBlank(q.optString("subject"), "Physics")))
        out.put("section", firstNonBlank(q.optString("section"), "Section A"))
        out.put("type", type)
        out.put("questionText", firstNonBlank(q.optString("questionText"), q.optString("question"), ""))
        out.put("imageUrl", JSONObject.NULL)
        out.put("options", if (type == "MCQ") options else JSONArray())
        out.put("correctAnswer", answer)
        out.put("chapter", firstNonBlank(q.optString("chapter"), "Unclassified"))
        out.put("concept", firstNonBlank(q.optString("concept"), q.optString("subtopic"), "Unclassified"))
        out.put("difficulty", normalizeDifficulty(firstNonBlank(q.optString("difficulty"), "MEDIUM")))
        out.put("solutionText", solutionText)
        out.put("idealTimeSeconds", q.optInt("estimatedTime", q.optInt("idealTimeSeconds", 120)).coerceIn(5, 1800))
        out.put("youtubeSearchQuery", firstNonBlank(q.optString("youtubeSearchQuery"), q.optString("youtubeQuery"), "JEE ${normalizeSubject(q.optString("subject"))} solution question $number"))
        return out
    }

    private fun extractQuestions(root: JSONObject): JSONArray {
        root.optJSONArray("questions")?.let { return it }
        root.optJSONArray("questionList")?.let { return it }
        root.optJSONArray("data")?.let { return it }
        throw IllegalArgumentException("Question JSON must contain a questions array.")
    }

    private fun extractAnswers(root: JSONObject): JSONArray {
        root.optJSONArray("answerKey")?.let { return it }
        root.optJSONArray("answers")?.let { return it }
        root.optJSONArray("data")?.let { return it }

        // Also accept a simple object map: {"1":"A","2":"B",...}
        val map = JSONArray()
        val keys = root.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = root.opt(key)
            if (value is String || value is Number) {
                map.put(JSONObject().put("questionNumber", key.toIntOrNull() ?: -1).put("correctAnswer", value.toString()))
            }
        }
        if (map.length() > 0) return map
        throw IllegalArgumentException("Answer-key JSON must contain answerKey/answers or a question-number map.")
    }

    private fun findAnswer(answers: JSONArray, id: String, number: Int, index: Int): String? {
        for (i in 0 until answers.length()) {
            val item = answers.opt(i)
            if (item is String || item is Number) {
                if (i == index) return item.toString()
                continue
            }
            if (item is JSONObject) {
                val itemId = firstNonBlank(item.optString("questionId"), item.optString("id"))
                val itemNumber = item.optInt("questionNumber", item.optInt("number", -1))
                if (itemId == id || itemNumber == number) {
                    return firstNonBlankOrNull(
                        item.optString("correctAnswer"),
                        item.optString("answer"),
                        item.optString("correctOption"),
                        item.optString("key")
                    )
                }
            }
        }
        return null
    }

    private fun normalizeSubject(value: String): String = when {
        value.equals("Physics", true) || value.equals("PHY", true) -> "PHYSICS"
        value.equals("Chemistry", true) || value.equals("CHE", true) -> "CHEMISTRY"
        value.equals("Mathematics", true) || value.equals("Maths", true) || value.equals("Math", true) || value.equals("MAT", true) -> "MATHEMATICS"
        else -> value.uppercase().ifBlank { "PHYSICS" }
    }

    private fun normalizeDifficulty(value: String): String = when {
        value.equals("Easy", true) -> "EASY"
        value.equals("Hard", true) -> "HARD"
        else -> "MEDIUM"
    }

    private fun readText(context: Context, uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: throw IllegalArgumentException("Could not read selected JSON file.")

    private fun firstNonBlank(vararg values: String): String =
        values.firstOrNull { it.isNotBlank() } ?: ""

    private fun firstNonBlankOrNull(vararg values: String): String? =
        values.firstOrNull { it.isNotBlank() }
}
