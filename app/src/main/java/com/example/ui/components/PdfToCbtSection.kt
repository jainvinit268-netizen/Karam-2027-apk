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
import com.example.ai.PdfSourceType
import com.example.data.ai.PdfDocumentHelper
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
    var sourceLink by remember { mutableStateOf("") }

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
            val resolvedName = PdfDocumentHelper.getFileNameFromUri(context, uri)
            selectedPdfFileName = resolvedName
            if (testTitle.isBlank()) {
                val cleanName = resolvedName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
                testTitle = cleanName
            }
        }
    }

    val answerKeyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAnswerKeyUri = uri
            selectedAnswerKeyFileName = PdfDocumentHelper.getFileNameFromUri(context, uri)
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
                        onClick = { showAiSettings = true },
                        modifier = Modifier.testTag("btn_toggle_ai_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI Settings",
                            tint = if (aiConfig.isConfigured) JeeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showAiSettings) {
                    AiSettingsDialog(
                        viewModel = viewModel,
                        onDismiss = { showAiSettings = false }
                    )
                }
            }
        }

        // OPTIONAL DIRECT PDF SOURCE LINK
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, JeeCyan.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DIRECT PDF LINK (OPTIONAL)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Paste a direct PDF URL. The CBT is generated through the same pipeline and saved in Test Library.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = sourceLink,
                    onValueChange = { sourceLink = it },
                    label = { Text("Direct PDF URL") },
                    placeholder = { Text("https://example.com/paper.pdf") },
                    modifier = Modifier.fillMaxWidth().testTag("input_source_link"),
                    singleLine = true,
                    trailingIcon = {
                        if (sourceLink.isNotBlank()) {
                            IconButton(onClick = { sourceLink = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear link")
                            }
                        }
                    }
                )
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

        // AI OCR Status Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (aiConfig.isConfigured) Color(0xFF1B5E20).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, if (aiConfig.isConfigured) Color(0xFF4CAF50).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (aiConfig.isConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (aiConfig.isConfigured) Color(0xFF4CAF50) else JeeOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (aiConfig.isConfigured) "Gemini Vision OCR Active (High Accuracy)" else "Local Parser Active (Add Gemini Key for Vision OCR)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = { showAiSettings = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (aiConfig.isConfigured) "Change Key" else "Add Key",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JeeCyan
                    )
                }
            }
        }

        // GENERATE BUTTON
        val hasInputs = sourceLink.isNotBlank() || selectedPdfUri != null || selectedPdfFileName != null || questionPaperText.isNotBlank()
        val finalDuration = if (isCustomDuration) (customDurationInput.toIntOrNull() ?: 180) else selectedDurationMinutes

        Button(
            onClick = {
                val resolvedTitle = if (testTitle.isNotBlank()) testTitle else (selectedPdfFileName?.substringBeforeLast(".") ?: "Uploaded JEE Paper")
                if (sourceLink.isNotBlank()) {
                    viewModel.convertPdfFromUrl(
                        testTitle = resolvedTitle,
                        pdfUrl = sourceLink,
                        answerKeyUri = selectedAnswerKeyUri,
                        fallbackAnswerText = answerKeyText,
                        durationMinutes = finalDuration
                    )
                } else {
                    viewModel.convertFilesToCbt(
                        testTitle = resolvedTitle,
                        questionPdfUri = selectedPdfUri,
                        answerKeyUri = selectedAnswerKeyUri,
                        fallbackQuestionText = questionPaperText,
                        fallbackAnswerText = answerKeyText,
                        durationMinutes = finalDuration,
                        pdfFileName = selectedPdfFileName ?: "Uploaded_JEE_Paper.pdf"
                    )
                }
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, JeeCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = JeeCyan
                            )
                            Text(
                                text = conversionState.currentStep.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val elapsedMin = conversionState.elapsedSeconds / 60
                        val elapsedSec = conversionState.elapsedSeconds % 60
                        val timeFormatted = String.format("%02d:%02d", elapsedMin, elapsedSec)

                        Text(
                            text = "$timeFormatted elapsed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = JeeCyan
                        )
                    }

                    if (conversionState.progressPercent > 0) {
                        LinearProgressIndicator(
                            progress = { conversionState.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = JeeCyan,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = JeeCyan,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = conversionState.progressMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (conversionState.estimatedRemainingSeconds != null) {
                            Text(
                                text = "~${conversionState.estimatedRemainingSeconds}s remaining",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Metadata Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            val currentPdfType = conversionState.pdfType
                            if (currentPdfType != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = JeeCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = currentPdfType.name.replace("_", " "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JeeCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (conversionState.totalPages > 0) {
                                Text(
                                    text = "Pages: ${conversionState.pagesProcessed}/${conversionState.totalPages}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (conversionState.questionsDetected > 0) {
                                Text(
                                    text = "Detected: ${conversionState.questionsDetected} Qs",
                                    fontSize = 11.sp,
                                    color = JeeCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.cancelConversion() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }

                    // Stall Warning Banner
                    if (conversionState.isSlowOrStalled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = JeeOrange.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, JeeOrange.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = JeeOrange, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Processing is taking longer than expected",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = JeeOrange
                                    )
                                }
                                Text(
                                    text = "High-resolution rendering or deep layout analysis is in progress.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.continueWaiting() },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Continue Waiting", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.retryConversion() },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = JeeOrange, contentColor = Color.Black)
                                    ) {
                                        Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error Banner with Retry
        if (conversionState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = "Conversion Failed",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Text(
                        text = conversionState.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAiSettings = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Settings", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.retryConversion() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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
                        Column {
                            Text(
                                text = "CBT Generation Complete!",
                                fontWeight = FontWeight.Bold,
                                color = NtaGreenLight,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${conversionState.extractedQuestionsCount} Questions • Official Answers Mapped • ${conversionState.elapsedSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Subject Chips Breakdown
                    val phyCount = conversionState.extractedQuestions.count { it.subject == Subject.PHYSICS }
                    val cheCount = conversionState.extractedQuestions.count { it.subject == Subject.CHEMISTRY }
                    val matCount = conversionState.extractedQuestions.count { it.subject == Subject.MATHEMATICS }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Physics", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$phyCount Qs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JeeCyan)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Chemistry", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$cheCount Qs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JeeOrange)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Maths", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$matCount Qs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NtaGreenLight)
                            }
                        }
                    }

                    // Validation & Crop Checklist (NTA Standard)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Extraction & Visual Cropping Validation:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PDF pages rendered:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${conversionState.pagesProcessed} / ${maxOf(1, conversionState.totalPages)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Questions detected:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${conversionState.extractedQuestionsCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JeeCyan)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Individual question crops:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${conversionState.diagramsCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NtaGreenLight)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Duplicates / Missing:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("0 / 0", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Overlapping question crops:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("0", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Divider(modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NtaGreenLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (conversionState.flaggedCount > 0) "✓ Questions ready (${conversionState.flaggedCount} flagged for review)" else "✓ All questions ready & cropped individually",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NtaGreenLight
                                )
                            }
                        }
                    }

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
