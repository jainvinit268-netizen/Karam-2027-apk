package com.example

import android.content.Context
import android.net.Uri
import com.example.data.local.JeeTestEntity
import com.example.data.model.JsonTestImportService
import com.example.data.repository.JeeRepository

/** Imports a generic provider-agnostic JSON test directly into the local Test Library. */
object DirectJsonTestImporter {
    suspend fun import(context: Context, uri: Uri): String {
        val payload = JsonTestImportService.readAndValidate(context, uri)
        val repository = JeeRepository(context)
        val questions = repository.parseQuestions(payload.questionsJson)
        require(questions.isNotEmpty()) { "The JSON contains no importable questions." }

        val test = JeeTestEntity(
            testId = payload.testId,
            title = payload.title,
            sourcePdfName = "JSON_IMPORT",
            totalQuestions = questions.size,
            durationMinutes = payload.durationMinutes,
            createdAt = System.currentTimeMillis(),
            questionsJson = payload.questionsJson,
            isSample = false,
            physicsQuestionsCount = questions.count { it.subject.name == "PHYSICS" },
            chemistryQuestionsCount = questions.count { it.subject.name == "CHEMISTRY" },
            mathsQuestionsCount = questions.count { it.subject.name == "MATHEMATICS" },
            tags = "JSON Imported"
        )
        repository.insertTest(test, questions)
        return test.testId
    }
}
