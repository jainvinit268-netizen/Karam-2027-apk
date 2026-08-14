package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.theme.*
import com.example.ui.viewmodel.JeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToCbtSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val conversionState by viewModel.conversionState.collectAsState()
    val aiConfig by viewModel.aiConfigState.collectAsState()
    val context = LocalContext.current

    var showAiSettings by remember { mutableStateOf(false) }
    var testTitle by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfFileName by remember { mutableStateOf<String?>(null) }

    var selectedAnswerKeyUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAnswerKeyFileName by remember { mutableStateOf<String?>(null) }

    var questionPaperText by remember { mutableStateOf("") }
    var answerKeyText by remember { mutableStateOf("") }

    var selectedDurationMinutes by remember { mutableStateOf(180) }
    var customDurationInput by remember { mutableStateOf("180") }
    var isCustomDuration by remember { mutableStateOf(false) }

    // Document pickers
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            val resolvedName = uri.lastPathSegment?.substringAfterLast("/") ?: "Question_Paper.pdf"
            selectedPdfFileName = resolvedName
            if (testTitle.isBlank()) {
                testTitle = resolvedName.substringBeforeLast(".").replace("_", " ")
            }
        }
    }

    val answerKeyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAnswerKeyUri = uri
            selectedAnswerKeyFileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Answer_Key.pdf"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, JeeCyan.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(JeeBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "PDF → CBT Extraction Engine",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Deep OCR • Layout Parsing • Official Answer Mapping",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { showAiSettings = !showAiSettings },
                        modifier = Modifier.testTag("btn_toggle_ai_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI Settings",
                            tint = if (aiConfig.isConfigured) JeeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // AI Key Config Drawer
                AnimatedVisibility(visible = showAiSettings) {
                    var apiKeyInput by remember { mutableStateOf("") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Gemini AI Configuration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Gemini API Key (Optional)") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    viewModel.saveGeminiApiKey(apiKeyInput.trim())
                                    showAiSettings = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JeeCyan)
                            ) {
                                Text("Save Key", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // STEP 1: QUESTION PAPER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(JeeCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", fontWeight = FontWeight.Bold, color = JeeCyan)
                    }
                    Text(
                        text = "QUESTION PAPER PDF",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                if (selectedPdfFileName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = NtaRedLight)
                            Text(
                                text = selectedPdfFileName!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = {
                            selectedPdfUri = null
                            selectedPdfFileName = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove File", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            pdfPickerLauncher.launch(arrayOf("application/pdf", "image/*", "text/plain"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_upload_question_paper"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedPdfFileName == null) "Upload Question Paper PDF" else "Change File")
                    }
                }

                // Optional text fallback input
                var showTextInput by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { showTextInput = !showTextInput },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        if (showTextInput) "Hide OCR Text Box" else "Or Paste Paper Text / OCR Directly",
                        fontSize = 12.sp,
                        color = JeeCyan
                    )
                }

                AnimatedVisibility(visible = showTextInput) {
                    OutlinedTextField(
                        value = questionPaperText,
                        onValueChange = { questionPaperText = it },
                        label = { Text("Paste Question Paper Text (Optional)") },
                        placeholder = { Text("Q1. A particle of mass m...\n(A) 10 m/s  (B) 20 m/s...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 10
                    )
                }
            }
        }

        // STEP 2: OFFICIAL ANSWER KEY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NtaGreenLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", fontWeight = FontWeight.Bold, color = NtaGreenLight)
                    }
                    Text(
                        text = "OFFICIAL ANSWER KEY",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                if (selectedAnswerKeyFileName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NtaGreenLight)
                            Text(
                                text = selectedAnswerKeyFileName!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = {
                            selectedAnswerKeyUri = null
                            selectedAnswerKeyFileName = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Key", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            answerKeyPickerLauncher.launch(arrayOf("application/pdf", "text/plain", "image/*"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_upload_answer_key"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedAnswerKeyFileName == null) "Upload Official Answer Key" else "Change Key File")
                    }
                }

                // Optional text fallback input
                var showKeyTextInput by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { showKeyTextInput = !showKeyTextInput },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        if (showKeyTextInput) "Hide Key Text Box" else "Or Paste Answer Key Text (e.g. Q1: B, Q2: A...)",
                        fontSize = 12.sp,
                        color = JeeCyan
                    )
                }

                AnimatedVisibility(visible = showKeyTextInput) {
                    OutlinedTextField(
                        value = answerKeyText,
                        onValueChange = { answerKeyText = it },
                        label = { Text("Answer Key (e.g. 1-A, 2-B, 21-45.5...)") },
                        placeholder = { Text("Q1: A\nQ2: B\nQ21: 4.5\nQ51: C") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 8
                    )
                }
            }
        }

        // STEP 3: TEST DETAILS & CONFIGURATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "TEST DETAILS & TIMER",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                OutlinedTextField(
                    value = testTitle,
                    onValueChange = { testTitle = it },
                    label = { Text("Test Name") },
                    placeholder = { Text("e.g. JEE Main 2026 Part Test / Full Mock 01") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_test_title"),
                    singleLine = true
                )

                // Duration Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Exam Duration: ${if (isCustomDuration) "$customDurationInput min (Custom)" else "$selectedDurationMinutes min"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val durationPresets = listOf(10, 30, 60, 90, 120, 180)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(durationPresets) { d ->
                            val isSelected = !isCustomDuration && selectedDurationMinutes == d
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    isCustomDuration = false
                                    selectedDurationMinutes = d
                                },
                                label = { Text("${d}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JeeCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = isCustomDuration,
                                onClick = { isCustomDuration = true },
                                label = { Text("Custom") }
                            )
                        }
                    }

                    if (isCustomDuration) {
                        OutlinedTextField(
                            value = customDurationInput,
                            onValueChange = { input ->
                                customDurationInput = input.filter { ch -> ch.isDigit() }
                                selectedDurationMinutes = customDurationInput.toIntOrNull() ?: 180
                            },
                            label = { Text("Custom Duration in Minutes") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // GENERATE BUTTON
        val hasInputs = selectedPdfUri != null || selectedPdfFileName != null || questionPaperText.isNotBlank()
        val finalDuration = if (isCustomDuration) (customDurationInput.toIntOrNull() ?: 180) else selectedDurationMinutes

        Button(
            onClick = {
                val resolvedTitle = if (testTitle.isNotBlank()) testTitle else (selectedPdfFileName?.substringBeforeLast(".") ?: "Uploaded JEE Paper")
                viewModel.convertFilesToCbt(
                    testTitle = resolvedTitle,
                    questionPdfUri = selectedPdfUri,
                    answerKeyUri = selectedAnswerKeyUri,
                    fallbackQuestionText = questionPaperText,
                    fallbackAnswerText = answerKeyText,
                    durationMinutes = finalDuration,
                    pdfFileName = selectedPdfFileName ?: "Uploaded_JEE_Paper.pdf"
                )
            },
            enabled = hasInputs && !conversionState.isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_generate_cbt"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JeeCyan,
                contentColor = Color.Black,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (conversionState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.Black,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Generating CBT...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE CBT TEST", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Processing / Progress indicator
        if (conversionState.isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = JeeCyan)
                    Text(
                        text = conversionState.progressMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error Banner
        if (conversionState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NtaRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, NtaRedLight)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = NtaRedLight)
                    Text(
                        text = conversionState.errorMessage!!,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Extraction Success Result Card
        if (conversionState.extractedQuestionsCount > 0 && conversionState.newlyCreatedTestId != null) {
            val testId = conversionState.newlyCreatedTestId!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NtaGreenLight.copy(alpha = 0.12f)),
                border = BorderStroke(1.5.dp, NtaGreenLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NtaGreenLight)
                        Text(
                            text = "CBT Generation Complete!",
                            fontWeight = FontWeight.Bold,
                            color = NtaGreenLight,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = "Successfully extracted ${conversionState.extractedQuestionsCount} questions and mapped official answers.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!conversionState.aiStatusMessage.isNullOrBlank()) {
                        Text(
                            text = conversionState.aiStatusMessage.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleReviewModal(true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Preview Questions")
                        }

                        Button(
                            onClick = {
                                viewModel.startExamWithCustomConfig(
                                    testId = testId,
                                    customMinutes = finalDuration
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_start_cbt_now"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black)
                        ) {
                            Text("START CBT NOW", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Question Verification Modal
    if (conversionState.isReviewModalOpen && conversionState.extractedQuestions.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleReviewModal(false) },
            title = {
                Text(
                    text = "Extracted Questions Preview (${conversionState.extractedQuestions.size})",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    conversionState.extractedQuestions.forEach { q ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Q${q.questionNumber} • ${q.subject.displayName} (${q.type.name})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Ans: ${q.correctAnswer}", fontWeight = FontWeight.Bold, color = NtaGreenLight, fontSize = 12.sp)
                                }
                                Text(q.questionText, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleReviewModal(false)
                        val id = conversionState.newlyCreatedTestId
                        if (id != null) {
                            viewModel.startExamWithCustomConfig(
                                testId = id,
                                customMinutes = if (isCustomDuration) (customDurationInput.toIntOrNull() ?: 180) else selectedDurationMinutes
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black)
                ) {
                    Text("Take Test", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleReviewModal(false) }) {
                    Text("Close")
                }
            }
        )
    }
}
