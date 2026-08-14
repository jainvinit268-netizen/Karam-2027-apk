package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.JeeAttemptEntity
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForensicReportScreen(
    attemptId: String,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val attempt by viewModel.currentAttempt.collectAsState()
    val report by viewModel.currentForensicReport.collectAsState()
    val selectedQuestion by viewModel.selectedAnalysisQuestion.collectAsState()

    val examState by viewModel.examState.collectAsState()
    val allQuestions = remember(attempt, examState.allQuestions) {
        if (examState.allQuestions.isNotEmpty()) examState.allQuestions else emptyList()
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Overview & Strategy, 1: Subject Deep Dive, 2: Mistake Audit & Questions

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Forensic Score & Leak Audit",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = attempt?.testTitle ?: "JEE Main Performance Analysis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.TestLibrary) },
                        modifier = Modifier.testTag("back_from_report")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Library")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.TestLibrary) }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (report == null || attempt == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = JeeCyan)
            }
        } else {
            val r = report!!
            val att = attempt!!

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Score Hero Banner
                ScoreHeroBanner(report = r, attempt = att)

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Forensic Diagnostic", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("report_tab_diagnostic")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Subject Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("report_tab_subjects")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Question Audit (${r.totalQuestions})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("report_tab_questions")
                    )
                }

                // Tab Content
                when (selectedTab) {
                    0 -> DiagnosticOverviewTab(report = r, onQuestionClick = { qNum ->
                        val targetQ = allQuestions.find { it.questionNumber == qNum }
                        if (targetQ != null) {
                            viewModel.selectQuestionForDetail(targetQ)
                        }
                    })
                    1 -> SubjectDeepDiveTab(report = r)
                    2 -> QuestionAuditTab(
                        allQuestions = allQuestions,
                        attempt = att,
                        onQuestionSelected = { q -> viewModel.selectQuestionForDetail(q) }
                    )
                }
            }
        }
    }

    // Question Detail Modal Sheet
    if (selectedQuestion != null && attempt != null) {
        val q = selectedQuestion!!
        val responses = remember(attempt) {
            val conv = com.example.data.local.JeeConverters()
            conv.toResponseMap(attempt!!.responsesJson)
        }
        val userResp = responses[q.id]

        QuestionDetailDialog(
            question = q,
            response = userResp,
            onDismiss = { viewModel.selectQuestionForDetail(null) }
        )
    }
}

@Composable
private fun ScoreHeroBanner(
    report: ForensicReport,
    attempt: JeeAttemptEntity
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL RAW SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${report.totalScore}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (report.totalScore >= 180) NtaGreenLight else JeeCyan
                        )
                        Text(
                            text = " / ${report.maxPossibleScore}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                }

                // Estimated Percentile Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JeeBlue.copy(alpha = 0.3f))
                        .border(1.dp, JeeCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Est. Percentile", fontSize = 10.sp, color = JeeCyanLight)
                        Text(
                            text = "${String.format("%.2f", report.estimatedPercentile)} %ile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Quick Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniMetric(label = "Attempted", value = "${report.totalAttempted} / ${report.totalQuestions}", color = MaterialTheme.colorScheme.onSurface)
                MiniMetric(label = "Correct (+4)", value = "${report.correctCount}", color = NtaGreenLight)
                MiniMetric(label = "Incorrect (-1)", value = "${report.incorrectCount}", color = NtaRedLight)
                MiniMetric(label = "Negative Leaks", value = "-${report.totalNegativeMarksLost}", color = NtaRed)
                MiniMetric(label = "Accuracy", value = "${String.format("%.1f", report.accuracyPercentage)}%", color = JeeAmber)
            }
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
    }
}

