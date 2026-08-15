package com.example

import android.content.Context
import com.example.data.local.JeeTestEntity
import com.example.data.model.JsonTestFileImporter
import com.example.data.repository.JeeRepository

/** Imports a generic JSON CBT file into KARAM's local Test Library. */
object JsonFileImportHelper {
    suspend fun import(context: Context, jsonText: String): String {
        val parsed = JsonTestFileImporter.parse(jsonText)
        val payload = parsed.payload
        val repository = JeeRepository(context)
        val questions = repository.parseQuestions(payload.questionsJson)
        require(questions.isNotEmpty()) { "The JSON contains no usable questions." }

        val existing = repository.getTestById(payload.testId)
        if (existing == null) {
            val test = JeeTestEntity(
                testId = payload.testId,
                title = payload.title,
                sourcePdfName = "JSON Import",
                totalQuestions = questions.size,
                durationMinutes = payload.durationMinutes,
                createdAt = System.currentTimeMillis(),
                questionsJson = payload.questionsJson,
                isSample = false,
                physicsQuestionsCount = questions.count { it.subject.name == "PHYSICS" },
                chemistryQuestionsCount = questions.count { it.subject.name == "CHEMISTRY" },
                mathsQuestionsCount = questions.count { it.subject.name == "MATHEMATICS" },
                tags = "JSON Import"
            )
            repository.insertTest(test, questions)
        }
        return payload.testId
    }
}
