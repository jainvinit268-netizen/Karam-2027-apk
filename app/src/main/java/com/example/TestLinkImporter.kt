package com.example

import android.content.Context
import android.net.Uri
import com.example.data.local.JeeTestEntity
import com.example.data.model.TestLinkCodec
import com.example.data.model.TestLinkPayload
import com.example.data.repository.JeeRepository

/** Imports a self-contained KARAM CBT snapshot from a test-link URI. */
object TestLinkImporter {
    suspend fun import(context: Context, uri: Uri): String? {
        val token = TestLinkCodec.tokenFromUri(uri) ?: return null
        val payload = TestLinkPayload.decode(token)
        val repository = JeeRepository(context)
        val questions = repository.parseQuestions(payload.questionsJson)
        if (questions.isEmpty()) return null

        val test = JeeTestEntity(
            testId = payload.testId,
            title = payload.title,
            sourcePdfName = "KARAM_Test_Link",
            totalQuestions = questions.size,
            durationMinutes = payload.durationMinutes,
            createdAt = System.currentTimeMillis(),
            questionsJson = payload.questionsJson,
            isSample = false,
            physicsQuestionsCount = questions.count { it.subject.name == "PHYSICS" },
            chemistryQuestionsCount = questions.count { it.subject.name == "CHEMISTRY" },
            mathsQuestionsCount = questions.count { it.subject.name == "MATHEMATICS" },
            tags = "Imported Test Link"
        )
        repository.insertTest(test, questions)
        return test.testId
    }
}