@Composable
private fun DiagnosticOverviewTab(
    report: ForensicReport,
    onQuestionClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // 1. Mistake Anatomy Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = NtaRedLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mistake Anatomy & Mark Leaks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MistakeBar(label = "Silly Mistakes (Sign, unit, misread)", count = report.sillyMistakesCount, color = JeeAmber)
                    MistakeBar(label = "Conceptual Mistakes (Theory/Formula)", count = report.conceptualMistakesCount, color = NtaRedLight)
                    MistakeBar(label = "Calculation Mistakes (Algebraic error)", count = report.calculationMistakesCount, color = JeeOrange)
                    MistakeBar(label = "Wrong Approach / Guesswork", count = report.wrongApproachCount, color = NtaPurpleLight)
                    MistakeBar(label = "Time Traps (>3 min wasted)", count = report.timeTrapsCount, color = NtaRed)
                }
            }
        }

        // 2. Question Selection Recommendations (Should Have Attempted vs Skipped)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Question Selection Precision",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (report.shouldHaveAttemptedQuestions.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = NtaGreenLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Easy Questions You Missed (Free +4 Marks):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NtaGreenLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    report.shouldHaveAttemptedQuestions.take(5).forEach { qNum ->
                                        SuggestionChip(
                                            onClick = { onQuestionClick(qNum) },
                                            label = { Text("Q$qNum", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = NtaGreen.copy(alpha = 0.2f),
                                                labelColor = NtaGreenLight
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (report.shouldHaveSkippedQuestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = NtaRedLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Time Traps You Should Have Skipped:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NtaRedLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    report.shouldHaveSkippedQuestions.take(5).forEach { qNum ->
                                        SuggestionChip(
                                            onClick = { onQuestionClick(qNum) },
                                            label = { Text("Q$qNum", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = NtaRed.copy(alpha = 0.2f),
                                                labelColor = NtaRedLight
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Actionable Revision Plan
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = JeeAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actionable Revision & Next Test Strategy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = report.nextTestStrategy,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (report.revisionActionPlan.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        report.revisionActionPlan.forEach { plan ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", fontWeight = FontWeight.Bold, color = JeeCyan)
                                Text(text = plan, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MistakeBar(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = "$count questions",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (count > 0) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubjectDeepDiveTab(report: ForensicReport) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        items(report.subjectAnalyses) { sub ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub.subject.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (sub.subject) {
                                Subject.PHYSICS -> JeeCyan
                                Subject.CHEMISTRY -> NtaGreenLight
                                Subject.MATHEMATICS -> JeeAmber
                            }
                        )
                        Text(
                            text = "${sub.score} / ${sub.maxScore} Marks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Attempted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${sub.attemptedCount} / ${sub.totalQuestions}", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format("%.1f", sub.accuracy)}%", fontWeight = FontWeight.Bold, color = NtaGreenLight)
                        }
                        Column {
                            Text("Negatives", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-${sub.negativeMarksLost}", fontWeight = FontWeight.Bold, color = NtaRedLight)
                        }
                        Column {
                            Text("Avg Time/Q", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${sub.avgTimePerQuestionSeconds}s", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionAuditTab(
    allQuestions: List<QuestionItem>,
    attempt: JeeAttemptEntity,
    onQuestionSelected: (QuestionItem) -> Unit
) {
    val responses = remember(attempt) {
        val conv = com.example.data.local.JeeConverters()
        conv.toResponseMap(attempt.responsesJson)
    }

    var filterSubject by remember { mutableStateOf<Subject?>(null) }

    val filteredQuestions = remember(allQuestions, filterSubject) {
        if (filterSubject == null) allQuestions else allQuestions.filter { it.subject == filterSubject }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterSubject == null,
                onClick = { filterSubject = null },
                label = { Text("All (${allQuestions.size})") }
            )
            Subject.values().forEach { s ->
                FilterChip(
                    selected = filterSubject == s,
                    onClick = { filterSubject = s },
                    label = { Text(s.shortCode) }
                )
            }
        }

        // Question Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredQuestions) { q ->
                val resp = responses[q.id]
                val isCorrect = resp?.isCorrect == true
                val isAttempted = resp?.selectedOption != null || resp?.numericalAnswer != null

                val (badgeColor, textColor) = when {
                    isCorrect -> Pair(NtaGreenLight, Color.White)
                    isAttempted -> Pair(NtaRedLight, Color.White)
                    else -> Pair(NtaGrayDark.copy(alpha = 0.35f), MaterialTheme.colorScheme.onSurface)
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .clickable { onQuestionSelected(q) }
                        .testTag("audit_q_${q.questionNumber}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Q${q.questionNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textColor
                        )
                        Text(
                            text = when {
                                isCorrect -> "+4"
                                isAttempted -> "-1"
                                else -> "0"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
