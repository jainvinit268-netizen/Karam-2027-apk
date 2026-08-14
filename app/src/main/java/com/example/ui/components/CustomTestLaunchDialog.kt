package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JeeTestEntity
import com.example.data.model.Subject
import com.example.ui.theme.JeeBlue
import com.example.ui.theme.JeeCyan
import com.example.ui.theme.JeeNavyDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTestLaunchDialog(
    test: JeeTestEntity,
    onDismiss: () -> Unit,
    onLaunch: (durationMinutes: Int, questionLimit: Int?, subjectFilter: Subject?) -> Unit
) {
    val durationPresets = listOf(1, 5, 10, 30, 60, 90, 120, 180)
    var selectedDurationMinutes by remember { mutableStateOf(test.durationMinutes) }
    var customDurationText by remember { mutableStateOf("") }
    var isCustomDurationActive by remember { mutableStateOf(false) }

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var selectedQuestionLimit by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = JeeCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Custom CBT Exam Launcher", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // 1. Duration Presets
                Text(
                    text = "1. Test Duration (Speed / Full Simulation):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(durationPresets) { mins ->
                        val isSelected = !isCustomDurationActive && selectedDurationMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) JeeCyan else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    isCustomDurationActive = false
                                    selectedDurationMinutes = mins
                                }
                                .testTag("duration_preset_$mins")
                        ) {
                            Text(
                                text = "${mins}m",
                                color = if (isSelected) JeeNavyDark else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Custom Minute Input
                OutlinedTextField(
                    value = customDurationText,
                    onValueChange = {
                        customDurationText = it
                        val parsed = it.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            isCustomDurationActive = true
                            selectedDurationMinutes = parsed
                        }
                    },
                    label = { Text("Or Custom Minutes (e.g. 15, 45, 240)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )

                // 2. Subject Filter
                Text(
                    text = "2. Subject Focus:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val subjects = listOf<Pair<String, Subject?>>(
                        "All (P+C+M)" to null,
                        "Physics" to Subject.PHYSICS,
                        "Chemistry" to Subject.CHEMISTRY,
                        "Maths" to Subject.MATHEMATICS
                    )

                    subjects.forEach { (label, subj) ->
                        val isSelected = selectedSubject == subj
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) JeeCyan else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSubject = subj }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) JeeNavyDark else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // 3. Question Count Limit
                Text(
                    text = "3. Question Limit:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                val dynamicLimits = mutableListOf<Pair<String, Int?>>()
                dynamicLimits.add("All (${test.totalQuestions})" to null)
                val candidates = listOf(5, 10, 15, 20, 25, 30, 45, 60, 75)
                candidates.filter { it < test.totalQuestions }.take(4).forEach { count ->
                    dynamicLimits.add("$count Qs" to count)
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(dynamicLimits) { (label, count) ->
                        val isSelected = selectedQuestionLimit == count
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) JeeCyan else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedQuestionLimit = count }
                                .testTag("limit_chip_${count ?: "all"}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) JeeNavyDark else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDuration = if (isCustomDurationActive) {
                        customDurationText.toIntOrNull()?.coerceAtLeast(1) ?: selectedDurationMinutes
                    } else {
                        selectedDurationMinutes
                    }
                    onLaunch(finalDuration, selectedQuestionLimit, selectedSubject)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JeeCyan,
                    contentColor = JeeNavyDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("launch_custom_exam_btn")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start CBT Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
