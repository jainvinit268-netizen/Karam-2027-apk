package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JeeConverters
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun MistakeBookSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val attempts by viewModel.allAttempts.collectAsState()
    val tests by viewModel.allTests.collectAsState()
    val context = LocalContext.current

    var selectedMistakeFilter by remember { mutableStateOf("All") }
    var selectedSubjectFilter by remember { mutableStateOf<Subject?>(null) }

    // Parse all real mistakes across past user attempts (NO demo/sample data)
    val converters = remember { JeeConverters() }
    val mistakeList = remember(attempts, tests) {
        val list = mutableListOf<Triple<QuestionItem, StudentResponse, String>>()
        val testMap = tests.associateBy { it.testId }

        for (att in attempts) {
            val test = testMap[att.testId] ?: continue
            val qList = converters.toQuestionList(test.questionsJson)
            val respMap = converters.toResponseMap(att.responsesJson)

            for (q in qList) {
                val resp = respMap[q.id]
                val isWrong = resp?.isCorrect == false || (resp != null && resp.status == QuestionStatus.ANSWERED && resp.marksAwarded <= 0)
                val hasMistakeTag = resp?.mistakeCategory != null && resp.mistakeCategory != MistakeType.NONE
                if (isWrong || hasMistakeTag) {
                    val safeResp = resp ?: StudentResponse(questionId = q.id, mistakeCategory = MistakeType.SILLY_MISTAKE)
                    list.add(Triple(q, safeResp, test.title))
                }
            }
        }
        list
    }

    val filteredMistakes = remember(mistakeList, selectedMistakeFilter, selectedSubjectFilter) {
        mistakeList.filter { (q, resp, _) ->
            val matchesSubj = selectedSubjectFilter == null || q.subject == selectedSubjectFilter
            val matchesType = when (selectedMistakeFilter) {
                "All" -> true
                "Formula / Concept" -> resp.mistakeCategory == MistakeType.CONCEPTUAL_MISTAKE
                "Calculation" -> resp.mistakeCategory == MistakeType.CALCULATION_MISTAKE
                "Silly Mistake" -> resp.mistakeCategory == MistakeType.SILLY_MISTAKE
                "Time Trap" -> resp.mistakeCategory == MistakeType.TIME_TRAP
                "Wrong Approach" -> resp.mistakeCategory == MistakeType.WRONG_APPROACH
                else -> true
            }
            matchesSubj && matchesType
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (mistakeList.isEmpty()) {
            // Clean empty state
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = JeeCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Mistake Book is Empty",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Take a test from your library or upload a new PDF. Any incorrect, time-trapped, or flagged questions will automatically appear here with chapter breakdown and video solutions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.ConvertPdf) },
                        colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Question Paper & Key", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Hero card with Revision Test Generator
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, JeeCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("mistake_book_hero_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = JeeCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My JEE Mistake Book (${mistakeList.size} Logged)", fontWeight = FontWeight.Bold, color = JeeCyan, fontSize = 14.sp)
                    }
                    Text(
                        text = "Mistakes are automatically tagged by error type. Fix your conceptual leaks and re-test weak spots.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            viewModel.createRevisionTestFromMistakes { testId ->
                                viewModel.startExam(testId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_generate_revision_test")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🎯 Generate Targeted Revision CBT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Mistake Type Filters
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                val categories = listOf("All", "Formula / Concept", "Calculation", "Silly Mistake", "Time Trap", "Wrong Approach")
                items(categories) { cat ->
                    val isSelected = selectedMistakeFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMistakeFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NtaRedLight.copy(alpha = 0.25f),
                            selectedLabelColor = NtaRedLight
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Subject Filters
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val subjects = listOf<Pair<String, Subject?>>(
                    "All Subjects" to null,
                    "Physics" to Subject.PHYSICS,
                    "Chemistry" to Subject.CHEMISTRY,
                    "Maths" to Subject.MATHEMATICS
                )
                subjects.forEach { (label, subj) ->
                    val isSelected = selectedSubjectFilter == subj
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) JeeCyan else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f).clickable { selectedSubjectFilter = subj }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // List of Mistake Cards
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredMistakes) { (q, resp, testTitle) ->
                    MistakeCardItem(
                        question = q,
                        response = resp,
                        testTitle = testTitle,
                        onWatchVideo = {
                            val query = q.youtubeSearchQuery.ifBlank { "JEE Main ${q.subject.displayName} ${q.chapter} solution" }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MistakeCardItem(
    question: QuestionItem,
    response: StudentResponse,
    testTitle: String,
    onWatchVideo: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q${question.questionNumber} • ${question.subject.displayName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = JeeCyan
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NtaRedLight.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = response.mistakeCategory.label,
                        color = NtaRedLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(text = question.questionText, fontSize = 12.sp, maxLines = if (isExpanded) Int.MAX_VALUE else 2)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Your Ans: ${response.selectedOption ?: response.numericalAnswer ?: "Unattempted"}",
                    fontSize = 11.sp,
                    color = NtaRedLight,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Correct Ans: ${question.correctAnswer}",
                    fontSize = 11.sp,
                    color = NtaGreenLight,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
                    Text("Chapter: ${question.chapter} • Concept: ${question.concept}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Official JEE Solution:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NtaGreenLight)
                    Text(text = question.solutionText, fontSize = 11.sp)

                    Button(
                        onClick = onWatchVideo,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NtaRedLight, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Watch Verified Video Solution", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
