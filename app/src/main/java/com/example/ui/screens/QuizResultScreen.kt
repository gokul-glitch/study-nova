package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    viewModel: QuizViewModel,
    onReviewAnswers: () -> Unit,
    onRetryQuiz: () -> Unit,
    onGenerateNewQuiz: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    val currentQuiz by viewModel.currentQuiz.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()

    val totalQuestions = questions.size.takeIf { it > 0 } ?: (currentQuiz?.totalQuestions ?: 0)
    val correctCount = currentQuiz?.correctCount ?: score
    val incorrectCount = currentQuiz?.incorrectCount ?: maxOf(0, totalQuestions - correctCount)
    val percentage = if (totalQuestions > 0) ((correctCount.toFloat() / totalQuestions) * 100).toInt() else 0

    val resultColor = when {
        percentage >= 80 -> Color(0xFF10B981)
        percentage >= 50 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    val appraisalMessage = when {
        percentage >= 90 -> "Outstanding! You demonstrated near perfect mastery of this material."
        percentage >= 80 -> "Great job! You have a solid grasp of this study subject."
        percentage >= 60 -> "Good effort! A quick review of missed concepts will boost your score."
        else -> "Keep studying! Review your notes and try another quiz to build confidence."
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        "Quiz Results",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "QUIZ COMPLETED!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = resultColor
            )

            Text(
                currentQuiz?.quizTitle?.ifBlank { "Study Quiz" } ?: "Study Quiz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(resultColor.copy(alpha = 0.12f))
                    .border(
                        width = 2.dp,
                        color = resultColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$percentage%",
                        fontSize = 34.sp,
                        color = resultColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$correctCount / $totalQuestions",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = appraisalMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Performance Breakdown Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Quiz Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ResultDetailRow(label = "Subject", value = currentQuiz?.subjectName?.ifBlank { "General" } ?: "General")
                    ResultDetailRow(label = "Material Used", value = currentQuiz?.materialTitle?.ifBlank { "All Subject Notes" } ?: "All Subject Notes")
                    ResultDetailRow(label = "Difficulty", value = currentQuiz?.difficulty ?: "Medium")
                    ResultDetailRow(label = "Question Type", value = currentQuiz?.questionType ?: "Multiple Choice")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Text("Correct Answers", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$correctCount", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF10B981))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Text("Incorrect Answers", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$incorrectCount", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Review Answers
                GlassButton(
                    onClick = {
                        haptics.perform(LiquidHapticType.LightClick)
                        onReviewAnswers()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review Answers", fontWeight = FontWeight.Bold)
                    }
                }

                // 2. Retry Quiz
                GlassOutlinedButton(
                    onClick = {
                        haptics.perform(LiquidHapticType.LightClick)
                        viewModel.retryCurrentQuiz()
                        onRetryQuiz()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Quiz", fontWeight = FontWeight.SemiBold)
                    }
                }

                // 3. Generate New Quiz
                GlassOutlinedButton(
                    onClick = {
                        haptics.perform(LiquidHapticType.LightClick)
                        viewModel.resetForNewQuiz()
                        onGenerateNewQuiz()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate New Quiz", fontWeight = FontWeight.SemiBold)
                    }
                }

                // 4. Back to Home
                TextButton(
                    onClick = {
                        haptics.perform(LiquidHapticType.Selection)
                        viewModel.resetForNewQuiz()
                        onNavigateHome()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ResultDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
