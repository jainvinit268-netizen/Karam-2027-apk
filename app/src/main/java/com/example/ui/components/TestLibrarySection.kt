package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JeeTestEntity
import com.example.data.model.Subject
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun TestLibrarySection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val tests by viewModel.allTests.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var testForCustomLaunch by remember { mutableStateOf<JeeTestEntity?>(null) }

    val filterOptions = listOf("All", "Full Syllabus", "Physics", "Chemistry", "Maths", "Sample Papers")

    val filteredTests = remember(tests, searchQuery, selectedFilter) {
        tests.filter { test ->
            val matchesQuery = test.title.contains(searchQuery, ignoreCase = true) ||
                    (test.tags?.contains(searchQuery, ignoreCase = true) == true)
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Full Syllabus" -> test.totalQuestions >= 50
                "Physics" -> test.physicsQuestionsCount > 0
                "Chemistry" -> test.chemistryQuestionsCount > 0
                "Maths" -> test.mathsQuestionsCount > 0
                "Sample Papers" -> test.isSample
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search JEE Main papers, shifts, mocks...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("test_search_input")
        )

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JeeCyan.copy(alpha = 0.25f),
                        selectedLabelColor = JeeCyan
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_$filter")
                )
            }
        }

        // Test Cards List
        if (filteredTests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No tests match your filter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upload a PDF or clear filters to start practicing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTests, key = { it.testId }) { test ->
                    TestItemCard(
                        test = test,
                        onQuickStart = { viewModel.startExam(test.testId) },
                        onCustomLaunch = { testForCustomLaunch = test },
                        onDelete = { viewModel.deleteTest(test.testId) }
                    )
                }
            }
        }
    }

    testForCustomLaunch?.let { test ->
        CustomTestLaunchDialog(
            test = test,
            onDismiss = { testForCustomLaunch = null },
            onLaunch = { duration, count, subj ->
                testForCustomLaunch = null
                viewModel.startExamWithCustomConfig(
                    testId = test.testId,
                    customMinutes = duration,
                    questionLimit = count,
                    subjectFilter = subj
                )
            }
        )
    }
}

@Composable
private fun TestItemCard(
    test: JeeTestEntity,
    onQuickStart: () -> Unit,
    onCustomLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    val isTrialTest = test.testId == "quizrr_qpt_02"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTrialTest) JeeCyan.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isTrialTest) 1.5.dp else 1.dp,
            if (isTrialTest) JeeCyan else Color(0xFF2E2E42)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("test_card_${test.testId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title & Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isTrialTest) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = JeeCyan.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "⭐ YOUR UPLOADED TRIAL TEST (QPT-02)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JeeCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = test.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = test.tags ?: "JEE Main Official CBT",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isTrialTest) NtaGreenLight else JeeCyan
                    )
                }

                if (!test.isSample) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Metric Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    icon = Icons.Default.HelpOutline,
                    label = "${test.totalQuestions} Questions"
                )
                MetricChip(
                    icon = Icons.Default.Timer,
                    label = "${test.durationMinutes} Mins"
                )
                MetricChip(
                    icon = Icons.Default.StarBorder,
                    label = "+4 / -1 Marking"
                )
            }

            // Subject Breakdown Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SubjectCountPill(subj = "Physics", count = test.physicsQuestionsCount, color = JeeBlue)
                SubjectCountPill(subj = "Chemistry", count = test.chemistryQuestionsCount, color = NtaGreenLight)
                SubjectCountPill(subj = "Maths", count = test.mathsQuestionsCount, color = JeeAmber)
            }

            Divider(color = Color(0xFF2E2E42), thickness = 0.8.dp)

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCustomLaunch,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_custom_launch_${test.testId}")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom Speed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onQuickStart,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = JeeNavyDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_start_test_${test.testId}")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start CBT Exam", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SubjectCountPill(subj: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "$subj: $count",
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
