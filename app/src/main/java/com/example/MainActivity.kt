package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.CbtExamScreen
import com.example.ui.screens.ConvertPdfScreen
import com.example.ui.screens.ForensicReportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OpeningBlessingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: JeeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JeeMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun JeeMainApp(viewModel: JeeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
        when (screen) {
            is AppScreen.SplashInvocation -> OpeningBlessingScreen(viewModel = viewModel)
            is AppScreen.TestLibrary -> HomeScreen(viewModel = viewModel)
            is AppScreen.ConvertPdf -> ConvertPdfScreen(viewModel = viewModel)
            is AppScreen.CbtExam -> CbtExamScreen(viewModel = viewModel)
            is AppScreen.ForensicAnalysis -> ForensicReportScreen(attemptId = screen.attemptId, viewModel = viewModel)
        }
    }
}

