package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JeeAttemptEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttemptsHistorySection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val attempts by viewModel.allAttempts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (attempts.isEmpty()) {
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
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Test Attempts Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Launch a CBT mock from the Test Library to view your forensic scorecard.",
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
                items(attempts, key = { it.attemptId }) { attempt ->
                    AttemptHistoryCard(
                        attempt = attempt,
                        onViewReport = {
                            viewModel.navigateTo(AppScreen.ForensicAnalysis(attempt.attemptId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttemptHistoryCard(
    attempt: JeeAttemptEntity,
    onViewReport: () -> Unit
) {
    val dateStr = remember(attempt.attemptTimestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(attempt.attemptTimestamp))
    }

    val durationMin = remember(attempt.durationSeconds) {
        attempt.durationSeconds / 60
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E42)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attempt_card_${attempt.attemptId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attempt.testTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (attempt.totalScore >= 180) NtaGreenLight.copy(alpha = 0.2f) else JeeCyan.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${attempt.totalScore} / 300",
                        fontWeight = FontWeight.Bold,
                        color = if (attempt.totalScore >= 180) NtaGreenLight else JeeCyan,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(
                    label = "Accuracy",
                    value = "${String.format("%.1f", attempt.accuracy)}%",
                    color = NtaGreenLight
                )
                MetricPill(
                    label = "Attempted",
                    value = "${attempt.totalAttempted}",
                    color = JeeCyan
                )
                MetricPill(
                    label = "Correct",
                    value = "${attempt.correctCount} ✓",
                    color = NtaGreenLight
                )
                MetricPill(
                    label = "Incorrect",
                    value = "${attempt.incorrectCount} ✗",
                    color = NtaRedLight
                )
            }

            Divider(color = Color(0xFF2E2E42), thickness = 0.8.dp)

            // CTA Button
            Button(
                onClick = onViewReport,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = JeeNavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_view_report_${attempt.attemptId}")
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Deep Forensic Report", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
