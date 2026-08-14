package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KaramBrandCard
import com.example.ui.components.KaramMonogram
import com.example.ui.theme.JeeCyan
import com.example.ui.theme.JeeNavyDark
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * App Opening Splash & Divine Invocation Screen.
 * Displays:
 * 1. "जय गणपति बप्पा"
 * 2. "राधे कृष्ण"
 * 3. "जय प्रेमानंद जी महाराज"
 * 4. KARAM 2027 Branding Logo with animated entrance and auto-navigation.
 */
@Composable
fun OpeningBlessingScreen(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    var hasNavigated by remember { mutableStateOf(false) }

    // Auto-advance after 3.8 seconds
    LaunchedEffect(Unit) {
        delay(3800)
        if (!hasNavigated) {
            hasNavigated = true
            viewModel.navigateTo(AppScreen.TestLibrary)
        }
    }

    // Animation states for staggered entrance
    var showGanesh by remember { mutableStateOf(false) }
    var showRadheKrishna by remember { mutableStateOf(false) }
    var showPremanand by remember { mutableStateOf(false) }
    var showKaramLogo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        showGanesh = true
        delay(300)
        showRadheKrishna = true
        delay(300)
        showPremanand = true
        delay(300)
        showKaramLogo = true
    }

    // Infinite breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "divine_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090D))
            .testTag("opening_blessing_screen")
    ) {
        // Divine Ambient Canvas with golden & cyan aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF9900).copy(alpha = auraAlpha * 0.35f),
                        Color(0xFFFFD700).copy(alpha = auraAlpha * 0.20f),
                        Color(0xFF00E5FF).copy(alpha = auraAlpha * 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.85f * glowScale
                ),
                center = center,
                radius = size.width * 0.85f * glowScale
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Invocation Card: Jay Ganpati Bappa
                AnimatedVisibility(
                    visible = showGanesh,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1A1208).copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.6f)),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🕉️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "|| श्री गणेशाय नमः ||",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "जय गणपति बप्पा",
                                    color = Color(0xFFFFFFFF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Invocation Card: Radhe Krishna
                AnimatedVisibility(
                    visible = showRadheKrishna,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF130F1A).copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBA68C8).copy(alpha = 0.6f)),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪶", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🌸 राधे राधे 🌸",
                                    color = Color(0xFFCE93D8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "जय श्री राधे कृष्ण",
                                    color = Color(0xFFFFFFFF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Invocation Card: Jay Premanand Ji Maharaj
                AnimatedVisibility(
                    visible = showPremanand,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F181A).copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4DD0E1).copy(alpha = 0.6f)),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🙏 वृंदावन कृपा पात्र 🙏",
                                    color = Color(0xFF80DEEA),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "जय श्री प्रेमानंद जी महाराज",
                                    color = Color(0xFFFFFFFF),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. KARAM 2027 Logo & Shloka Card
                AnimatedVisibility(
                    visible = showKaramLogo,
                    enter = fadeIn(tween(700)) + scaleIn(tween(700, easing = OvershootInterpolatorEase))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KaramBrandCard(
                            modifier = Modifier.fillMaxWidth(),
                            showSubtext = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sankalpa Note
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF14141E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28283C)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "❤️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "अटूट विश्वास और अखंड मेहनत से JEE 2027 फतह करेंगे! कर्म पर ध्यान दो, विजय निश्चित है।",
                                    color = Color(0xFFD4D4E0),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Continue Action & Auto-Timer Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        hasNavigated = true
                        viewModel.navigateTo(AppScreen.TestLibrary)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JeeCyan,
                        contentColor = JeeNavyDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("enter_app_button")
                ) {
                    Text(
                        text = "प्रवेश करें / Enter CBT Tests",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Enter",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Auto-opening in a moment...",
                    color = Color(0xFF8A8A9E),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private val OvershootInterpolatorEase = Easing { x ->
    val tension = 1.8f
    val t = x - 1f
    t * t * ((tension + 1f) * t + tension) + 1f
}
