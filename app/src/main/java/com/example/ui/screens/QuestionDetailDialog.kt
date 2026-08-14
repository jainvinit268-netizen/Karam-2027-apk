package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.MistakeType
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.StudentResponse
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailDialog(
    question: QuestionItem,
    response: StudentResponse?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showImageZoom by remember { mutableStateOf(false) }

    val isCorrect = response?.isCorrect == true
    val isAttempted = response?.selectedOption != null || response?.numericalAnswer != null
    val marksAwarded = response?.marksAwarded ?: 0

    val userGivenAns = if (question.type == QuestionType.MCQ) {
        response?.selectedOption ?: "None"
    } else {
        response?.numericalAnswer ?: "None"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("question_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCorrect -> NtaGreenLight
                                    isAttempted -> NtaRedLight
                                    else -> NtaGrayDark
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Q${question.questionNumber}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${question.subject.displayName} • ${question.chapter}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Concept: ${question.concept} • Difficulty: ${question.difficulty.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // Marks Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isCorrect -> NtaGreen.copy(alpha = 0.2f)
                                isAttempted -> NtaRed.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when {
                            isCorrect -> "+4 Marks"
                            isAttempted -> "-1 Mark"
                            else -> "0 (Unattempted)"
                        },
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCorrect -> NtaGreenLight
                            isAttempted -> NtaRedLight
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 12.sp
                    )
                }
            }

            // Question Box: Render Original Visual Crop if available, else text
            if (!question.imageUrl.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JeeCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ORIGINAL QUESTION VISUAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = JeeNavyDark,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = { showImageZoom = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = JeeNavyDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        AsyncImage(
                            model = question.imageUrl,
                            contentDescription = "Question Visual",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showImageZoom = true }
                        )
                    }
                }

                if (showImageZoom) {
                    Dialog(
                        onDismissRequest = { showImageZoom = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(alpha = 0.95f)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = question.imageUrl,
                                    contentDescription = "Zoomed Question",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                )

                                IconButton(
                                    onClick = { showImageZoom = false },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(24.dp)
                                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "QUESTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = JeeCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = question.questionText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )

                        if (question.type == QuestionType.MCQ && question.options.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            question.options.forEach { opt ->
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Comparison: Your Answer vs Correct Answer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) NtaGreen.copy(alpha = 0.15f) else if (isAttempted) NtaRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Your Answer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userGivenAns,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) NtaGreenLight else if (isAttempted) NtaRedLight else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = NtaGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Correct Answer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.correctAnswer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NtaGreenLight
                        )
                    }
                }
            }

            // Time Spent vs Expected Ideal Time
            val timeSpentSec = response?.timeSpentSeconds ?: 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Time Analysis", style = MaterialTheme.typography.labelSmall, color = JeeCyan, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Time spent: ${timeSpentSec}s (Ideal: ${question.idealTimeSeconds}s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (timeSpentSec > question.idealTimeSeconds * 1.5) NtaRedLight else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (timeSpentSec > question.idealTimeSeconds * 1.5) {
                        Text("Time Trap ⚠️", fontSize = 11.sp, color = JeeAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Error Forensic Diagnosis (if incorrect)
            if (response != null && response.mistakeCategory != MistakeType.NONE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NtaRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = NtaRedLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mistake Diagnosis: ${response.mistakeCategory.label}",
                                fontWeight = FontWeight.Bold,
                                color = NtaRedLight,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = response.mistakeCategory.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Best JEE Solution
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = JeeAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Best JEE Solution & Shortcut Method",
                            fontWeight = FontWeight.Bold,
                            color = JeeAmber,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.solutionText,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            // Verified YouTube Solution Button (Direct Intent)
            Button(
                onClick = {
                    val query = if (question.youtubeSearchQuery.isNotBlank()) {
                        question.youtubeSearchQuery
                    } else {
                        "JEE Main ${question.subject.displayName} ${question.chapter} ${question.concept} solution"
                    }
                    val ytUri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                    val intent = Intent(Intent.ACTION_VIEW, ytUri)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("youtube_solution_btn_q${question.questionNumber}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCC0000),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayCircleFilled, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Watch Verified Solution on YouTube",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
