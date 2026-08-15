package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.TestLinkImporter
import com.example.data.model.GenericJsonImportController
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel
import kotlinx.coroutines.launch

/** Direct, provider-agnostic JSON -> local Test Library -> CBT importer. */
@Composable
fun JsonTestImportFab(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val shareUrl = GenericJsonImportController.import(context, uri)
                TestLinkImporter.import(context, Uri.parse(shareUrl))
                    ?: throw IllegalArgumentException("JSON was valid but contained no importable questions.")
            }.onSuccess { testId ->
                Toast.makeText(context, "Test imported successfully", Toast.LENGTH_SHORT).show()
                viewModel.navigateTo(AppScreen.CbtExam(testId))
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    error.message ?: "Invalid JEE test JSON",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    FloatingActionButton(
        onClick = {
            launcher.launch(arrayOf("application/json", "text/json", "text/plain"))
        },
        modifier = modifier.testTag("fab_import_json_test")
    ) {
        Icon(Icons.Default.UploadFile, contentDescription = "Import JSON Test")
    }
}
