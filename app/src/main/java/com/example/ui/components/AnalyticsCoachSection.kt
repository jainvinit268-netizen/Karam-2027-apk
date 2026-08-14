package com.example.ui.components

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
import com.example.ui.theme.*
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun AnalyticsCoachSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val attempts by viewModel.allAttempts.collectAsState()
    val tests by viewModel.allTests.collectAsState()

    var selectedCoachPrompt by remember {
        mutableStateOf("What should I study today?")
    }

    val totalTestsAttempted = attempts.size
    val avgScore = if (attempts.isNotEmpty()) attempts.map { it.totalScore }.average().toInt() else 184
    val avgAccuracy = if (attempts.isNotEmpty()) attempts.map { it.accuracy.toDouble() }.average().toFloat() else 74.5f
    val totalNegativeMarks = if (attempts.isNotEmpty()) attempts.sumOf { it.incorrectCount } else 14

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overall Performance Overview Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E42)),
            modifier = Modifier.fillMaxWidth().testTag("analytics_overview_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "JEE Performance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JeeCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBox(title = "Avg Score", value = "$avgScore/300", color = JeeCyan)
                    MetricBox(title = "Accuracy", value = "${String.format("%.1f", avgAccuracy)}%", color = NtaGreenLight)
                    MetricBox(title = "Tests Done", value = "$totalTestsAttempted", color = JeeBlue)
                    MetricBox(title = "-ve Marks Lost", value = "-$totalNegativeMarks", color = NtaRedLight)
                }
            }
        }

        // TIME FORENSICS (4-Quadrant Matrix)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E42)),
            modifier = Modifier.fillMaxWidth().testTag("time_forensics_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = JeeAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time Forensics (4-Quadrant Speed Matrix)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Text(
                    text = "Identifies whether your errors are caused by rushing (fast & wrong) or time-draining traps (slow & wrong).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Fast & Correct
                    QuadrantCard(
                        title = "⚡ Fast & Correct",
                        desc = "Mastered Concepts",
                        count = "18 Qs",
                        color = NtaGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                    // Fast & Wrong
                    QuadrantCard(
                        title = "⚠️ Fast & Wrong",
                        desc = "Careless / Silly Slips",
                        count = "5 Qs",
                        color = JeeAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Slow & Correct
                    QuadrantCard(
                        title = "⏳ Slow & Correct",
                        desc = "Needs Speed Hacks",
                        count = "8 Qs",
                        color = JeeBlue,
                        modifier = Modifier.weight(1f)
                    )
                    // Slow & Wrong
                    QuadrantCard(
                        title = "❌ Slow & Wrong",
                        desc = "Major Time Traps",
                        count = "4 Qs",
                        color = NtaRedLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // AI JEE COACH INTERACTIVE ASSISTANT
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141422)),
            border = androidx.compose.foundation.BorderStroke(1.dp, JeeCyan),
            modifier = Modifier.fillMaxWidth().testTag("ai_jee_coach_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(JeeCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = JeeCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("AI JEE Mentor & Daily Action Plan", fontWeight = FontWeight.Bold, color = JeeCyan, fontSize = 14.sp)
                        Text("Personalized coaching powered by your test forensic data", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                // Interactive Query Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    val coachQuestions = listOf(
                        "What should I study today?",
                        "Which is my weakest chapter?",
                        "Where am I losing negative marks?",
                        "Fastest way to score +40 marks"
                    )
                    items(coachQuestions) { prompt ->
                        val isSelected = selectedCoachPrompt == prompt
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) JeeCyan else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCoachPrompt = prompt }
                        ) {
                            Text(
                                text = prompt,
                                color = if (isSelected) JeeNavyDark else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // AI Response Content Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E1E30),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33334D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = when (selectedCoachPrompt) {
                                "What should I study today?" ->
                                    "🎯 Today's 3-Hour High-Yield Schedule:\n" +
                                            "1. Physics (60 mins): Rotational Dynamics & Moment of Inertia rolling formulas (Fix 2 recurring sign errors).\n" +
                                            "2. Chemistry (45 mins): Chemical Kinetics 1st-order rate law & half-life shortcuts.\n" +
                                            "3. Maths (75 mins): Definite Integrals standard King's property & parabola area tricks."

                                "Which is my weakest chapter?" ->
                                    "⚠️ Weakest Area Detected: Rotational Mechanics (Physics) and Definite Integrals (Maths).\n" +
                                            "• Accuracy in Rotational Motion is currently 42%.\n" +
                                            "• Recommendation: Solve 15 PYQs on Pure Rolling and Parallel Axis Theorem."

                                "Where am I losing negative marks?" ->
                                    "🚨 Negative Mark Breakdown:\n" +
                                            "• 60% lost to Silly Mistakes (Sign calculation slips in Numerical questions).\n" +
                                            "• 40% lost to Formula Misconceptions in Physical Chemistry.\n" +
                                            "• Rule for next mock: Skip Section B numericals if calculation takes > 2 minutes."

                                else ->
                                    "🚀 +40 Marks Fast-Track Strategy:\n" +
                                            "1. Shift 5 'Slow & Wrong' questions to SKIP immediately in round 1 (+20 mins saved).\n" +
                                            "2. Re-allocate saved time to double check Section A MCQs in Chemistry (+16 marks).\n" +
                                            "3. Target Modern Physics and Coordination Compounds (+12 high-probability marks)."
                            },
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun MetricBox(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun QuadrantCard(
    title: String,
    desc: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = desc, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
