package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.AiConfigState
import com.example.data.ai.AiConnectionStatus
import com.example.data.ai.AiKeySource
import com.example.ui.theme.*
import com.example.ui.viewmodel.JeeViewModel

/**
 * Top App Bar AI Connection Status Badge
 */
@Composable
fun AiStatusTopBarChip(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val aiConfig by viewModel.aiConfigState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    val isConnected = aiConfig.isConfigured
    val badgeBg = if (isConnected) JeeCyan.copy(alpha = 0.15f) else Color(0xFFFFB74D).copy(alpha = 0.18f)
    val badgeBorder = if (isConnected) JeeCyan.copy(alpha = 0.5f) else Color(0xFFFFB74D)
    val badgeText = if (isConnected) "Gemini AI: Ready" else "AI Setup"
    val iconColor = if (isConnected) JeeCyan else Color(0xFFE65100)

    Surface(
        onClick = { showSettingsDialog = true },
        shape = RoundedCornerShape(16.dp),
        color = badgeBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder),
        modifier = modifier
            .padding(end = 4.dp)
            .testTag("ai_status_top_bar_chip")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(iconColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (showSettingsDialog) {
        AiSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

/**
 * Full AI / Gemini Settings Dialog with 3-tier Fallback Engine & Test Ping.
 */
@Composable
fun AiSettingsDialog(
    viewModel: JeeViewModel,
    onDismiss: () -> Unit
) {
    val aiConfig by viewModel.aiConfigState.collectAsState()
    var inputKey by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            viewModel.resetGeminiTestStatus()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("ai_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(JeeCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = JeeCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gemini AI Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "JEE Question Paper AI Parser Engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = {
                        viewModel.resetGeminiTestStatus()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Active Status Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (aiConfig.source) {
                        AiKeySource.USER_KEY -> Color(0xFFE8F5E9)
                        AiKeySource.ENV_SECRET -> Color(0xFFE3F2FD)
                        AiKeySource.NONE -> Color(0xFFFFF3E0)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (aiConfig.source) {
                            AiKeySource.USER_KEY -> Color(0xFF81C784)
                            AiKeySource.ENV_SECRET -> Color(0xFF90CAF9)
                            AiKeySource.NONE -> Color(0xFFFFB74D)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (aiConfig.source) {
                                    AiKeySource.USER_KEY, AiKeySource.ENV_SECRET -> Icons.Default.CheckCircle
                                    AiKeySource.NONE -> Icons.Default.WarningAmber
                                },
                                contentDescription = null,
                                tint = when (aiConfig.source) {
                                    AiKeySource.USER_KEY -> Color(0xFF2E7D32)
                                    AiKeySource.ENV_SECRET -> Color(0xFF1565C0)
                                    AiKeySource.NONE -> Color(0xFFE65100)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (aiConfig.source) {
                                    AiKeySource.USER_KEY -> "Gemini Connected (User API Key)"
                                    AiKeySource.ENV_SECRET -> "Gemini Connected (AI Studio Environment Secret)"
                                    AiKeySource.NONE -> "Gemini Not Configured"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = when (aiConfig.source) {
                                    AiKeySource.USER_KEY -> Color(0xFF2E7D32)
                                    AiKeySource.ENV_SECRET -> Color(0xFF1565C0)
                                    AiKeySource.NONE -> Color(0xFFE65100)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (aiConfig.isConfigured) "Active: ${aiConfig.maskedKey}" else "No active key detected. The app uses local algorithmic extraction until configured.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Key Input Section
                Text(
                    text = "Configure Custom Gemini API Key",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "You can supply your personal key from Google AI Studio (aistudio.google.com). Custom keys override environment defaults securely on-device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Gemini API Key (AIzaSy...)") },
                    placeholder = { Text("Enter your Gemini API key") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide Key" else "Show Key"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (inputKey.isNotBlank()) {
                                viewModel.saveGeminiApiKey(inputKey)
                                inputKey = ""
                            }
                        },
                        enabled = inputKey.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_gemini_key_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Key")
                    }

                    if (aiConfig.hasUserCustomKey) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearGeminiApiKey()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("clear_gemini_key_button")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Test API Connection Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Connection",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pings Google Gemini API live to verify authentication and quota",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.testGeminiConnection(inputKey.takeIf { it.isNotBlank() })
                        },
                        enabled = (aiConfig.isConfigured || inputKey.isNotBlank()) && aiConfig.connectionStatus !is AiConnectionStatus.Testing,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("test_gemini_connection_button")
                    ) {
                        if (aiConfig.connectionStatus is AiConnectionStatus.Testing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...")
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test API")
                        }
                    }
                }

                // Connection Test Feedback Display
                AnimatedVisibility(visible = aiConfig.connectionStatus !is AiConnectionStatus.NotTested) {
                    Spacer(modifier = Modifier.height(10.dp))
                    when (val status = aiConfig.connectionStatus) {
                        is AiConnectionStatus.Testing -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Sending test prompt to gemini-3.5-flash...", fontSize = 12.sp)
                                }
                            }
                        }
                        is AiConnectionStatus.Success -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE8F5E9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Connection Successful", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(status.message, fontSize = 12.sp, color = Color(0xFF1B5E20))
                                    Text("Model: ${status.model}", fontSize = 11.sp, color = Color(0xFF388E3C))
                                }
                            }
                        }
                        is AiConnectionStatus.Error -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (status.httpCode != null) "Error (HTTP ${status.httpCode})" else "Connection Failed",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(status.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                    if (!status.details.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Details: ${status.details}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3-Tier Hierarchy Info Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Intelligent AI Fallback Hierarchy:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. User-Configured Key: Stored locally & securely.\n2. Server-side Secret: AI Studio environment secret.\n3. Local Fallback Engine: If unconfigured, the app uses built-in smart regex extraction so testing never crashes.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
