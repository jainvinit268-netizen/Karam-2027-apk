package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JeeCyan
import com.example.ui.theme.NtaGreenLight
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun LinkToCbtSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val conversionState by viewModel.conversionState.collectAsState()
    var pdfUrl by remember { mutableStateOf("") }
    var testTitle by remember { mutableStateOf("Linked JEE Paper") }
    var duration by remember { mutableStateOf("180") }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = JeeCyan.copy(alpha = 0.18f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = JeeCyan,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Generate CBT from Link", fontWeight = FontWeight.Bold, color = JeeCyan)
                    Text(
                        "Paste the direct PDF link. The existing Gemini extraction, test saving, analysis and history stay unchanged.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedTextField(
            value = testTitle,
            onValueChange = { testTitle = it },
            label = { Text("Test Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("link_test_title")
        )

        OutlinedTextField(
            value = pdfUrl,
            onValueChange = { pdfUrl = it },
            label = { Text("Paste PDF Link") },
            placeholder = { Text("https://.../paper.pdf") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("pdf_link_input"),
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            trailingIcon = {
                if (pdfUrl.isNotBlank()) {
                    androidx.compose.material3.IconButton(onClick = { pdfUrl = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear link")
                    }
                }
            }
        )

        OutlinedTextField(
            value = duration,
            onValueChange = { if (it.all(Char::isDigit) && it.length <= 4) duration = it },
            label = { Text("Exam Duration (minutes)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("link_duration_input")
        )

        Button(
            onClick = {
                viewModel.convertPdfFromUrl(
                    testTitle = testTitle.ifBlank { "Linked JEE Paper" },
                    pdfUrl = pdfUrl.trim(),
                    answerKeyUri = null,
                    fallbackAnswerText = "",
                    durationMinutes = duration.toIntOrNull()?.coerceIn(1, 600) ?: 180
                )
            },
            enabled = pdfUrl.trim().startsWith("http://") || pdfUrl.trim().startsWith("https://"),
            modifier = Modifier.fillMaxWidth().testTag("generate_cbt_from_link"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GENERATE CBT", fontWeight = FontWeight.Bold)
        }

        if (conversionState.isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = JeeCyan.copy(alpha = 0.10f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = JeeCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Generating CBT...", fontWeight = FontWeight.Bold)
                        Text(
                            conversionState.progressMessage.ifBlank { "Downloading and analysing PDF..." },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (conversionState.errorMessage.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1720))
            ) {
                Text(
                    "${conversionState.errorMessage}",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (conversionState.newlyCreatedTestId != null) {
            Button(
                onClick = {
                    val id = conversionState.newlyCreatedTestId!!
                    viewModel.resetConversionState()
                    viewModel.navigateTo(AppScreen.CbtExam(id))
                },
                modifier = Modifier.fillMaxWidth().testTag("launch_linked_cbt"),
                colors = ButtonDefaults.buttonColors(containerColor = NtaGreenLight, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LAUNCH CBT", fontWeight = FontWeight.Bold)
            }
        }
    }
}
