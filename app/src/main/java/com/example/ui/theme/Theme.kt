package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JeeCyan,
    onPrimary = JeeNavyDark,
    primaryContainer = JeeBlue,
    onPrimaryContainer = JeeCyanLight,
    secondary = JeeAmber,
    onSecondary = Color.Black,
    tertiary = NtaPurpleLight,
    background = DarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkCard,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF13233D),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkCardBorder,
    error = NtaRedLight
)

private val LightColorScheme = lightColorScheme(
    primary = JeeBlueAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = JeeBlue,
    secondary = JeeOrange,
    onSecondary = Color.White,
    tertiary = NtaPurple,
    background = LightBg,
    onBackground = Color(0xFF0F172A),
    surface = LightCard,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = LightCardBorder,
    error = NtaRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to clean, high-contrast dark theme for exam readability
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
