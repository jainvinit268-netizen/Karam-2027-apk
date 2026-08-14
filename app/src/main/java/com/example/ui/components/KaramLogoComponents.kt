package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JeeCyan

/**
 * High-precision Composable rendering of the iconic "K" monogram from KARAM 2027.
 * Features parallel double vertical bars, dual upper/lower diagonal arms, and metallic gradient sheen.
 */
@Composable
fun KaramMonogram(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animatedSheen: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "karam_sheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_float"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.18f))
            .background(Color(0xFF0F0F14))
            .border(1.5.dp, Color(0xFF2B2B38), RoundedCornerShape(size * 0.18f))
            .padding(size * 0.12f)
            .testTag("karam_monogram_logo"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Base metallic gradient
            val metallicGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF0F0F0),
                    Color(0xFFB5B5B5),
                    Color(0xFF707070)
                ),
                startY = 0f,
                endY = h
            )

            // Optional subtle animated shimmer brush
            val sheenBrush = if (animatedSheen) {
                Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    start = Offset(w * sheenProgress, 0f),
                    end = Offset(w * (sheenProgress + 0.35f), h)
                )
            } else null

            // Geometry proportions for 'K'
            val barW = w * 0.14f
            val barGap = w * 0.07f
            val leftBar1X = w * 0.08f
            val leftBar2X = leftBar1X + barW + barGap
            val topY = h * 0.05f
            val botY = h * 0.95f
            val barH = botY - topY
            val midY = h * 0.50f

            // 1. Left Vertical Bar 1
            drawRoundRect(
                brush = metallicGradient,
                topLeft = Offset(leftBar1X, topY),
                size = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )

            // 2. Left Vertical Bar 2
            drawRoundRect(
                brush = metallicGradient,
                topLeft = Offset(leftBar2X, topY),
                size = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )

            // 3. Upper Diagonal Arms
            // Upper Main Arm
            val pathUpperMain = Path().apply {
                moveTo(leftBar2X + barW, midY)
                lineTo(w * 0.92f, topY)
                lineTo(w * 0.92f - barW, topY)
                lineTo(leftBar2X + barW, midY - barW * 1.2f)
                close()
            }
            drawPath(pathUpperMain, brush = metallicGradient)

            // Upper Secondary Inner Arm
            val pathUpperInner = Path().apply {
                moveTo(leftBar2X + barW, midY + barW * 0.5f)
                lineTo(w * 0.92f, topY + barW * 1.5f)
                lineTo(w * 0.92f - barW, topY + barW * 1.5f)
                lineTo(leftBar2X + barW, midY - barW * 0.2f)
                close()
            }
            drawPath(pathUpperInner, brush = metallicGradient)

            // 4. Lower Diagonal Arms
            // Lower Main Arm
            val pathLowerMain = Path().apply {
                moveTo(leftBar2X + barW, midY - barW * 0.4f)
                lineTo(w * 0.82f, botY)
                lineTo(w * 0.82f + barW, botY)
                lineTo(leftBar2X + barW + barW * 0.8f, midY)
                close()
            }
            drawPath(pathLowerMain, brush = metallicGradient)

            // Lower Outer Parallel Arm
            val pathLowerOuter = Path().apply {
                moveTo(leftBar2X + barW + barW * 0.9f, midY + barW * 0.3f)
                lineTo(w * 0.96f, botY)
                lineTo(w * 0.96f - barW, botY)
                lineTo(leftBar2X + barW + barW * 0.4f, midY + barW * 0.3f)
                close()
            }
            drawPath(pathLowerOuter, brush = metallicGradient)

            // Draw sheen overlay if active
            if (sheenBrush != null) {
                drawRoundRect(brush = sheenBrush, topLeft = Offset.Zero, size = this.size)
            }
        }
    }
}

/**
 * Full KARAM 2027 Brand Badge with Logo, Typography and Flame Emojis.
 */
@Composable
fun KaramBrandCard(
    modifier: Modifier = Modifier,
    showSubtext: Boolean = true
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0D0D12))
            .border(1.dp, Color(0xFF2A2A3A), RoundedCornerShape(20.dp))
            .padding(vertical = 18.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KaramMonogram(size = 110.dp, animatedSheen = true)

        Spacer(modifier = Modifier.height(14.dp))

        // "K A R A M" Title with wide letter spacing
        Text(
            text = "K A R A M",
            color = Color(0xFFE2E2E8),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 8.sp,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(4.dp))

        // "2027 🔥🔥" Subtitle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "2 0 2 7",
                color = Color(0xFFA5A5B5),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🔥 🔥",
                fontSize = 18.sp
            )
        }

        if (showSubtext) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E1E28)
            ) {
                Text(
                    text = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Compact Top App Bar Brand Logo & Title
 */
@Composable
fun KaramTopBarBrand(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KaramMonogram(size = 36.dp, animatedSheen = false)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "KARAM",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "2027",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = JeeCyan
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "🔥", fontSize = 12.sp)
            }
            Text(
                text = "JEE CBT Simulator",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
