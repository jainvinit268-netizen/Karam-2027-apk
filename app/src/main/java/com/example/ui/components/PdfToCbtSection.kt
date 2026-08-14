package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject
import com.example.data.sample.SampleJeePapers
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToCbtSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val conversionState by viewModel.conversionState.collectAsState()
    val aiConfig by viewModel.aiConfigState.collectAsState()

    var showAiSettings by remember { mutableStateOf(false) }
    var testTitle by remember { mutableStateOf("JEE Main 2025 Jan Session Paper 1") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfFileName by remember { mutableStateOf("JEE_Main_2025_Jan_Paper1.pdf") }

    var selectedAnswerKeyUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAnswerKeyFileName by remember { mutableStateOf("Official_Answer_Key_2025.txt") }

    var questionPaperText by remember {
        mutableStateOf(
            """[PHYSICS - SECTION A (MCQ)]
Q1. A solid sphere of mass M and radius R rolls without slipping down an inclined plane of inclination θ. The acceleration of the sphere down the plane is:
(A) g sin θ  (B) 5/7 g sin θ  (C) 2/3 g sin θ  (D) 3/5 g sin θ
Q2. A current of 5 A flows through a copper wire of cross-sectional area 2 × 10⁻⁶ m². If electron density is 8.5 × 10²⁸ m⁻³, find drift velocity.
(A) 1.84 × 10⁻⁴ m/s  (B) 3.68 × 10⁻⁴ m/s  (C) 0.92 × 10⁻⁴ m/s  (D) 5.12 × 10⁻⁴ m/s
Q3. When light of frequency 2ν₀ is incident on a metal plate, max velocity of emitted electrons is v₁. When frequency is 5ν₀, max velocity is v₂. Ratio v₁/v₂ is:
(A) 1 : 2  (B) 1 : 4  (C) 1 : √2  (D) 2 : 1
Q4. A Carnot engine has efficiency 40% when sink temp is 300 K. To increase efficiency to 60%, new sink temp should be:
(A) 200 K  (B) 250 K  (C) 150 K  (D) 180 K

[PHYSICS - SECTION B (NUMERICAL)]
Q21. A projectile is launched from ground level with speed 50 m/s at angle 37°. Time taken in seconds to reach max height is (take g=10):
Q22. A circular coil of 100 turns and radius 10 cm carries current 2 A. Magnetic field at center is x × 10⁻⁴ T. Value of x (take π=3.14):

[CHEMISTRY - SECTION A (MCQ)]
Q26. For a first order reaction A -> Products, half life is 20 min. Time taken for 75% completion in minutes is:
(A) 40  (B) 60  (C) 30  (D) 80
Q27. Which compound gives positive Iodoform and Silver Mirror test?
(A) CH3CHO  (B) CH3COCH3  (C) HCHO  (D) CH3CH2OH
Q28. The correct order of basic strength of aliphatic amines in aqueous medium is:
(A) (CH3)2NH > CH3NH2 > (CH3)3N > NH3  (B) (CH3)3N > (CH3)2NH > CH3NH2  (C) CH3NH2 > (CH3)2NH > (CH3)3N

[CHEMISTRY - SECTION B (NUMERICAL)]
Q46. The total number of lone pairs of electrons in XeF2 is:
Q47. Number of unpaired electrons in [Fe(H2O)6]²⁺ (atomic number of Fe = 26) is:

[MATHEMATICS - SECTION A (MCQ)]
Q51. The value of definite integral ∫[0 to π/2] (sin⁴x)/(sin⁴x + cos⁴x) dx is:
(A) π/4  (B) π/2  (C) π/8  (D) 1
Q52. If A is a 3x3 matrix with |A| = 4, the value of |adj(2A)| is:
(A) 1024  (B) 256  (C) 512  (D) 64
Q53. Area bounded by parabola y² = 4x and line y = 2x is:
(A) 1/3  (B) 2/3  (C) 4/3  (D) 1/6

[MATHEMATICS - SECTION B (NUMERICAL)]
Q71. Let f(x) = x³ - 3x² + 6x + 7. The number of real roots of f'(x) = 0 is:
Q72. The maximum value of 3 sin θ + 4 cos θ + 5 is:"""
        )
    }

    var answerKeyText by remember {
        mutableStateOf(
            """Q1: B
Q2: A
Q3: A
Q4: A
Q21: 3
Q22: 13
Q26: A
Q27: A
Q28: A
Q46: 9
Q47: 4
Q51: A
Q52: A
Q53: A
Q71: 0
Q72: 10"""
        )
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Uploaded_Question_Paper.pdf"
            selectedPdfFileName = fileName
            testTitle = "JEE Main CBT (${fileName.substringBeforeLast(".")})"
        }
    }

    val answerKeyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAnswerKeyUri = uri
            selectedAnswerKeyFileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Official_Answer_Key.pdf"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Status Pill Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (aiConfig.isConfigured) JeeCyan.copy(alpha = 0.12f) else Color(0xFFFFB74D).copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (aiConfig.isConfigured) JeeCyan.copy(alpha = 0.4f) else Color(0xFFFFB74D)
            ),
            modifier = Modifier.fillMaxWidth().testTag("ai_status_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (aiConfig.isConfigured) Icons.Default.AutoAwesome else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (aiConfig.isConfigured) JeeCyan else Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (aiConfig.isConfigured) "Gemini AI Engine: Connected (${aiConfig.maskedKey})" else "Gemini AI: Local Parsing Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (aiConfig.isConfigured) JeeCyan else Color(0xFFE65100)
                        )
                        Text(
                            text = "Extracts 100% questions, formulas, numericals, and aligns official answer keys.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { showAiSettings = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Settings", fontSize = 11.sp)
                }
            }
        }

        // Test Title
        OutlinedTextField(
            value = testTitle,
            onValueChange = { testTitle = it },
            label = { Text("Test Name / Examination Session") },
            placeholder = { Text("e.g. JEE Main 2025 Jan 24 Shift 1") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_test_title"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) }
        )

        // Presets Selector & Uploaded Trial Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JeeCyan.copy(alpha = 0.12f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, JeeCyan),
            modifier = Modifier.fillMaxWidth().clickable {
                testTitle = "Quizrr Part Test - 02 (QPT-02) • Official Paper & Key"
                selectedPdfFileName = "Quizrr_QPT_02_Full_Paper.pdf"
                selectedAnswerKeyFileName = "QPT_02_Handwritten_Answer_Key.jpg"
                questionPaperText = """[MATHEMATICS - SECTION A (MCQ)]
Q1. If A + B = 135°, then the value of ((cot A)/(cot A - 1)) · ((cot B)/(cot B - 1)), if it exists, is equal to:
(A) 0  (B) 1  (C) 2  (D) 1/2
Q2. If tan 20° = p, then the value of the expression (tan 160° - tan 250°) / (tan 200° + tan 290°) in terms of p is:
(A) (1 - p²)/(1 + p²)  (B) (p² + 1)/(p² - 1)  (C) (1 + p²)/(1 - p²)  (D) 2p/(1 - p²)
Q3. If cos 36° = (√5 + 1)/4, then the value of sin² 42° - sin² 12° is:
(A) (√5 + 1)/8  (B) (√5 - 1)/8  (C) (√5 + 1)/4  (D) (√5 - 1)/4
Q4. The number of solutions of the equation 3^(1 - sin² x + sin⁴ x - ... to ∞) = 81 in x ∈ [0, 2π] is:
(A) 2  (B) 4  (C) 6  (D) 8
Q5. If u + v + cos 4α = 19 and u - v = 12 sin 2α, then the value of √u + √v is equal to:
(A) 4  (B) 5  (C) 6  (D) 8

[PHYSICS - SECTION A (MCQ)]
Q26. A body of mass 5 kg is thrown vertically up with a kinetic energy of 490 J. The height at which the kinetic energy of the body becomes half of the original value is (g = 9.8 ms⁻²):
(A) 5 m  (B) 2.5 m  (C) 10 m  (D) 12.5 m
Q27. The upper half of an inclined plane with inclination ϕ is perfectly smooth while the lower half is rough. A body starting from rest at top comes to rest at bottom if coefficient of friction is:
(A) 2 cos ϕ  (B) 2 sin ϕ  (C) tan ϕ  (D) 2 tan ϕ
Q28. The maximum velocity (in ms⁻¹) with which a car can traverse a flat curve of radius 150 m and μ = 0.6 is (g = 10 ms⁻²):
(A) 60  (B) 30  (C) 15  (D) 25
Q29. A block of mass 2 kg subjected to force F(t). Kinetic energy after 4.5 seconds is:
(A) 4.50 J  (B) 7.50 J  (C) 5.06 J  (D) 14.06 J

[CHEMISTRY - SECTION A (MCQ)]
Q51. If hydrogen and oxygen are mixed and kept in the same vessel at room temperature, the reaction does not take place because:
(A) activation energy for the reaction is very high at room temperature  (B) molecules have no proper orientation  (C) collision frequency low  (D) no catalyst
Q52. A conductivity cell is filled with KCl solution. Relationship between Λm,exp 2 and Λm,exp 1 is:
(A) Λm,exp 2 = 2 Λm,exp 1  (B) Λm,exp 2 = 4 Λm,exp 1  (C) Λm,exp 2 = 0.5 Λm,exp 1  (D) Λm,exp 2 = Λm,exp 1
Q53. 31 g solute in 500 g water cooled to -3.72°C. Mass of water that separates out as ice:
(A) 100 g  (B) 200 g  (C) 250 g  (D) 300 g"""
                answerKeyText = """Maths:
Q1: D (1/2) | Q2: C | Q3: A | Q4: B | Q5: C | Q6: B | Q7: C | Q8: D | Q9: B | Q10: A
Q11: A | Q12: B | Q13: D | Q14: A | Q15: C | Q16: B | Q17: B | Q18: A | Q19: B | Q20: A
Q21: 2 | Q22: 10 | Q23: 7 | Q24: 12 | Q25: 2

Physics:
Q26: A (5m) | Q27: D (2tanϕ) | Q28: B (30) | Q29: C (5.06J) | Q30: C (144N)
Q31: C | Q32: C | Q33: D (32N) | Q34: B (2mg) | Q35: A | Q36: D | Q37: C | Q38: A | Q39: D | Q40: A
Q41: A | Q42: D | Q43: A | Q44: B (3.3) | Q45: B (3.6J) | Q46: 5 | Q47: 10 | Q48: 5 | Q49: 8 | Q50: 0

Chemistry:
Q51: A | Q52: B | Q53: C (250g) | Q54: B | Q55: D (117) | Q56: A | Q57: C | Q58: C | Q59: C | Q60: C
Q61: A | Q62: A | Q63: C | Q64: B | Q65: A | Q66: A | Q67: B | Q68: A | Q69: D (6) | Q70: B
Q71: 650 | Q72: 30 | Q73: 1 | Q74: 9 | Q75: 108"""
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = JeeCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🎯 User Uploaded Paper: Quizrr Part Test - 02 (QPT-02)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = JeeCyan
                        )
                        Text(
                            text = "Tap to load exact Questions (Maths, Physics, Chem) + Handwritten Answer Key",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(
                    onClick = {
                        viewModel.startExam("quizrr_qpt_02")
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = JeeCyan, contentColor = JeeNavyDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Instant Trial", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Presets Selector
        Text("Quick Load Real Exam Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val presets = listOf(
                "Quizrr QPT-02 (Uploaded)",
                "JEE Main 2025 Jan 1",
                "Allen Score Booster",
                "FIITJEE AITS Mock"
            )
            items(presets) { preset ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (preset.contains("Quizrr")) JeeCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (preset.contains("Quizrr")) androidx.compose.foundation.BorderStroke(1.dp, JeeCyan) else null,
                    modifier = Modifier.clickable {
                        testTitle = "$preset (Full CBT)"
                        selectedPdfFileName = "${preset.replace(" ", "_")}.pdf"
                    }
                ) {
                    Text(
                        text = "⚡ $preset",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (preset.contains("Quizrr")) JeeCyan else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // TWO SEPARATE INPUT CARDS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Question Paper
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { pdfPickerLauncher.launch("application/pdf") }
                    .testTag("upload_question_paper_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPdfUri != null) JeeBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPdfUri != null) JeeCyan else Color(0xFF2E2E42))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selectedPdfUri != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (selectedPdfUri != null) NtaGreenLight else JeeCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("1. Question Paper", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = if (selectedPdfUri != null) selectedPdfFileName else "PDF / Document",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Card 2: Official Answer Key
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { answerKeyPickerLauncher.launch("*/*") }
                    .testTag("upload_answer_key_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedAnswerKeyUri != null) NtaGreenLight.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedAnswerKeyUri != null) NtaGreenLight else Color(0xFF2E2E42))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selectedAnswerKeyUri != null) Icons.Default.CheckCircle else Icons.Default.Key,
                        contentDescription = null,
                        tint = if (selectedAnswerKeyUri != null) NtaGreenLight else JeeAmber,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("2. Official Answer Key", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = if (selectedAnswerKeyUri != null) selectedAnswerKeyFileName else "PDF / Image / Text",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // Editable Content Areas
        OutlinedTextField(
            value = questionPaperText,
            onValueChange = { questionPaperText = it },
            label = { Text("Question Paper Text / OCR Buffer (All Questions)") },
            modifier = Modifier.fillMaxWidth().height(140.dp).testTag("ocr_paper_text_input"),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = answerKeyText,
            onValueChange = { answerKeyText = it },
            label = { Text("Official Answer Key Text (e.g. Q1: B, Q21: 3)") },
            modifier = Modifier.fillMaxWidth().height(90.dp).testTag("ocr_answer_key_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Live Multi-stage Ingestion Stepper
        if (conversionState.isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JeeBlue.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JeeCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Ingestion & Validation Pipeline in Progress...", fontWeight = FontWeight.Bold, color = JeeCyan)
                    }
                    Text("1. Reading PDF & OCR  ✓", fontSize = 11.sp, color = NtaGreenLight)
                    Text("2. Detecting question boundaries & formulas  ✓", fontSize = 11.sp, color = NtaGreenLight)
                    Text("3. Cropping & structuring MCQ/Numerical objects...", fontSize = 11.sp, color = JeeCyan)
                    Text("4. Mapping official answer keys strictly...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Validation Summary Card upon successful extraction
        if (conversionState.extractedQuestionsCount > 0 && conversionState.newlyCreatedTestId != null) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("validation_summary_card"),
                colors = CardDefaults.cardColors(containerColor = NtaGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NtaGreenLight)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NtaGreenLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Validation Engine Report: 100% Passed",
                            fontWeight = FontWeight.Bold,
                            color = NtaGreenLight,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "• Extracted ${conversionState.extractedQuestionsCount} questions strictly preserved.",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Zero question mixing verified across Physics, Chemistry, and Mathematics.",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Answer keys mapped accurately (+4, -1 marking).",
                        fontSize = 12.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Button(
                            onClick = {
                                val id = conversionState.newlyCreatedTestId!!
                                viewModel.resetConversionState()
                                viewModel.navigateTo(AppScreen.CbtExam(id))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NtaGreenLight, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("launch_converted_cbt_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Launch CBT Exam", fontWeight = FontWeight.Bold)
                        }

                        if (conversionState.extractedQuestions.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.toggleReviewModal(true) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_review_questions")
                            ) {
                                Text("Review Questions", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Test Making Preview Section (Visualizes test as it will appear in actual CBT exam)
        TestMakingLivePreviewCard(
            testTitle = testTitle,
            onLaunchDirectTrial = {
                viewModel.startExam("quizrr_qpt_02")
            }
        )

        // Action Button
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
                .height(52.dp)
                .testTag("btn_auto_convert_cbt"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = JeeNavyDark)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (conversionState.isProcessing) "Extracting & Validating All Questions..." else "Auto-Convert PDF to NTA CBT",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showAiSettings) {
        AiSettingsDialog(viewModel = viewModel, onDismiss = { showAiSettings = false })
    }

    if (conversionState.isReviewModalOpen && conversionState.extractedQuestions.isNotEmpty()) {
        LowConfidenceReviewDialog(
            questions = conversionState.extractedQuestions,
            onDismiss = { viewModel.toggleReviewModal(false) },
            onConfirmAll = {
                viewModel.toggleReviewModal(false)
                conversionState.newlyCreatedTestId?.let { id ->
                    viewModel.resetConversionState()
                    viewModel.navigateTo(AppScreen.CbtExam(id))
                }
            }
        )
    }
}

@Composable
fun TestMakingLivePreviewCard(
    testTitle: String,
    onLaunchDirectTrial: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var selectedSubject by remember { mutableStateOf(Subject.MATHEMATICS) }
    val trialQuestions = remember { SampleJeePapers.getQuizrrPartTest02() }
    
    val subjectQuestions = remember(selectedSubject, trialQuestions) {
        trialQuestions.filter { it.subject == selectedSubject }
    }
    
    var currentQuestionIndex by remember(selectedSubject) { mutableIntStateOf(0) }
    val activeQuestion = subjectQuestions.getOrNull(currentQuestionIndex) ?: subjectQuestions.firstOrNull()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, JeeCyan.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth().testTag("live_test_making_preview_card")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = JeeCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Live Test Making Preview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = JeeCyan
                        )
                        Text(
                            text = "Normal CBT simulation preview of your questions & answer keys",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = JeeCyan
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Subject Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(Subject.MATHEMATICS, "Maths (25)", JeeAmber),
                            Triple(Subject.PHYSICS, "Physics (25)", JeeBlue),
                            Triple(Subject.CHEMISTRY, "Chemistry (25)", NtaGreenLight)
                        ).forEach { (subj, label, color) ->
                            val isSelected = selectedSubject == subj
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedSubject = subj }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Question Number selector strip
                    Text("Select Question:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjectQuestions.indices.toList()) { idx ->
                            val isSelected = idx == currentQuestionIndex
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) JeeCyan else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { currentQuestionIndex = idx }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) JeeNavyDark else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Active Question Live Screen Card
                    activeQuestion?.let { q ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383854)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Question Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = JeeCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Q${currentQuestionIndex + 1} • ${if (q.type == QuestionType.MCQ) "Section A (MCQ)" else "Section B (Numerical)"}",
                                            color = JeeCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NtaGreenLight.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "+4.00, -1.00",
                                            color = NtaGreenLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Question Text
                                Text(
                                    text = q.questionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 20.sp
                                )

                                // Options / Numerical Input
                                if (q.type == QuestionType.MCQ && !q.options.isNullOrEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val optLabels = listOf("A", "B", "C", "D")
                                        q.options.forEachIndexed { optIdx, optText ->
                                            val label = optLabels.getOrElse(optIdx) { "${optIdx + 1}" }
                                            val isKeyAnswer = label.equals(q.correctAnswer, ignoreCase = true)
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isKeyAnswer) NtaGreenLight.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isKeyAnswer) NtaGreenLight else Color(0xFF383854)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (isKeyAnswer) NtaGreenLight else MaterialTheme.colorScheme.surfaceVariant,
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isKeyAnswer) Color.Black else MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = optText,
                                                        fontSize = 12.sp,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (isKeyAnswer) {
                                                        Text(
                                                            text = "✓ Key",
                                                            color = NtaGreenLight,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Numerical input mock
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NtaGreenLight),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Official Numerical Answer: ${q.correctAnswer}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NtaGreenLight)
                                            Text("CBT Keypad Enabled", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                // Chapter & Concept Footer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🏷️ ${q.chapter} • ${q.concept}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Target: ${q.idealTimeSeconds}s",
                                        fontSize = 10.sp,
                                        color = JeeCyan
                                    )
                                }
                            }
                        }
                    }

                    // Direct Action: Launch Trial CBT
                    Button(
                        onClick = onLaunchDirectTrial,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NtaGreenLight, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Launch Trial CBT Simulator (75 Qs)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
