package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JeeCyan
import com.example.ui.theme.NtaRedLight

@Composable
fun ExitTestConfirmDialog(
    onContinueTest: () -> Unit,
    onSaveAndExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinueTest,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = NtaRedLight,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Exit Test?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Do you want to exit the test?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Your current responses and timer will be paused. You can return to the test library anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onContinueTest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JeeCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_continue_test")
            ) {
                Text("Continue Test", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onSaveAndExit,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_and_exit")
            ) {
                Text("Save & Exit", fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("exit_test_dialog")
    )
}
