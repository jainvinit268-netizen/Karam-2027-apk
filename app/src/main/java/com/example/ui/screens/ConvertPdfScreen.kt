package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.ai.AiKeySource
import com.example.ui.components.AiSettingsDialog
import com.example.ui.components.AiStatusTopBarChip
import com.example.ui.components.UserAccountTopBarAction
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertPdfScreen(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val conversionState by viewModel.conversionState.collectAsState()
    val aiConfig by viewModel.aiConfigState.collectAsState()
    val context = LocalContext.current
    var showAiSettings by remember { mutableStateOf(false) }

    var testTitle by remember { mutableStateOf("JEE Main 2025 Jan Session Paper 1") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfFileName by remember { mutableStateOf("JEE_Main_2025_Paper1.pdf") }

    var questionPaperText by remember {
        mutableStateOf(
            """[PHYSICS]
Q1. A solid sphere of mass M and radius R rolls without slipping down an inclined plane of inclination θ. The acceleration of the sphere down the plane is:
(A) g sin θ  (B) 5/7 g sin θ  (C) 2/3 g sin θ  (D) 3/5 g sin θ
Q2. A current of 5 A flows through a copper wire of cross-sectional area 2 × 10⁻⁶ m². If electron density is 8.5 × 10²⁸ m⁻³, find drift velocity.
(A) 1.84 × 10⁻⁴ m/s  (B) 3.68 × 10⁻⁴ m/s  (C) 0.92 × 10⁻⁴ m/s  (D) 5.12 × 10⁻⁴ m/s
Q21. A projectile is launched from ground level with speed 50 m/s at angle 37°. Time taken in seconds to reach max height is:

[CHEMISTRY]
Q26. For a first order reaction A -> Products, half life is 20 min. Time taken for 75% completion in minutes is:
(A) 40  (B) 60  (C) 30  (D) 80
Q27. Which compound gives positive Iodoform and Silver Mirror test?
(A) CH3CHO  (B) CH3COCH3  (C) HCHO  (D) CH3CH2OH
Q46. The total number of lone pairs of electrons in XeF2 is:

[MATHEMATICS]
Q51. The value of definite integral ∫[0 to π/2] (sin⁴x)/(sin⁴x + cos⁴x) dx is:
(A) π/4  (B) π/2  (C) π/8  (D) 1
Q52. If A is a 3x3 matrix with |A| = 4, the value of |adj(2A)| is:
(A) 1024  (B) 256  (C) 512  (D) 64
Q71. Let f(x) = x³ - 3x² + 6x + 7. The number of real roots of f'(x) = 0 is:"""
        )
    }

    var answerKeyText by remember {
        mutableStateOf(
            """Q1: B
Q2: A
Q3: A
Q4: A
Q5: A
Q6: B
Q21: 3
Q22: 13
Q26: A
Q27: A
Q28: A
Q29: A
Q46: 9
Q51: A
Q52: A
Q53: A
Q54: A
Q71: 0
Q72: 72"""
        )
    }

    // PDF Picker Launcher
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Uploaded_Question_Paper.pdf"
            selectedPdfFileName = fileName
            if (testTitle.isBlank() || testTitle.startsWith("JEE Main")) {
                testTitle = "JEE Main CBT (${fileName.substringBeforeLast(".")})"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI PDF → CBT Converter",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.resetConversionState()
                            viewModel.navigateTo(AppScreen.TestLibrary)
                        },
                        modifier = Modifier.testTag("back_button_convert")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AiStatusTopBarChip(viewModel = viewModel)
                    UserAccountTopBarAction(viewModel = viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Engine Configuration Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (aiConfig.isConfigured) JeeCyan.copy(alpha = 0.12f) else Color(0xFFFFB74D).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (aiConfig.isConfigured) JeeCyan.copy(alpha = 0.4f) else Color(0xFFFFB74D)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_engine_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (aiConfig.isConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (aiConfig.isConfigured) JeeCyan else Color(0xFFE65100),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (aiConfig.isConfigured) "Gemini AI Engine: Active" else "Gemini AI: Not Configured",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (aiConfig.isConfigured) JeeCyan else Color(0xFFE65100)
                            )
                            Text(
                                text = if (aiConfig.isConfigured) "Source: ${aiConfig.maskedKey}" else "Will use local regex extraction. Tap configure to add Gemini key.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showAiSettings = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("configure_ai_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (aiConfig.isConfigured) "Manage" else "Configure",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Header Instructions Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(JeeCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = JeeCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Automatic NTA CBT Ingestion",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = JeeCyan
                        )
                        Text(
                            text = "Upload question paper PDF + official answer key. AI maps questions, options, numericals, formulas, solutions, and marking keys automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Test Title Field
            OutlinedTextField(
                value = testTitle,
                onValueChange = { testTitle = it },
                label = { Text("Test Name / Examination Session") },
                placeholder = { Text("e.g. JEE Main 2025 Jan 24 Shift 1") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_title_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) }
            )

            // 1. PDF Upload Area
            Text(
                text = "1. Question Paper PDF / Document",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pdfPickerLauncher.launch("application/pdf") }
                    .testTag("pdf_upload_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPdfUri != null) JeeBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selectedPdfUri != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (selectedPdfUri != null) NtaGreenLight else JeeCyan,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedPdfUri != null) selectedPdfFileName else "Select / Upload Question Paper PDF",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedPdfUri != null) "PDF Ready for AI Parsing" else "Tap to choose PDF from device or use text mode below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Text/OCR Editor Toggle
            OutlinedTextField(
                value = questionPaperText,
                onValueChange = { questionPaperText = it },
                label = { Text("Question Paper Text / OCR Extracted Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("question_text_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Official Answer Key Input
            Text(
                text = "2. Official Answer Key (Table or Text)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = answerKeyText,
                onValueChange = { answerKeyText = it },
                label = { Text("Answer Key (e.g. Q1: B, Q2: A, Q21: 3)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("answer_key_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
            )

            // Conversion Status / Success Box
            AnimatedVisibility(visible = conversionState.isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JeeBlue.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = JeeCyan,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Gemini AI Ingestion in Progress",
                                fontWeight = FontWeight.Bold,
                                color = JeeCyan
                            )
                            Text(
                                text = conversionState.progressMessage.ifBlank { "Extracting questions and aligning answer keys..." },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Success Card
            AnimatedVisibility(visible = conversionState.newlyCreatedTestId != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NtaGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NtaGreenLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CBT Test Generated Successfully!",
                                fontWeight = FontWeight.Bold,
                                color = NtaGreenLight,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Extracted ${conversionState.extractedQuestionsCount} questions across Physics, Chemistry, and Maths. Official marking scheme (+4, -1) configured.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (conversionState.flaggedCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ ${conversionState.flaggedCount} uncertain questions auto-checked with default answer keys.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JeeAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val id = conversionState.newlyCreatedTestId!!
                                    viewModel.resetConversionState()
                                    viewModel.navigateTo(AppScreen.CbtExam(id))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NtaGreenLight, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("start_now_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch CBT Exam", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetConversionState()
                                    viewModel.navigateTo(AppScreen.TestLibrary)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Go to Library")
                            }
                        }
                    }
                }
            }

            // Error Card
            AnimatedVisibility(visible = conversionState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NtaRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = NtaRedLight)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = conversionState.errorMessage ?: "Conversion failed.",
                            color = NtaRedLight,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Convert Action Button
            Button(
                onClick = {
                    viewModel.convertPdfToCbt(
                        testTitle = testTitle,
                        questionPaperText = questionPaperText,
                        answerKeyText = answerKeyText,
                        pdfFileName = selectedPdfFileName
                    )
                },
                enabled = !conversionState.isProcessing && questionPaperText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("convert_action_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JeeCyan,
                    contentColor = JeeNavyDark
                )
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (conversionState.isProcessing) "Converting with AI..." else "Auto-Convert to NTA CBT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showAiSettings) {
        AiSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showAiSettings = false }
        )
    }
}
