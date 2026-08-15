package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.PairedJsonTestImporter
import com.example.data.local.JeeTestEntity
import com.example.data.model.Subject
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel
import kotlinx.coroutines.launch

@Composable
fun TestLibrarySection(viewModel: JeeViewModel, modifier: Modifier = Modifier) {
    val tests by viewModel.allTests.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var testForCustomLaunch by remember { mutableStateOf<JeeTestEntity?>(null) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }
    var questionJsonUri by remember { mutableStateOf<Uri?>(null) }
    var answerKeyJsonUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    fun launchPairedImport() {
        val qUri = questionJsonUri ?: return
        val aUri = answerKeyJsonUri ?: return
        scope.launch {
            importing = true
            importMessage = null
            try {
                val testId = PairedJsonTestImporter.import(viewModel.getApplication(), qUri, aUri)
                importMessage = "Test created successfully: $testId"
                questionJsonUri = null
                answerKeyJsonUri = null
            } catch (e: Exception) {
                importMessage = "JSON mapping failed: ${e.message ?: "Invalid question/answer JSON"}"
            } finally {
                importing = false
            }
        }
    }

    val questionPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) questionJsonUri = uri
    }
    val answerPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) answerKeyJsonUri = uri
    }

    LaunchedEffect(questionJsonUri, answerKeyJsonUri) {
        if (questionJsonUri != null && answerKeyJsonUri != null && !importing) launchPairedImport()
    }

    val filterOptions = listOf("All", "Full Syllabus", "Physics", "Chemistry", "Maths")
    val filteredTests = remember(tests, searchQuery, selectedFilter) {
        tests.filter { test ->
            val matchesQuery = test.title.contains(searchQuery, ignoreCase = true) || test.tags?.contains(searchQuery, ignoreCase = true) == true
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Full Syllabus" -> test.totalQuestions >= 50
                "Physics" -> test.physicsQuestionsCount > 0
                "Chemistry" -> test.chemistryQuestionsCount > 0
                "Maths" -> test.mathsQuestionsCount > 0
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search JEE Main papers, tests...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "Clear") } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).testTag("test_search_input")
            )
            FilledTonalIconButton(
                onClick = { questionPicker.launch(arrayOf("application/json", "text/json", "text/plain")) },
                enabled = !importing,
                modifier = Modifier.size(52.dp).testTag("btn_import_questions_json")
            ) { Icon(Icons.Default.UploadFile, contentDescription = "Import Questions JSON") }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Build Test from 2 JSON files", fontWeight = FontWeight.Bold)
                Text("Select the Questions JSON and the separate Answer Key JSON. KARAM maps them by question ID/number and creates the CBT automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { questionPicker.launch(arrayOf("application/json", "text/json", "text/plain")) },
                        enabled = !importing,
                        modifier = Modifier.weight(1f).testTag("btn_questions_json")
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(if (questionJsonUri == null) "Questions JSON" else "Questions ✓", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { answerPicker.launch(arrayOf("application/json", "text/json", "text/plain")) },
                        enabled = !importing,
                        modifier = Modifier.weight(1f).testTag("btn_answer_key_json")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(if (answerKeyJsonUri == null) "Answer Key JSON" else "Key ✓", fontSize = 12.sp)
                    }
                }
                if (questionJsonUri != null || answerKeyJsonUri != null) {
                    Text(
                        text = when {
                            importing -> "Mapping questions + answer key…"
                            questionJsonUri != null && answerKeyJsonUri == null -> "Questions selected • now select Answer Key JSON"
                            questionJsonUri == null && answerKeyJsonUri != null -> "Answer key selected • now select Questions JSON"
                            else -> "Both selected • creating test…"
                        },
                        color = JeeCyan,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        importMessage?.let { message ->
            AssistChip(
                onClick = { importMessage = null },
                label = { Text(message, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(if (message.startsWith("Test created")) Icons.Default.CheckCircle else Icons.Default.Error, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("json_import_status")
            )
        }

        if (tests.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JeeCyan.copy(alpha = 0.25f), selectedLabelColor = JeeCyan),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }
        }

        if (filteredTests.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = JeeCyan, modifier = Modifier.size(48.dp))
                        Text(text = if (tests.isEmpty()) "Test Library is Empty" else "No tests match your filter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (tests.isEmpty()) "Upload Questions JSON + Answer Key JSON and KARAM will map them into a JEE CBT automatically, or use the PDF + official key workflow." else "Clear filters or import a new test.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { questionPicker.launch(arrayOf("application/json", "text/json", "text/plain")) }, enabled = !importing, colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black), shape = RoundedCornerShape(10.dp), modifier = Modifier.testTag("btn_empty_questions_json")) {
                                Icon(Icons.Default.DataObject, contentDescription = null); Spacer(modifier = Modifier.width(6.dp)); Text("Questions JSON", fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = { answerPicker.launch(arrayOf("application/json", "text/json", "text/plain")) }, enabled = !importing, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(10.dp), modifier = Modifier.testTag("btn_empty_answer_key_json")) {
                                Icon(Icons.Default.Key, contentDescription = null); Spacer(modifier = Modifier.width(6.dp)); Text("Answer Key")
                            }
                        }
                        Button(onClick = { viewModel.navigateTo(AppScreen.ConvertPdf) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(10.dp), modifier = Modifier.testTag("btn_empty_upload_pdf")) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null); Spacer(modifier = Modifier.width(6.dp)); Text("PDF + Key")
                        }
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(filteredTests, key = { it.testId }) { test ->
                    TestItemCard(test = test, onQuickStart = { viewModel.startExam(test.testId) }, onCustomLaunch = { testForCustomLaunch = test }, onDelete = { viewModel.deleteTest(test.testId) })
                }
            }
        }
    }

    testForCustomLaunch?.let { test ->
        CustomTestLaunchDialog(
            test = test,
            onDismiss = { testForCustomLaunch = null },
            onLaunch = { duration: Int, count: Int?, subj: Subject? ->
                testForCustomLaunch = null
                viewModel.startExamWithCustomConfig(testId = test.testId, customMinutes = duration, questionLimit = count, subjectFilter = subj)
            }
        )
    }
}

@Composable
private fun TestItemCard(test: JeeTestEntity, onQuickStart: () -> Unit, onCustomLaunch: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth().testTag("test_card_${test.testId}")) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(test.tags ?: "JEE Main CBT Paper", style = MaterialTheme.typography.labelSmall, color = JeeCyan)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Test", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetadataBadge(Icons.Default.HelpOutline, "${test.totalQuestions} Questions")
                MetadataBadge(Icons.Default.Timer, "${test.durationMinutes} mins")
                MetadataBadge(Icons.Default.MilitaryTech, "+4 / -1 Marking")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SubjectBadge("Physics", test.physicsQuestionsCount, NtaRedLight)
                SubjectBadge("Chemistry", test.chemistryQuestionsCount, NtaGreenLight)
                SubjectBadge("Maths", test.mathsQuestionsCount, JeeCyan)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCustomLaunch, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).testTag("btn_custom_launch_${test.testId}")) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(6.dp)); Text("Customize", fontSize = 12.sp)
                }
                Button(onClick = onQuickStart, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black), modifier = Modifier.weight(1.3f).testTag("btn_start_test_${test.testId}")) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(6.dp)); Text("Start CBT Exam", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MetadataBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}

@Composable
private fun SubjectBadge(subject: String, count: Int, color: Color) {
    if (count <= 0) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 3.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Text("$subject: $count", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
