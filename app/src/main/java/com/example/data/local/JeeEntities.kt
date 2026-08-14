package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.example.data.model.Difficulty
import com.example.data.model.ForensicReport
import com.example.data.model.MistakeType
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.StudentResponse
import com.example.data.model.Subject
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Independent entity representing a JEE CBT Test.
 */
@Entity(tableName = "jee_tests")
data class JeeTestEntity(
    @PrimaryKey val testId: String,
    val title: String,
    val sourcePdfName: String? = null,
    val totalQuestions: Int = 75,
    val durationMinutes: Int = 180,
    val createdAt: Long = System.currentTimeMillis(),
    val questionsJson: String = "[]",
    val isCompleted: Boolean = false,
    val bestScore: Int? = null,
    val lastAttemptDate: Long? = null,
    val isSample: Boolean = false,
    val physicsQuestionsCount: Int = 25,
    val chemistryQuestionsCount: Int = 25,
    val mathsQuestionsCount: Int = 25,
    val tags: String = "JEE Main"
)

/**
 * Entity representing an individual question belonging to a test.
 */
@Entity(
    tableName = "jee_questions",
    foreignKeys = [
        ForeignKey(
            entity = JeeTestEntity::class,
            parentColumns = ["testId"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["testId"]),
        Index(value = ["testId", "subject"]),
        Index(value = ["testId", "questionNumber"])
    ]
)
data class JeeQuestionEntity(
    @PrimaryKey val questionEntityId: String, // Format: "${testId}_${questionId}"
    val testId: String,
    val questionId: String,
    val questionNumber: Int,
    val subject: Subject,
    val section: String = "Section A",
    val type: QuestionType,
    val questionText: String,
    val imageUrl: String? = null,
    val optionsJson: String = "[]", // Serialized List<String>
    val correctAnswer: String,
    val chapter: String,
    val concept: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val solutionText: String,
    val idealTimeSeconds: Int = 120,
    val youtubeSearchQuery: String = ""
)

/**
 * Entity representing a submitted attempt for a test.
 */
@Entity(
    tableName = "jee_attempts",
    foreignKeys = [
        ForeignKey(
            entity = JeeTestEntity::class,
            parentColumns = ["testId"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["testId"]),
        Index(value = ["attemptTimestamp"])
    ]
)
data class JeeAttemptEntity(
    @PrimaryKey val attemptId: String,
    val testId: String,
    val testTitle: String,
    val attemptTimestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val totalScore: Int = 0,
    val physicsScore: Int = 0,
    val chemistryScore: Int = 0,
    val mathsScore: Int = 0,
    val totalAttempted: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val accuracy: Float = 0f,
    val responsesJson: String,
    val forensicReportJson: String
)

/**
 * Entity storing individual question-level user response data.
 */
@Entity(
    tableName = "jee_user_responses",
    foreignKeys = [
        ForeignKey(
            entity = JeeAttemptEntity::class,
            parentColumns = ["attemptId"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["attemptId"]),
        Index(value = ["testId"]),
        Index(value = ["questionId"])
    ]
)
data class JeeUserResponseEntity(
    @PrimaryKey val responseEntityId: String, // Format: "${attemptId}_${questionId}"
    val attemptId: String,
    val testId: String,
    val questionId: String,
    val questionNumber: Int = 0,
    val subject: Subject = Subject.PHYSICS,
    val selectedOption: String? = null,
    val numericalAnswer: String? = null,
    val status: QuestionStatus = QuestionStatus.NOT_VISITED,
    val timeSpentSeconds: Int = 0,
    val isCorrect: Boolean? = null,
    val marksAwarded: Int = 0,
    val mistakeCategory: MistakeType = MistakeType.NONE,
    val userNote: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Relational model: Test with all its questions.
 */
data class TestWithQuestions(
    @Embedded val test: JeeTestEntity,
    @Relation(
        parentColumn = "testId",
        entityColumn = "testId"
    )
    val questions: List<JeeQuestionEntity>
)

/**
 * Relational model: Test with all its historical attempts.
 */
data class TestWithAttempts(
    @Embedded val test: JeeTestEntity,
    @Relation(
        parentColumn = "testId",
        entityColumn = "testId"
    )
    val attempts: List<JeeAttemptEntity>
)

/**
 * Relational model: Attempt with granular user responses.
 */
data class AttemptWithUserResponses(
    @Embedded val attempt: JeeAttemptEntity,
    @Relation(
        parentColumn = "attemptId",
        entityColumn = "attemptId"
    )
    val userResponses: List<JeeUserResponseEntity>
)

/**
 * Domain mapping extensions.
 */
fun QuestionItem.toEntity(testId: String, converters: JeeConverters): JeeQuestionEntity {
    return JeeQuestionEntity(
        questionEntityId = "${testId}_${id}",
        testId = testId,
        questionId = id,
        questionNumber = questionNumber,
        subject = subject,
        section = section,
        type = type,
        questionText = questionText,
        imageUrl = imageUrl,
        optionsJson = converters.fromStringList(options),
        correctAnswer = correctAnswer,
        chapter = chapter,
        concept = concept,
        difficulty = difficulty,
        solutionText = solutionText,
        idealTimeSeconds = idealTimeSeconds,
        youtubeSearchQuery = youtubeSearchQuery
    )
}

fun JeeQuestionEntity.toDomain(converters: JeeConverters): QuestionItem {
    return QuestionItem(
        id = questionId,
        questionNumber = questionNumber,
        subject = subject,
        section = section,
        type = type,
        questionText = questionText,
        imageUrl = imageUrl,
        options = converters.toStringList(optionsJson),
        correctAnswer = correctAnswer,
        chapter = chapter,
        concept = concept,
        difficulty = difficulty,
        solutionText = solutionText,
        idealTimeSeconds = idealTimeSeconds,
        youtubeSearchQuery = youtubeSearchQuery
    )
}

fun StudentResponse.toEntity(attemptId: String, testId: String, qNum: Int = 0, subj: Subject = Subject.PHYSICS): JeeUserResponseEntity {
    return JeeUserResponseEntity(
        responseEntityId = "${attemptId}_${questionId}",
        attemptId = attemptId,
        testId = testId,
        questionId = questionId,
        questionNumber = qNum,
        subject = subj,
        selectedOption = selectedOption,
        numericalAnswer = numericalAnswer,
        status = status,
        timeSpentSeconds = timeSpentSeconds,
        isCorrect = isCorrect,
        marksAwarded = marksAwarded,
        mistakeCategory = mistakeCategory,
        userNote = userNote
    )
}

fun JeeUserResponseEntity.toDomain(): StudentResponse {
    return StudentResponse(
        questionId = questionId,
        selectedOption = selectedOption,
        numericalAnswer = numericalAnswer,
        status = status,
        timeSpentSeconds = timeSpentSeconds,
        isCorrect = isCorrect,
        marksAwarded = marksAwarded,
        mistakeCategory = mistakeCategory,
        userNote = userNote
    )
}

/**
 * Room TypeConverters.
 */
class JeeConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val questionListType = Types.newParameterizedType(List::class.java, QuestionItem::class.java)
    private val responseMapType = Types.newParameterizedType(Map::class.java, String::class.java, StudentResponse::class.java)

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        val adapter = moshi.adapter<List<String>>(stringListType)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String): List<String> {
        return try {
            val adapter = moshi.adapter<List<String>>(stringListType)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromQuestionList(list: List<QuestionItem>): String {
        val adapter = moshi.adapter<List<QuestionItem>>(questionListType)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toQuestionList(json: String): List<QuestionItem> {
        return try {
            val adapter = moshi.adapter<List<QuestionItem>>(questionListType)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromResponseMap(map: Map<String, StudentResponse>): String {
        val adapter = moshi.adapter<Map<String, StudentResponse>>(responseMapType)
        return adapter.toJson(map)
    }

    @TypeConverter
    fun toResponseMap(json: String): Map<String, StudentResponse> {
        return try {
            val adapter = moshi.adapter<Map<String, StudentResponse>>(responseMapType)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromForensicReport(report: ForensicReport): String {
        val adapter = moshi.adapter(ForensicReport::class.java)
        return adapter.toJson(report)
    }

    @TypeConverter
    fun toForensicReport(json: String): ForensicReport? {
        return try {
            val adapter = moshi.adapter(ForensicReport::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromSubject(subject: Subject): String = subject.name

    @TypeConverter
    fun toSubject(value: String): Subject = try {
        Subject.valueOf(value)
    } catch (e: Exception) {
        Subject.PHYSICS
    }

    @TypeConverter
    fun fromQuestionType(type: QuestionType): String = type.name

    @TypeConverter
    fun toQuestionType(value: String): QuestionType = try {
        QuestionType.valueOf(value)
    } catch (e: Exception) {
        QuestionType.MCQ
    }

    @TypeConverter
    fun fromQuestionStatus(status: QuestionStatus): String = status.name

    @TypeConverter
    fun toQuestionStatus(value: String): QuestionStatus = try {
        QuestionStatus.valueOf(value)
    } catch (e: Exception) {
        QuestionStatus.NOT_VISITED
    }

    @TypeConverter
    fun fromDifficulty(diff: Difficulty): String = diff.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = try {
        Difficulty.valueOf(value)
    } catch (e: Exception) {
        Difficulty.MEDIUM
    }

    @TypeConverter
    fun fromMistakeType(mistake: MistakeType): String = mistake.name

    @TypeConverter
    fun toMistakeType(value: String): MistakeType = try {
        MistakeType.valueOf(value)
    } catch (e: Exception) {
        MistakeType.NONE
    }
}

