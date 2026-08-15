package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIProvider
import com.example.ai.AIProviderType
import com.example.ai.DocumentProcessingParams
import com.example.ai.DocumentProcessingResult
import com.example.ai.GeminiAIProvider
import com.example.ai.GeminiJeeExtractor
import com.example.ai.LocalLayoutAIProvider
import com.example.ai.PdfSourceType
import com.example.ai.ProcessingProgress
import com.example.ai.ProcessingStep
import com.example.data.ai.AiConfigState
import com.example.data.ai.AiConnectionStatus
import com.example.data.ai.AiKeyManager
import com.example.data.auth.AuthState
import com.example.data.auth.GoogleAuthManager
import com.example.data.auth.UserProfile
import com.example.data.local.JeeAttemptEntity
import com.example.data.local.JeeConverters
import com.example.data.local.JeeTestEntity
import com.example.data.model.ForensicReport
import com.example.data.model.MistakeType
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.StudentResponse
import com.example.data.model.Subject
import com.example.data.repository.JeeRepository
import com.example.data.sample.SampleJeePapers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AppScreen {
    object SplashInvocation : AppScreen
    object TestLibrary : AppScreen
    object ConvertPdf : AppScreen
    data class CbtExam(val testId: String) : AppScreen
    data class ForensicAnalysis(val attemptId: String) : AppScreen
}

data class ConversionUiState(
    val isProcessing: Boolean = false,
    val currentStep: ProcessingStep = ProcessingStep.ANALYZING_PDF,
    val progressPercent: Int = 0,
    val progressMessage: String = "",
    val pagesProcessed: Int = 0,
    val totalPages: Int = 0,
    val questionsDetected: Int = 0,
    val elapsedSeconds: Long = 0,
    val estimatedRemainingSeconds: Int? = null,
    val errorMessage: String? = null,
    val extractedQuestionsCount: Int = 0,
    val validatedCount: Int = 0,
    val diagramsCount: Int = 0,
    val flaggedCount: Int = 0,
    val newlyCreatedTestId: String? = null,
    val aiStatusMessage: String? = null,
    val extractedQuestions: List<QuestionItem> = emptyList(),
    val isReviewModalOpen: Boolean = false,
    val providerUsed: AIProviderType? = null,
    val pdfType: PdfSourceType? = null,
    val isSlowOrStalled: Boolean = false
)

data class ExamUiState(
    val test: JeeTestEntity? = null,
    val allQuestions: List<QuestionItem> = emptyList(),
    val currentSubject: Subject = Subject.PHYSICS,
    val currentQuestionIndexInSubject: Int = 0,
    val responses: Map<String, StudentResponse> = emptyMap(),
    val remainingTimeSeconds: Long = 180 * 60L,
    val totalAllocatedSeconds: Long = 180 * 60L,
    val isTimerRunning: Boolean = false,
    val isSubmitDialogOpen: Boolean = false,
    val isExitDialogOpen: Boolean = false,
    val isPaletteDrawerOpen: Boolean = false,
    val isSubmitted: Boolean = false,
    val generatedAttemptId: String? = null
)

class JeeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = JeeRepository(application)
    private val authManager = GoogleAuthManager.getInstance(application)
    private val aiKeyManager = AiKeyManager.getInstance(application)

    val authState: StateFlow<AuthState> = authManager.authState
    val currentUser: StateFlow<UserProfile?> = authManager.currentUser
    val aiConfigState: StateFlow<AiConfigState> = aiKeyManager.configState

    val allTests: StateFlow<List<JeeTestEntity>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttempts: StateFlow<List<JeeAttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.SplashInvocation)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _conversionState = MutableStateFlow(ConversionUiState())
    val conversionState: StateFlow<ConversionUiState> = _conversionState.asStateFlow()

    private val _examState = MutableStateFlow(ExamUiState())
    val examState: StateFlow<ExamUiState> = _examState.asStateFlow()

    private val _currentAttempt = MutableStateFlow<JeeAttemptEntity?>(null)
    val currentAttempt: StateFlow<JeeAttemptEntity?> = _currentAttempt.asStateFlow()

    private val _currentForensicReport = MutableStateFlow<ForensicReport?>(null)
    val currentForensicReport: StateFlow<ForensicReport?> = _currentForensicReport.asStateFlow()

    private val _selectedAnalysisQuestion = MutableStateFlow<QuestionItem?>(null)
    val selectedAnalysisQuestion: StateFlow<QuestionItem?> = _selectedAnalysisQuestion.asStateFlow()

    private var timerJob: Job? = null
    private var questionStartTimeMillis = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            repository.initializeDefaultTestsIfEmpty()
        }
    }

    // ---------------- Google Authentication Actions ----------------
    fun signInWithGoogle(activityContext: Context, overrideClientId: String? = null) {
        viewModelScope.launch {
            authManager.signInWithGoogle(activityContext, overrideClientId)
        }
    }

    fun signInDirectWithGmail(email: String, displayName: String? = null) {
        authManager.signInDirectWithGmail(email, displayName)
    }

    fun saveOAuthClientId(clientId: String) {
        authManager.saveCustomOAuthClientId(clientId)
    }

    fun getOAuthClientId(): String? {
        return authManager.getCustomOAuthClientId()
    }

    fun clearOAuthClientId() {
        authManager.clearCustomOAuthClientId()
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    fun clearAuthError() {
        authManager.clearError()
    }

    // ---------------- AI / Gemini Key Management ----------------
    fun saveGeminiApiKey(key: String) {
        aiKeyManager.saveUserApiKey(key)
    }

    fun clearGeminiApiKey() {
        aiKeyManager.clearUserApiKey()
    }

    fun testGeminiConnection(overrideKey: String? = null) {
        viewModelScope.launch {
            aiKeyManager.testConnection(overrideKey)
        }
    }

    fun resetGeminiTestStatus() {
        aiKeyManager.resetTestStatus()
    }

    fun navigateTo(screen: AppScreen) {
        if (screen is AppScreen.CbtExam) {
            startExam(screen.testId)
        } else if (screen is AppScreen.ForensicAnalysis) {
            loadAttempt(screen.attemptId)
        }
        _currentScreen.value = screen
    }

    private val geminiAiProvider = GeminiAIProvider(application)
    private val localAiProvider = LocalLayoutAIProvider()
    private var conversionJob: Job? = null
    private var conversionTickerJob: Job? = null
    private var lastConversionParams: DocumentProcessingParams? = null
    private var lastResolvedPdfName: String = "Uploaded_Paper.pdf"
    private var activeQuestionPdfUri: android.net.Uri? = null

    // ---------------- PDF & Answer Key AI Conversion ----------------
    fun cancelConversion() {
        conversionJob?.cancel()
        conversionTickerJob?.cancel()
        _conversionState.value = _conversionState.value.copy(
            isProcessing = false,
            isSlowOrStalled = false,
            errorMessage = "Processing was cancelled by user."
        )
    }

    fun continueWaiting() {
        _conversionState.value = _conversionState.value.copy(isSlowOrStalled = false)
    }

    fun retryConversion() {
        val params = lastConversionParams ?: return
        executeDocumentConversion(params, lastResolvedPdfName)
    }

    fun convertPdfToCbt(
        testTitle: String,
        questionPaperText: String,
        answerKeyText: String,
        pdfFileName: String = "Uploaded_Paper.pdf",
        durationMinutes: Int = 180
    ) {
        activeQuestionPdfUri = null
        val params = DocumentProcessingParams(
            testTitle = testTitle.ifBlank { "JEE Main Paper (${pdfFileName.substringBeforeLast(".")})" },
            questionPaperText = questionPaperText,
            answerKeyText = answerKeyText,
            pageImagesBase64 = emptyList(),
            totalPages = 1,
            durationMinutes = durationMinutes,
            pdfType = PdfSourceType.NATIVE_TEXT
        )
        lastConversionParams = params
        lastResolvedPdfName = pdfFileName
        executeDocumentConversion(params, pdfFileName)
    }

    /** Imports a directly accessible PDF URL through the existing persistent PDF->CBT pipeline. */
    fun convertPdfFromUrl(
        testTitle: String,
        pdfUrl: String,
        answerKeyUri: android.net.Uri?,
        fallbackAnswerText: String,
        durationMinutes: Int = 180
    ) {
        activeQuestionPdfUri = null
        conversionJob?.cancel()
        conversionTickerJob?.cancel()
        conversionJob = viewModelScope.launch(Dispatchers.IO) {
            _conversionState.value = ConversionUiState(
                isProcessing = true,
                currentStep = ProcessingStep.ANALYZING_PDF,
                progressPercent = 2,
                progressMessage = "Downloading PDF from source link..."
            )
            try {
                val normalized = pdfUrl.trim()
                require(normalized.startsWith("https://") || normalized.startsWith("http://")) {
                    "Please paste a direct HTTP/HTTPS PDF link."
                }
                val connection = (java.net.URL(normalized).openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 20000
                    readTimeout = 60000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "KARAM-2027/1.0")
                }
                connection.connect()
                val code = connection.responseCode
                require(code in 200..299) { "Source link returned HTTP $code." }
                val file = java.io.File(getApplication<Application>().cacheDir, "linked_source_${System.currentTimeMillis()}.pdf")
                connection.inputStream.use { input ->
                    java.io.FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                connection.disconnect()
                require(file.length() > 100 && file.inputStream().use { it.readNBytes(4).contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46)) }) {
                    "The source URL did not return a valid PDF."
                }
                _conversionState.value = _conversionState.value.copy(
                    progressPercent = 5,
                    progressMessage = "PDF downloaded. Starting extraction..."
                )
                convertFilesToCbt(
                    testTitle = testTitle.ifBlank { "Linked JEE Paper" },
                    questionPdfUri = android.net.Uri.fromFile(file),
                    answerKeyUri = answerKeyUri,
                    fallbackQuestionText = "",
                    fallbackAnswerText = fallbackAnswerText,
                    durationMinutes = durationMinutes,
                    pdfFileName = "Linked_JEE_Paper.pdf"
                )
            } catch (e: Exception) {
                _conversionState.value = ConversionUiState(
                    isProcessing = false,
                    errorMessage = "Source-link import failed: ${e.localizedMessage ?: "invalid PDF link"}"
                )
            }
        }
    }

    fun convertFilesToCbt(
        testTitle: String,
        questionPdfUri: android.net.Uri?,
        answerKeyUri: android.net.Uri?,
        fallbackQuestionText: String,
        fallbackAnswerText: String,
        durationMinutes: Int = 180,
        pdfFileName: String = "Uploaded_Paper.pdf"
    ) {
        activeQuestionPdfUri = questionPdfUri
        conversionJob?.cancel()
        conversionTickerJob?.cancel()

        val startTimestamp = System.currentTimeMillis()
        var lastActivityTime = System.currentTimeMillis()

        // Start real-time live elapsed timer ticker IMMEDIATELY
        conversionTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - startTimestamp) / 1000
                val currentState = _conversionState.value
                val isStalledNow = (System.currentTimeMillis() - lastActivityTime) > 25000L && currentState.isProcessing

                val eta = if (currentState.progressPercent in 8..95 && elapsed > 0) {
                    ((elapsed * (100 - currentState.progressPercent)) / currentState.progressPercent).toInt().coerceIn(1, 300)
                } else null

                _conversionState.value = currentState.copy(
                    elapsedSeconds = elapsed,
                    estimatedRemainingSeconds = eta,
                    isSlowOrStalled = isStalledNow
                )
            }
        }

        conversionJob = viewModelScope.launch {
            _conversionState.value = ConversionUiState(
                isProcessing = true,
                currentStep = ProcessingStep.ANALYZING_PDF,
                progressPercent = 5,
                progressMessage = "Analyzing document structure & reading pages...",
                elapsedSeconds = 0,
                isSlowOrStalled = false
            )

            try {
                var qPaperText = fallbackQuestionText
                var pageImages = emptyList<String>()
                var resolvedPdfName = pdfFileName
                var totalPagesCount = 1
                var detectedPdfType = PdfSourceType.NATIVE_TEXT

                if (questionPdfUri != null) {
                    val extractedDoc = com.example.data.ai.PdfDocumentHelper.extractContentFromUri(
                        context = getApplication(),
                        uri = questionPdfUri,
                        onProgress = { step, percent, message, pagesDone, totalPages ->
                            lastActivityTime = System.currentTimeMillis()
                            _conversionState.value = _conversionState.value.copy(
                                currentStep = step,
                                progressPercent = percent,
                                progressMessage = message,
                                pagesProcessed = pagesDone,
                                totalPages = totalPages,
                                isSlowOrStalled = false
                            )
                        }
                    )
                    if (extractedDoc.extractedText.isNotBlank()) {
                        qPaperText = extractedDoc.extractedText
                    }
                    pageImages = extractedDoc.base64Images
                    resolvedPdfName = extractedDoc.fileName
                    totalPagesCount = maxOf(1, extractedDoc.pageCount)
                    detectedPdfType = extractedDoc.pdfType
                }

                _conversionState.value = _conversionState.value.copy(
                    currentStep = ProcessingStep.READING_ANSWER_KEY,
                    progressPercent = 25,
                    progressMessage = "Processing Official Answer Key & matching layout...",
                    pdfType = detectedPdfType,
                    totalPages = totalPagesCount
                )
                lastActivityTime = System.currentTimeMillis()

                var ansKeyText = fallbackAnswerText
                if (answerKeyUri != null) {
                    val extractedKeyDoc = com.example.data.ai.PdfDocumentHelper.extractContentFromUri(
                        context = getApplication(),
                        uri = answerKeyUri,
                        onProgress = { step, _, message, _, _ ->
                            lastActivityTime = System.currentTimeMillis()
                            _conversionState.value = _conversionState.value.copy(
                                currentStep = ProcessingStep.READING_ANSWER_KEY,
                                progressMessage = "Answer Key: $message",
                                isSlowOrStalled = false
                            )
                        }
                    )
                    if (extractedKeyDoc.extractedText.isNotBlank()) {
                        ansKeyText = extractedKeyDoc.extractedText
                    }
                }

                val params = DocumentProcessingParams(
                    testTitle = testTitle.ifBlank { "JEE Main CBT (${resolvedPdfName.substringBeforeLast(".")})" },
                    questionPaperText = qPaperText,
                    answerKeyText = ansKeyText,
                    pageImagesBase64 = pageImages,
                    totalPages = totalPagesCount,
                    durationMinutes = durationMinutes,
                    pdfType = detectedPdfType
                )
                lastConversionParams = params
                lastResolvedPdfName = resolvedPdfName

                runDocumentProcessingPipeline(params, resolvedPdfName, startTimestamp)
            } catch (e: CancellationException) {
                conversionTickerJob?.cancel()
            } catch (e: Exception) {
                conversionTickerJob?.cancel()
                _conversionState.value = ConversionUiState(
                    isProcessing = false,
                    isSlowOrStalled = false,
                    errorMessage = "Failed to extract document files: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun executeDocumentConversion(params: DocumentProcessingParams, resolvedPdfName: String) {
        conversionJob?.cancel()
        conversionTickerJob?.cancel()

        val startTimestamp = System.currentTimeMillis()
        var lastActivityTime = System.currentTimeMillis()

        // Start live elapsed timer ticker
        conversionTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - startTimestamp) / 1000
                val currentState = _conversionState.value
                val isStalledNow = (System.currentTimeMillis() - lastActivityTime) > 25000L && currentState.isProcessing

                val eta = if (currentState.progressPercent in 8..95 && elapsed > 0) {
                    ((elapsed * (100 - currentState.progressPercent)) / currentState.progressPercent).toInt().coerceIn(1, 300)
                } else null

                _conversionState.value = currentState.copy(
                    elapsedSeconds = elapsed,
                    estimatedRemainingSeconds = eta,
                    isSlowOrStalled = isStalledNow
                )
            }
        }

        conversionJob = viewModelScope.launch {
            runDocumentProcessingPipeline(params, resolvedPdfName, startTimestamp)
        }
    }

    private suspend fun runDocumentProcessingPipeline(
        params: DocumentProcessingParams,
        resolvedPdfName: String,
        startTimestamp: Long
    ) {
        var lastActivityTime = System.currentTimeMillis()

        _conversionState.value = _conversionState.value.copy(
            isProcessing = true,
            errorMessage = null,
            pdfType = params.pdfType,
            isSlowOrStalled = false
        )

        try {
            // Choose Provider: Gemini if configured, otherwise Fallback to LocalLayout
            val activeProvider: AIProvider = if (geminiAiProvider.isConfigured()) {
                geminiAiProvider
            } else {
                localAiProvider
            }

            val result: DocumentProcessingResult = activeProvider.processDocument(params) { progress ->
                lastActivityTime = System.currentTimeMillis()
                _conversionState.value = _conversionState.value.copy(
                    currentStep = progress.step,
                    progressPercent = progress.progressPercent,
                    progressMessage = progress.message,
                    pagesProcessed = progress.pagesProcessed,
                    totalPages = progress.totalPages,
                    questionsDetected = progress.questionsDetected,
                    estimatedRemainingSeconds = progress.estimatedRemainingSeconds,
                    pdfType = progress.pdfType ?: params.pdfType,
                    isSlowOrStalled = false
                )
            }

            if (result.success && result.questions.isNotEmpty()) {
                val testId = UUID.randomUUID().toString()

                // Generate authentic original PDF visual crops for all detected questions
                _conversionState.value = _conversionState.value.copy(
                    currentStep = ProcessingStep.CREATING_QUESTION_CROPS,
                    progressPercent = 88,
                    progressMessage = "Generating high-resolution visual question crops from original PDF...",
                    questionsDetected = result.questions.size
                )

                val questionsWithVisualCrops = if (activeQuestionPdfUri != null) {
                    com.example.data.ai.PdfVisualCropper.generateOriginalQuestionCrops(
                        context = getApplication(),
                        pdfUri = activeQuestionPdfUri,
                        testId = testId,
                        questions = result.questions,
                        onProgress = { current, total, pageNum, msg ->
                            lastActivityTime = System.currentTimeMillis()
                            val cropPercent = 88 + ((current * 8) / maxOf(1, total))
                            _conversionState.value = _conversionState.value.copy(
                                currentStep = ProcessingStep.CREATING_QUESTION_CROPS,
                                progressPercent = cropPercent,
                                progressMessage = msg,
                                pagesProcessed = pageNum,
                                totalPages = maxOf(pageNum, params.totalPages),
                                isSlowOrStalled = false
                            )
                        }
                    )
                } else {
                    result.questions
                }

                _conversionState.value = _conversionState.value.copy(
                    currentStep = ProcessingStep.BUILDING_CBT,
                    progressPercent = 98,
                    progressMessage = "Building NTA CBT database records..."
                )

                val phyCount = questionsWithVisualCrops.count { it.subject == Subject.PHYSICS }
                val cheCount = questionsWithVisualCrops.count { it.subject == Subject.CHEMISTRY }
                val matCount = questionsWithVisualCrops.count { it.subject == Subject.MATHEMATICS }

                val newTest = JeeTestEntity(
                    testId = testId,
                    title = result.testTitle,
                    sourcePdfName = resolvedPdfName,
                    totalQuestions = questionsWithVisualCrops.size,
                    durationMinutes = params.durationMinutes,
                    createdAt = System.currentTimeMillis(),
                    questionsJson = repository.convertersFromQuestions(questionsWithVisualCrops),
                    isSample = false,
                    physicsQuestionsCount = phyCount,
                    chemistryQuestionsCount = cheCount,
                    mathsQuestionsCount = matCount,
                    tags = if (result.providerUsed == AIProviderType.GEMINI_API) "AI Converted (Gemini), $resolvedPdfName" else "Local Layout Engine, $resolvedPdfName"
                )

                repository.insertTest(newTest, questionsWithVisualCrops)
                conversionTickerJob?.cancel()

                val totalElapsed = (System.currentTimeMillis() - startTimestamp) / 1000

                _conversionState.value = ConversionUiState(
                    isProcessing = false,
                    currentStep = ProcessingStep.COMPLETE,
                    progressPercent = 100,
                    progressMessage = "CBT Generation Complete!",
                    extractedQuestionsCount = questionsWithVisualCrops.size,
                    validatedCount = result.validatedCount,
                    diagramsCount = questionsWithVisualCrops.count { it.imageUrl != null },
                    flaggedCount = result.flaggedQuestions.size,
                    newlyCreatedTestId = testId,
                    aiStatusMessage = result.statusMessage,
                    extractedQuestions = questionsWithVisualCrops,
                    elapsedSeconds = totalElapsed,
                    providerUsed = result.providerUsed,
                    pdfType = params.pdfType,
                    isSlowOrStalled = false
                )
            } else {
                conversionTickerJob?.cancel()
                _conversionState.value = ConversionUiState(
                    isProcessing = false,
                    isSlowOrStalled = false,
                    errorMessage = result.errorMessage ?: "Could not detect questions. Please verify your PDF format or text."
                )
            }
        } catch (e: CancellationException) {
            conversionTickerJob?.cancel()
        } catch (e: Exception) {
            conversionTickerJob?.cancel()
            _conversionState.value = ConversionUiState(
                isProcessing = false,
                isSlowOrStalled = false,
                errorMessage = "Extraction failed: ${e.localizedMessage}"
            )
        }
    }

    fun createRevisionTestFromMistakes(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val allAttemptsList = allAttempts.value
            val allTestsList = allTests.value
            val testMap = allTestsList.associateBy { it.testId }
            val mistakeQuestions = mutableListOf<QuestionItem>()
            val seenIds = mutableSetOf<String>()

            for (att in allAttemptsList) {
                val test = testMap[att.testId] ?: continue
                val qList = repository.parseQuestions(test.questionsJson)
                val respMap = repository.parseResponses(att.responsesJson)

                for (q in qList) {
                    val resp = respMap[q.id]
                    val isWrong = resp?.isCorrect == false || (resp != null && resp.status == QuestionStatus.ANSWERED && resp.marksAwarded <= 0)
                    val isFlagged = resp?.mistakeCategory != null && resp.mistakeCategory != MistakeType.NONE
                    if ((isWrong || isFlagged) && !seenIds.contains(q.id)) {
                        seenIds.add(q.id)
                        mistakeQuestions.add(q)
                    }
                }
            }

            if (mistakeQuestions.isEmpty()) return@launch

            val revTestId = "rev_${UUID.randomUUID().toString().take(8)}"
            val phyCount = mistakeQuestions.count { it.subject == Subject.PHYSICS }
            val cheCount = mistakeQuestions.count { it.subject == Subject.CHEMISTRY }
            val matCount = mistakeQuestions.count { it.subject == Subject.MATHEMATICS }

            val revTest = JeeTestEntity(
                testId = revTestId,
                title = "🎯 Personal Mistake Revision Test (${mistakeQuestions.size} Questions)",
                sourcePdfName = "Generated_From_Mistake_Book",
                totalQuestions = mistakeQuestions.size,
                durationMinutes = maxOf(15, mistakeQuestions.size * 2),
                createdAt = System.currentTimeMillis(),
                questionsJson = repository.convertersFromQuestions(mistakeQuestions),
                isSample = false,
                physicsQuestionsCount = phyCount,
                chemistryQuestionsCount = cheCount,
                mathsQuestionsCount = matCount,
                tags = "Personal Revision, Mistake Book, High Priority"
            )

            repository.insertTest(revTest, mistakeQuestions)
            onSuccess(revTestId)
        }
    }

    fun sendForensicEmail(context: Context, attempt: JeeAttemptEntity, report: ForensicReport, recipient: String = "jainvinit268@gmail.com") {
        val subject = "JEE Main CBT Analysis Report: ${attempt.testTitle} [Score: ${report.totalScore}/300]"
        val phy = report.subjectAnalyses.find { it.subject == com.example.data.model.Subject.PHYSICS }
        val chem = report.subjectAnalyses.find { it.subject == com.example.data.model.Subject.CHEMISTRY }
        val math = report.subjectAnalyses.find { it.subject == com.example.data.model.Subject.MATHEMATICS }

        val body = """
====================================================
JEE MAIN CBT DETAILED FORENSIC PERFORMANCE REPORT
====================================================
Candidate: ${currentUser.value?.displayName ?: "JEE Aspirant"} (${currentUser.value?.email ?: recipient})
Test Name: ${attempt.testTitle}
Date: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(attempt.attemptTimestamp))}

OVERALL SCORECARD:
----------------------------------------------------
• Total Score: ${report.totalScore} / ${report.maxPossibleScore} (${String.format("%.1f", (report.totalScore.toFloat() / maxOf(1, report.maxPossibleScore)) * 100)}%)
• Estimated Percentile: ${String.format("%.2f", report.estimatedPercentile)} %ile
• Accuracy: ${String.format("%.1f", report.accuracyPercentage)}%
• Attempt Rate: ${String.format("%.1f", (report.totalAttempted.toFloat() / maxOf(1, report.totalQuestions)) * 100)}%
• Total Questions: ${report.totalQuestions}
• Correct: ${report.correctCount} (+${report.correctCount * 4} marks)
• Incorrect: ${report.incorrectCount} (-${report.totalNegativeMarksLost} marks)
• Unattempted: ${report.unattemptedCount}
• Net Negative Marks Lost: -${report.totalNegativeMarksLost} marks

SUBJECT-WISE PERFORMANCE:
----------------------------------------------------
1. PHYSICS:
   - Score: ${phy?.score ?: 0} / ${phy?.maxScore ?: 100}
   - Accuracy: ${String.format("%.1f", phy?.accuracy ?: 0f)}%
   - Correct / Incorrect / Unattempted: ${phy?.correctCount ?: 0} / ${phy?.incorrectCount ?: 0} / ${phy?.unattemptedCount ?: 0}
   - Negative Marks Lost: -${phy?.negativeMarksLost ?: 0}
   - Avg Time / Question: ${phy?.avgTimePerQuestionSeconds ?: 0}s

2. CHEMISTRY:
   - Score: ${chem?.score ?: 0} / ${chem?.maxScore ?: 100}
   - Accuracy: ${String.format("%.1f", chem?.accuracy ?: 0f)}%
   - Correct / Incorrect / Unattempted: ${chem?.correctCount ?: 0} / ${chem?.incorrectCount ?: 0} / ${chem?.unattemptedCount ?: 0}
   - Negative Marks Lost: -${chem?.negativeMarksLost ?: 0}
   - Avg Time / Question: ${chem?.avgTimePerQuestionSeconds ?: 0}s

3. MATHEMATICS:
   - Score: ${math?.score ?: 0} / ${math?.maxScore ?: 100}
   - Accuracy: ${String.format("%.1f", math?.accuracy ?: 0f)}%
   - Correct / Incorrect / Unattempted: ${math?.correctCount ?: 0} / ${math?.incorrectCount ?: 0} / ${math?.unattemptedCount ?: 0}
   - Negative Marks Lost: -${math?.negativeMarksLost ?: 0}
   - Avg Time / Question: ${math?.avgTimePerQuestionSeconds ?: 0}s

TIME FORENSICS & LEAK AUDIT:
----------------------------------------------------
• Time Traps: ${report.timeTrapsCount} questions
• Silly Mistakes: ${report.sillyMistakesCount} questions
• Conceptual Gaps: ${report.conceptualMistakesCount} questions
• Calculation Blunders: ${report.calculationMistakesCount} questions
• Wrong Approach: ${report.wrongApproachCount} questions

RECOMMENDED REVISION ACTIONS:
----------------------------------------------------
1. Review logged mistakes in the app's 'Mistake Book'.
2. Take the Personal Revision Mini-Test to master weak concepts.
3. Focus on time allocation in slow chapters before the next mock.

====================================================
Generated by AI Studio JEE CBT Platform
        """.trimIndent()

        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$recipient")
            putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
            putExtra(android.content.Intent.EXTRA_TEXT, body)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(android.content.Intent.createChooser(intent, "Send JEE Analysis Report via Email"))
        } catch (e: Exception) {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                putExtra(android.content.Intent.EXTRA_TEXT, body)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share JEE Analysis Report"))
        }
    }

    fun toggleReviewModal(isOpen: Boolean) {
        _conversionState.value = _conversionState.value.copy(isReviewModalOpen = isOpen)
    }

    fun resetConversionState() {
        _conversionState.value = ConversionUiState()
    }

    fun deleteTest(testId: String) {
        viewModelScope.launch {
            repository.deleteTest(testId)
        }
    }

    // ---------------- CBT Exam Engine ----------------
    fun startExam(testId: String) {
        startExamWithCustomConfig(testId = testId, customMinutes = null, questionLimit = null, subjectFilter = null)
    }

    fun startExamWithCustomConfig(
        testId: String,
        customMinutes: Int? = null,
        questionLimit: Int? = null,
        subjectFilter: Subject? = null
    ) {
        viewModelScope.launch {
            val test = repository.getTestById(testId) ?: return@launch
            var questions = repository.parseQuestions(test.questionsJson)

            if (subjectFilter != null) {
                questions = questions.filter { it.subject == subjectFilter }
            }
            if (questionLimit != null && questionLimit > 0 && questionLimit < questions.size) {
                questions = questions.take(questionLimit)
            }

            val allocatedDurationMinutes = customMinutes ?: test.durationMinutes
            val initialSubject = questions.firstOrNull()?.subject ?: Subject.PHYSICS

            val initialResponses = questions.associate { q ->
                q.id to StudentResponse(
                    questionId = q.id,
                    status = QuestionStatus.NOT_VISITED
                )
            }.toMutableMap()

            // Mark the very first question as NOT_ANSWERED (visited)
            if (questions.isNotEmpty()) {
                val firstQ = questions.first()
                initialResponses[firstQ.id] = StudentResponse(
                    questionId = firstQ.id,
                    status = QuestionStatus.NOT_ANSWERED
                )
            }

            _examState.value = ExamUiState(
                test = test.copy(durationMinutes = allocatedDurationMinutes, totalQuestions = questions.size),
                allQuestions = questions,
                currentSubject = initialSubject,
                currentQuestionIndexInSubject = 0,
                responses = initialResponses,
                remainingTimeSeconds = allocatedDurationMinutes * 60L,
                totalAllocatedSeconds = allocatedDurationMinutes * 60L,
                isTimerRunning = true,
                isSubmitDialogOpen = false,
                isExitDialogOpen = false,
                isPaletteDrawerOpen = false,
                isSubmitted = false,
                generatedAttemptId = null
            )

            questionStartTimeMillis = System.currentTimeMillis()
            startTimer()
            _currentScreen.value = AppScreen.CbtExam(testId)
        }
    }

    fun createAndLaunchRevisionTest(
        title: String = "Targeted JEE Weakness Revision Test",
        targetMistakeType: MistakeType? = null,
        targetSubject: Subject? = null,
        durationMinutes: Int = 30
    ) {
        viewModelScope.launch {
            val attempts = _allAttemptsCached()
            val mistakeQuestions = mutableListOf<QuestionItem>()
            val converters = JeeConverters()

            for (att in attempts) {
                val test = repository.getTestById(att.testId) ?: continue
                val qList = repository.parseQuestions(test.questionsJson)
                val respMap = repository.parseResponses(att.responsesJson)

                for (q in qList) {
                    val resp = respMap[q.id]
                    val isMistake = resp?.isCorrect == false || resp?.mistakeCategory != MistakeType.NONE
                    val matchesType = targetMistakeType == null || resp?.mistakeCategory == targetMistakeType
                    val matchesSubj = targetSubject == null || q.subject == targetSubject

                    if (isMistake && matchesType && matchesSubj && mistakeQuestions.none { it.id == q.id }) {
                        mistakeQuestions.add(q)
                    }
                }
            }

            // If empty, fetch from sample pool
            if (mistakeQuestions.isEmpty()) {
                val samples = SampleJeePapers.getSamplePaper2025Jan()
                mistakeQuestions.addAll(if (targetSubject != null) samples.filter { it.subject == targetSubject } else samples.take(15))
            }

            val testId = UUID.randomUUID().toString()
            val phyCount = mistakeQuestions.count { it.subject == Subject.PHYSICS }
            val cheCount = mistakeQuestions.count { it.subject == Subject.CHEMISTRY }
            val matCount = mistakeQuestions.count { it.subject == Subject.MATHEMATICS }

            val revTest = JeeTestEntity(
                testId = testId,
                title = title,
                sourcePdfName = "Mistake_Book_Targeted_Revision.pdf",
                totalQuestions = mistakeQuestions.size,
                durationMinutes = durationMinutes,
                createdAt = System.currentTimeMillis(),
                questionsJson = converters.fromQuestionList(mistakeQuestions),
                isSample = false,
                physicsQuestionsCount = phyCount,
                chemistryQuestionsCount = cheCount,
                mathsQuestionsCount = matCount,
                tags = "Revision Test, Mistake Book, AI Targeted"
            )

            repository.insertTest(revTest, mistakeQuestions)
            startExamWithCustomConfig(testId = testId, customMinutes = durationMinutes)
        }
    }

    private suspend fun _allAttemptsCached(): List<JeeAttemptEntity> {
        return allAttempts.value
    }

    fun setExitDialogOpen(isOpen: Boolean) {
        _examState.value = _examState.value.copy(isExitDialogOpen = isOpen)
    }

    fun saveExamStateAndExit() {
        timerJob?.cancel()
        updateTimeSpentOnCurrentQuestion()
        _examState.value = _examState.value.copy(
            isTimerRunning = false,
            isExitDialogOpen = false
        )
        _currentScreen.value = AppScreen.TestLibrary
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _examState.value.isTimerRunning && _examState.value.remainingTimeSeconds > 0) {
                delay(1000)
                val newTime = _examState.value.remainingTimeSeconds - 1
                _examState.value = _examState.value.copy(remainingTimeSeconds = newTime)
                if (newTime <= 0) {
                    submitExam()
                    break
                }
            }
        }
    }

    fun selectSubject(subject: Subject) {
        updateTimeSpentOnCurrentQuestion()
        val currentState = _examState.value
        val questionsInSubj = currentState.allQuestions.filter { it.subject == subject }
        if (questionsInSubj.isNotEmpty()) {
            val targetQ = questionsInSubj[0]
            markQuestionAsVisited(targetQ.id)
            _examState.value = currentState.copy(
                currentSubject = subject,
                currentQuestionIndexInSubject = 0
            )
        }
    }

    fun selectQuestionByIndexInSubject(index: Int) {
        updateTimeSpentOnCurrentQuestion()
        val currentState = _examState.value
        val questionsInSubj = currentState.allQuestions.filter { it.subject == currentState.currentSubject }
        if (index in questionsInSubj.indices) {
            val targetQ = questionsInSubj[index]
            markQuestionAsVisited(targetQ.id)
            _examState.value = currentState.copy(
                currentQuestionIndexInSubject = index
            )
        }
    }

    fun jumpToQuestionById(questionId: String) {
        updateTimeSpentOnCurrentQuestion()
        val currentState = _examState.value
        val targetQ = currentState.allQuestions.find { it.id == questionId } ?: return
        val questionsInSubj = currentState.allQuestions.filter { it.subject == targetQ.subject }
        val idx = questionsInSubj.indexOfFirst { it.id == questionId }.coerceAtLeast(0)

        markQuestionAsVisited(targetQ.id)
        _examState.value = currentState.copy(
            currentSubject = targetQ.subject,
            currentQuestionIndexInSubject = idx,
            isPaletteDrawerOpen = false
        )
    }

    fun selectMcqOption(optionLetter: String) {
        val currentQ = getCurrentQuestion() ?: return
        val currentResp = _examState.value.responses[currentQ.id] ?: StudentResponse(questionId = currentQ.id)
        val updated = currentResp.copy(
            selectedOption = optionLetter
        )
        val newMap = _examState.value.responses.toMutableMap()
        newMap[currentQ.id] = updated
        _examState.value = _examState.value.copy(responses = newMap)
    }

    fun setNumericalAnswer(answer: String) {
        val currentQ = getCurrentQuestion() ?: return
        val currentResp = _examState.value.responses[currentQ.id] ?: StudentResponse(questionId = currentQ.id)
        val updated = currentResp.copy(
            numericalAnswer = answer
        )
        val newMap = _examState.value.responses.toMutableMap()
        newMap[currentQ.id] = updated
        _examState.value = _examState.value.copy(responses = newMap)
    }

    fun saveAndNext() {
        val currentQ = getCurrentQuestion() ?: return
        updateTimeSpentOnCurrentQuestion()
        val currentResp = _examState.value.responses[currentQ.id] ?: StudentResponse(questionId = currentQ.id)

        val hasAnswer = if (currentQ.type == QuestionType.MCQ) {
            !currentResp.selectedOption.isNullOrBlank()
        } else {
            !currentResp.numericalAnswer.isNullOrBlank()
        }

        val updatedStatus = if (hasAnswer) QuestionStatus.ANSWERED else QuestionStatus.NOT_ANSWERED
        val updated = currentResp.copy(status = updatedStatus)

        val newMap = _examState.value.responses.toMutableMap()
        newMap[currentQ.id] = updated
        _examState.value = _examState.value.copy(responses = newMap)

        moveToNextQuestion()
    }

    fun markForReviewAndNext() {
        val currentQ = getCurrentQuestion() ?: return
        updateTimeSpentOnCurrentQuestion()
        val currentResp = _examState.value.responses[currentQ.id] ?: StudentResponse(questionId = currentQ.id)

        val hasAnswer = if (currentQ.type == QuestionType.MCQ) {
            !currentResp.selectedOption.isNullOrBlank()
        } else {
            !currentResp.numericalAnswer.isNullOrBlank()
        }

        val updatedStatus = if (hasAnswer) {
            QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW
        } else {
            QuestionStatus.MARKED_FOR_REVIEW
        }

        val updated = currentResp.copy(status = updatedStatus)
        val newMap = _examState.value.responses.toMutableMap()
        newMap[currentQ.id] = updated
        _examState.value = _examState.value.copy(responses = newMap)

        moveToNextQuestion()
    }

    fun clearResponse() {
        val currentQ = getCurrentQuestion() ?: return
        val currentResp = _examState.value.responses[currentQ.id] ?: StudentResponse(questionId = currentQ.id)
        val updated = currentResp.copy(
            selectedOption = null,
            numericalAnswer = null,
            status = QuestionStatus.NOT_ANSWERED
        )
        val newMap = _examState.value.responses.toMutableMap()
        newMap[currentQ.id] = updated
        _examState.value = _examState.value.copy(responses = newMap)
    }

    fun previousQuestion() {
        updateTimeSpentOnCurrentQuestion()
        val currentState = _examState.value
        val questionsInSubj = currentState.allQuestions.filter { it.subject == currentState.currentSubject }
        if (currentState.currentQuestionIndexInSubject > 0) {
            val prevIdx = currentState.currentQuestionIndexInSubject - 1
            markQuestionAsVisited(questionsInSubj[prevIdx].id)
            _examState.value = currentState.copy(currentQuestionIndexInSubject = prevIdx)
        } else {
            // Check previous subject
            val subjectOrder = listOf(Subject.PHYSICS, Subject.CHEMISTRY, Subject.MATHEMATICS)
            val currentSubjIdx = subjectOrder.indexOf(currentState.currentSubject)
            if (currentSubjIdx > 0) {
                val prevSubj = subjectOrder[currentSubjIdx - 1]
                val prevSubjQuestions = currentState.allQuestions.filter { it.subject == prevSubj }
                val targetIdx = prevSubjQuestions.size - 1
                if (targetIdx >= 0) {
                    markQuestionAsVisited(prevSubjQuestions[targetIdx].id)
                    _examState.value = currentState.copy(
                        currentSubject = prevSubj,
                        currentQuestionIndexInSubject = targetIdx
                    )
                }
            }
        }
    }

    private fun moveToNextQuestion() {
        val currentState = _examState.value
        val questionsInSubj = currentState.allQuestions.filter { it.subject == currentState.currentSubject }
        if (currentState.currentQuestionIndexInSubject < questionsInSubj.size - 1) {
            val nextIdx = currentState.currentQuestionIndexInSubject + 1
            markQuestionAsVisited(questionsInSubj[nextIdx].id)
            _examState.value = currentState.copy(currentQuestionIndexInSubject = nextIdx)
        } else {
            // Check next subject
            val subjectOrder = listOf(Subject.PHYSICS, Subject.CHEMISTRY, Subject.MATHEMATICS)
            val currentSubjIdx = subjectOrder.indexOf(currentState.currentSubject)
            if (currentSubjIdx < subjectOrder.size - 1) {
                val nextSubj = subjectOrder[currentSubjIdx + 1]
                val nextSubjQuestions = currentState.allQuestions.filter { it.subject == nextSubj }
                if (nextSubjQuestions.isNotEmpty()) {
                    markQuestionAsVisited(nextSubjQuestions[0].id)
                    _examState.value = currentState.copy(
                        currentSubject = nextSubj,
                        currentQuestionIndexInSubject = 0
                    )
                }
            }
        }
    }

    fun setSubmitDialogOpen(isOpen: Boolean) {
        _examState.value = _examState.value.copy(isSubmitDialogOpen = isOpen)
    }

    fun togglePaletteDrawer(isOpen: Boolean) {
        _examState.value = _examState.value.copy(isPaletteDrawerOpen = isOpen)
    }

    fun submitExam() {
        timerJob?.cancel()
        updateTimeSpentOnCurrentQuestion()
        val test = _examState.value.test ?: return
        val questions = _examState.value.allQuestions
        val responses = _examState.value.responses
        val durationTaken = (test.durationMinutes * 60L) - _examState.value.remainingTimeSeconds

        viewModelScope.launch {
            val (attemptEntity, forensicReport) = repository.evaluateAndSaveAttempt(
                test = test,
                questions = questions,
                rawResponses = responses,
                durationSeconds = durationTaken
            )

            _examState.value = _examState.value.copy(
                isTimerRunning = false,
                isSubmitDialogOpen = false,
                isSubmitted = true,
                generatedAttemptId = attemptEntity.attemptId
            )

            _currentAttempt.value = attemptEntity
            _currentForensicReport.value = forensicReport

            navigateTo(AppScreen.ForensicAnalysis(attemptEntity.attemptId))
        }
    }

    fun loadAttempt(attemptId: String) {
        viewModelScope.launch {
            val attempt = repository.getAttemptById(attemptId)
            _currentAttempt.value = attempt
            if (attempt != null) {
                _currentForensicReport.value = repository.parseForensicReport(attempt.forensicReportJson)
            }
        }
    }

    fun selectQuestionForDetail(question: QuestionItem?) {
        _selectedAnalysisQuestion.value = question
    }

    private fun getCurrentQuestion(): QuestionItem? {
        val state = _examState.value
        val questionsInSubj = state.allQuestions.filter { it.subject == state.currentSubject }
        return questionsInSubj.getOrNull(state.currentQuestionIndexInSubject)
    }

    private fun markQuestionAsVisited(questionId: String) {
        val currentResp = _examState.value.responses[questionId]
        if (currentResp == null || currentResp.status == QuestionStatus.NOT_VISITED) {
            val updated = (currentResp ?: StudentResponse(questionId = questionId)).copy(
                status = QuestionStatus.NOT_ANSWERED
            )
            val newMap = _examState.value.responses.toMutableMap()
            newMap[questionId] = updated
            _examState.value = _examState.value.copy(responses = newMap)
        }
        questionStartTimeMillis = System.currentTimeMillis()
    }

    private fun updateTimeSpentOnCurrentQuestion() {
        val currentQ = getCurrentQuestion() ?: return
        val elapsedSec = ((System.currentTimeMillis() - questionStartTimeMillis) / 1000).toInt()
        val currentResp = _examState.value.responses[currentQ.id] ?: StudentResponse(questionId = currentQ.id)
        val updated = currentResp.copy(
            timeSpentSeconds = currentResp.timeSpentSeconds + elapsedSec
        )
        val newMap = _examState.value.responses.toMutableMap()
        newMap[currentQ.id] = updated
        _examState.value = _examState.value.copy(responses = newMap)
        questionStartTimeMillis = System.currentTimeMillis()
    }

    private fun JeeRepository.convertersFromQuestions(list: List<QuestionItem>): String {
        return com.example.data.local.JeeConverters().fromQuestionList(list)
    }
}
