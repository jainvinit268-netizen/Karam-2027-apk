package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.StudentResponse
import com.example.data.model.Subject
import com.example.ui.components.ExitTestConfirmDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ExamUiState
import com.example.ui.viewmodel.JeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbtExamScreen(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val examState by viewModel.examState.collectAsState()
    val test = examState.test

    // Intercept hardware/gesture back press to show Exit Test dialog
    BackHandler {
        viewModel.setExitDialogOpen(true)
    }

    val currentSubjectQuestions = remember(examState.allQuestions, examState.currentSubject) {
        examState.allQuestions.filter { it.subject == examState.currentSubject }
    }

    val currentQuestion = currentSubjectQuestions.getOrNull(examState.currentQuestionIndexInSubject)
    val currentResponse = currentQuestion?.let { examState.responses[it.id] }

    val formattedTime = remember(examState.remainingTimeSeconds) {
        val hours = examState.remainingTimeSeconds / 3600
        val minutes = (examState.remainingTimeSeconds % 3600) / 60
        val seconds = examState.remainingTimeSeconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setExitDialogOpen(true) },
                        modifier = Modifier.testTag("btn_cbt_back")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Test",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = test?.title ?: "JEE Main Paper 1 CBT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Candidate: JEE Aspirant (Roll No. 2501001)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Real Countdown Timer Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (examState.remainingTimeSeconds < 600) NtaRed.copy(alpha = 0.2f) else JeeBlue.copy(alpha = 0.25f))
                            .border(
                                1.dp,
                                if (examState.remainingTimeSeconds < 600) NtaRedLight else JeeCyan,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (examState.remainingTimeSeconds < 600) NtaRedLight else JeeCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedTime,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (examState.remainingTimeSeconds < 600) NtaRedLight else JeeCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Question Palette Toggle Button
                    IconButton(
                        onClick = { viewModel.togglePaletteDrawer(true) },
                        modifier = Modifier.testTag("palette_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Question Palette",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // NTA CBT Bottom Action Bar
            CbtBottomActionBar(
                onPrevious = { viewModel.previousQuestion() },
                onClear = { viewModel.clearResponse() },
                onMarkForReview = { viewModel.markForReviewAndNext() },
                onSaveAndNext = { viewModel.saveAndNext() },
                onSubmit = { viewModel.setSubmitDialogOpen(true) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Subject Tabs: Physics, Chemistry, Mathematics
            SubjectTabsRow(
                selectedSubject = examState.currentSubject,
                onSubjectSelected = { viewModel.selectSubject(it) },
                allQuestions = examState.allQuestions,
                responses = examState.responses
            )

            // Current Question Display
            if (currentQuestion != null) {
                QuestionContentView(
                    question = currentQuestion,
                    response = currentResponse,
                    onOptionSelected = { viewModel.selectMcqOption(it) },
                    onNumericalEntered = { viewModel.setNumericalAnswer(it) }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Question Palette Bottom Sheet / Modal
    if (examState.isPaletteDrawerOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.togglePaletteDrawer(false) },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            QuestionPaletteSheet(
                examState = examState,
                onQuestionClick = { qId ->
                    viewModel.jumpToQuestionById(qId)
                }
            )
        }
    }

    // Final Submission Summary Dialog
    if (examState.isSubmitDialogOpen) {
        CbtSubmitDialog(
            examState = examState,
            onDismiss = { viewModel.setSubmitDialogOpen(false) },
            onConfirmSubmit = { viewModel.submitExam() }
        )
    }

    // Exit Test Confirmation Dialog
    if (examState.isExitDialogOpen) {
        ExitTestConfirmDialog(
            onContinueTest = { viewModel.setExitDialogOpen(false) },
            onSaveAndExit = { viewModel.saveExamStateAndExit() }
        )
    }
}

@Composable
private fun SubjectTabsRow(
    selectedSubject: Subject,
    onSubjectSelected: (Subject) -> Unit,
    allQuestions: List<QuestionItem>,
    responses: Map<String, StudentResponse>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Subject.values().forEach { subject ->
            val subQuestions = allQuestions.filter { it.subject == subject }
            val answeredCount = subQuestions.count {
                val resp = responses[it.id]
                resp?.status == QuestionStatus.ANSWERED || resp?.status == QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW
            }

            val isSelected = selectedSubject == subject

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) JeeCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) JeeCyan else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSubjectSelected(subject) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = subject.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) JeeCyan else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$answeredCount / ${subQuestions.size} Ans",
                        fontSize = 11.sp,
                        color = if (answeredCount > 0) NtaGreenLight else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionContentView(
    question: QuestionItem,
    response: StudentResponse?,
    onOptionSelected: (String) -> Unit,
    onNumericalEntered: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question Header Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(JeeBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Q${question.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${question.subject.displayName} • ${question.section}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${question.chapter} • ${question.type.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Marking Scheme Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NtaGreen.copy(alpha = 0.15f))
                    .border(1.dp, NtaGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+4, -1",
                    fontWeight = FontWeight.Bold,
                    color = NtaGreenLight,
                    fontSize = 12.sp
                )
            }
        }

        // Question Statement Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }

        // Answer Section: MCQ Options or Numerical Input
        if (question.type == QuestionType.MCQ) {
            Text(
                text = "Select one correct option:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            val optionLabels = listOf("A", "B", "C", "D")
            question.options.forEachIndexed { index, optionText ->
                val optionLetter = optionLabels.getOrElse(index) { "${index + 1}" }
                val isSelected = response?.selectedOption?.equals(optionLetter, ignoreCase = true) == true

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(optionLetter) }
                        .testTag("option_${optionLetter}_q${question.questionNumber}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) JeeCyan.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) {
                        CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(JeeCyan), width = 2.dp)
                    } else CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(optionLetter) },
                            colors = RadioButtonDefaults.colors(selectedColor = JeeCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Numerical Section Input
            Text(
                text = "Enter your numerical answer (Integer or Decimal):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            var numText by remember(question.id, response?.numericalAnswer) {
                mutableStateOf(response?.numericalAnswer ?: "")
            }

            OutlinedTextField(
                value = numText,
                onValueChange = {
                    numText = it
                    onNumericalEntered(it)
                },
                label = { Text("Numerical Answer Value") },
                placeholder = { Text("e.g. 24 or 3.5") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("numerical_input_q${question.questionNumber}"),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JeeCyan
                )
            )

            // Keypad shortcut buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("-", ".", "Clear").forEach { key ->
                    FilledTonalButton(
                        onClick = {
                            if (key == "Clear") {
                                numText = ""
                                onNumericalEntered("")
                            } else {
                                numText += key
                                onNumericalEntered(numText)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(key, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CbtBottomActionBar(
    onPrevious: () -> Unit,
    onClear: () -> Unit,
    onMarkForReview: () -> Unit,
    onSaveAndNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Row 1: Actions (Previous, Clear, Review)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("btn_previous"),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("Previous", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onClear,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.1f).testTag("btn_clear"),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("Clear Response", fontSize = 12.sp)
                }

                Button(
                    onClick = onMarkForReview,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NtaPurple),
                    modifier = Modifier.weight(1.3f).testTag("btn_mark_review"),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("Mark for Review", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Save & Next and Submit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveAndNext,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NtaGreenLight, contentColor = Color.Black),
                    modifier = Modifier.weight(2f).testTag("btn_save_next")
                ) {
                    Icon(Icons.Default.NavigateNext, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Next", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = onSubmit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = JeeNavyDark),
                    modifier = Modifier.weight(1.2f).testTag("btn_submit_exam")
                ) {
                    Text("Submit Test", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun QuestionPaletteSheet(
    examState: ExamUiState,
    onQuestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "NTA Question Palette",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Official NTA 5-Color Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendItem(color = NtaGreenLight, label = "Answered")
                LegendItem(color = NtaRedLight, label = "Not Answered")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendItem(color = NtaGrayDark, label = "Not Visited")
                LegendItem(color = NtaPurpleLight, label = "Marked for Review")
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                LegendItem(color = NtaPurpleLight, hasEvaluatedDot = true, label = "Ans & Marked for Review (Evaluated)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${examState.currentSubject.displayName} Questions:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val questionsInSubj = examState.allQuestions.filter { it.subject == examState.currentSubject }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
        ) {
            items(questionsInSubj) { q ->
                val resp = examState.responses[q.id]
                val status = resp?.status ?: QuestionStatus.NOT_VISITED

                val bgColor = when (status) {
                    QuestionStatus.ANSWERED -> NtaGreenLight
                    QuestionStatus.NOT_ANSWERED -> NtaRedLight
                    QuestionStatus.MARKED_FOR_REVIEW -> NtaPurpleLight
                    QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW -> NtaPurpleLight
                    QuestionStatus.NOT_VISITED -> NtaGrayDark.copy(alpha = 0.4f)
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .clickable { onQuestionClick(q.id) }
                        .testTag("palette_q_${q.questionNumber}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${q.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    if (status == QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NtaGreenLight)
                                .align(Alignment.BottomEnd)
                                .padding(1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    hasEvaluatedDot: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            if (hasEvaluatedDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NtaGreenLight)
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CbtSubmitDialog(
    examState: ExamUiState,
    onDismiss: () -> Unit,
    onConfirmSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm JEE CBT Submission", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Are you sure you want to submit the exam? Once submitted, you cannot change your responses.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Subject breakdown summary table
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Answered", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NtaGreenLight)
                            Text("Review", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NtaPurpleLight)
                            Text("Not Ans", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NtaRedLight)
                        }
                        Divider()
                        Subject.values().forEach { subj ->
                            val subQs = examState.allQuestions.filter { it.subject == subj }
                            val ans = subQs.count {
                                val st = examState.responses[it.id]?.status
                                st == QuestionStatus.ANSWERED || st == QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW
                            }
                            val rev = subQs.count {
                                examState.responses[it.id]?.status == QuestionStatus.MARKED_FOR_REVIEW
                            }
                            val notAns = subQs.size - ans - rev

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(subj.shortCode, fontSize = 12.sp)
                                Text("$ans", fontSize = 12.sp, color = NtaGreenLight, fontWeight = FontWeight.SemiBold)
                                Text("$rev", fontSize = 12.sp, color = NtaPurpleLight)
                                Text("$notAns", fontSize = 12.sp, color = NtaRedLight)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = NtaGreenLight, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_submit_btn")
            ) {
                Text("Yes, Final Submit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Resume Exam")
            }
        }
    )
}
