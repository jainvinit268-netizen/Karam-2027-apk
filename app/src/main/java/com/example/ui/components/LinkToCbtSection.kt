package com.example.ui.components

import android.net.Uri
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.TestLinkImporter
import com.example.ui.theme.JeeCyan
import com.example.ui.theme.NtaGreenLight
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel
import kotlinx.coroutines.launch

@Composable
fun LinkToCbtSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var testLink by remember { mutableStateOf("") }
    var importing by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }

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
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = JeeCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Open Saved CBT Test", fontWeight = FontWeight.Bold, color = JeeCyan)
                    Text(
                        "Paste the KARAM test link generated for you. The saved test opens directly in the existing NTA-style CBT flow.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedTextField(
            value = testLink,
            onValueChange = {
                testLink = it
                importError = null
            },
            label = { Text("Paste KARAM Test Link") },
            placeholder = { Text("karam://test/...") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth().testTag("test_link_input"),
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            trailingIcon = {
                if (testLink.isNotBlank()) {
                    androidx.compose.material3.IconButton(onClick = { testLink = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear link")
                    }
                }
            }
        )

        Button(
            onClick = {
                val raw = testLink.trim()
                scope.launch {
                    importing = true
                    importError = null
                    try {
                        require(raw.startsWith("karam://test/")) { "Invalid KARAM test link." }
                        val testId = TestLinkImporter.import(context, Uri.parse(raw))
                        require(!testId.isNullOrBlank()) { "Test link could not be imported." }
                        viewModel.resetConversionState()
                        viewModel.navigateTo(AppScreen.CbtExam(testId))
                    } catch (e: Exception) {
                        importError = e.localizedMessage ?: "Unable to open this test link."
                    } finally {
                        importing = false
                    }
                }
            },
            enabled = !importing && testLink.trim().startsWith("karam://test/"),
            modifier = Modifier.fillMaxWidth().testTag("open_test_link"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = JeeCyan, contentColor = Color.Black)
        ) {
            if (importing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (importing) "OPENING TEST..." else "OPEN TEST", fontWeight = FontWeight.Bold)
        }

        importError?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1720))
            ) {
                Text(it, modifier = Modifier.padding(16.dp), color = Color.White)
            }
        }
    }
}
