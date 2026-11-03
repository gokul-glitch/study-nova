package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StudyMaterialViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var isDarkTheme by remember { mutableStateOf(false) }
      val studyViewModel: StudyMaterialViewModel = viewModel()
      val summaryViewModel: com.example.viewmodel.SummaryViewModel = viewModel()
      val authViewModel: com.example.viewmodel.AuthViewModel = viewModel()
      val subjectsViewModel: com.example.viewmodel.SubjectsViewModel = viewModel()
      val aiTutorViewModel: com.example.viewmodel.AITutorViewModel = viewModel()
      val quizViewModel: com.example.viewmodel.QuizViewModel = viewModel()
      val flashcardsViewModel: com.example.viewmodel.FlashcardsViewModel = viewModel()
      val statisticsViewModel: com.example.viewmodel.StatisticsViewModel = viewModel()
      
      MyApplicationTheme(darkTheme = isDarkTheme) {
        AppNavigation(
            isDarkTheme = isDarkTheme,
            toggleTheme = { isDarkTheme = !isDarkTheme },
            studyViewModel = studyViewModel,
            summaryViewModel = summaryViewModel,
            authViewModel = authViewModel,
            subjectsViewModel = subjectsViewModel,
            aiTutorViewModel = aiTutorViewModel,
            quizViewModel = quizViewModel,
            flashcardsViewModel = flashcardsViewModel,
            statisticsViewModel = statisticsViewModel
        )
      }
    }
  }
}

