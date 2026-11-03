package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onFinishQuiz: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val selectedAnswers by viewModel.selectedAnswers.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val currentQuiz by viewModel.currentQuiz.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Leave Quiz?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to exit? Your current progress for this quiz will be cancelled.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.resetForNewQuiz()
                        onNavigateBack()
                    }
                ) {
                    Text("Exit", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        currentQuiz?.quizTitle?.ifBlank { "Quiz" } ?: "Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Exit Quiz",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Score: $score",
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No questions found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(onClick = onNavigateBack) {
                        Text("Back to Generator")
                    }
                }
            }
        } else {
            val totalQuestions = questions.size
            val currentQ = questions[currentQuestionIndex]
            val hasAnswered = selectedAnswers.containsKey(currentQuestionIndex)
            val selectedOptionText = selectedAnswers[currentQuestionIndex]
            val isCorrectChoice = hasAnswered && currentQ.correctAnswer.equals(selectedOptionText, ignoreCase = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // Progress Indicator & Step Count
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Question ${currentQuestionIndex + 1} / $totalQuestions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${((currentQuestionIndex + 1).toFloat() / totalQuestions * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFF2563EB).copy(alpha = 0.15f)
                    )
                }

                // Question Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (currentQ.questionType == "TF") "TRUE / FALSE" else "MULTIPLE CHOICE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF2563EB)
                            )
                            if (currentQuiz?.difficulty?.isNotBlank() == true) {
                                Text(
                                    currentQuiz?.difficulty ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = currentQ.question,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Answer Options List
                val options = currentQ.options
                val optionLetters = listOf("A", "B", "C", "D")

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOptionText == optionText
                        val isOptionCorrect = currentQ.correctAnswer.equals(optionText, ignoreCase = true)

                        val optionBorderColor = when {
                            hasAnswered && isOptionCorrect -> Color(0xFF10B981)
                            hasAnswered && isSelected && !isOptionCorrect -> Color(0xFFEF4444)
                            isSelected -> Color(0xFF2563EB)
                            else -> if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
                        }

                        val optionBgColor = when {
                            hasAnswered && isOptionCorrect -> Color(0xFF10B981).copy(alpha = 0.18f)
                            hasAnswered && isSelected && !isOptionCorrect -> Color(0xFFEF4444).copy(alpha = 0.18f)
                            isSelected -> Color(0xFF2563EB).copy(alpha = 0.18f)
                            else -> Color.Transparent
                        }

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (hasAnswered && isOptionCorrect) {
                                        Modifier.border(1.dp, Color(0xFF10B981), RoundedCornerShape(GlassMaterial.DefaultCornerRadius))
                                    } else if (hasAnswered && isSelected && !isOptionCorrect) {
                                        Modifier.border(1.dp, Color(0xFFEF4444), RoundedCornerShape(GlassMaterial.DefaultCornerRadius))
                                    } else if (isSelected) {
                                        Modifier.border(1.dp, Color(0xFF2563EB), RoundedCornerShape(GlassMaterial.DefaultCornerRadius))
                                    } else Modifier
                                ),
                            onClick = if (!hasAnswered) {
                                {
                                    val isCorrect = currentQ.correctAnswer.equals(optionText, ignoreCase = true)
                                    if (isCorrect) {
                                        haptics.perform(LiquidHapticType.Success)
                                    } else {
                                        haptics.perform(LiquidHapticType.Error)
                                    }
                                    viewModel.answerQuestion(optionText)
                                }
                            } else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Badge: Letter or Symbol
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                hasAnswered && isOptionCorrect -> Color(0xFF10B981)
                                                hasAnswered && isSelected && !isOptionCorrect -> Color(0xFFEF4444)
                                                isSelected -> Color(0xFF2563EB)
                                                else -> Color(0xFF2563EB).copy(alpha = 0.15f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val letterLabel = if (currentQ.questionType == "TF") {
                                        if (optionText.startsWith("T", ignoreCase = true)) "T" else "F"
                                    } else {
                                        optionLetters.getOrElse(index) { "${index + 1}" }
                                    }
                                    Text(
                                        letterLabel,
                                        color = if (hasAnswered || isSelected) Color.White else Color(0xFF2563EB),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = optionText,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected || (hasAnswered && isOptionCorrect)) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                if (hasAnswered) {
                                    if (isOptionCorrect) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = "Correct",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            Icons.Filled.Error,
                                            contentDescription = "Incorrect",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Answer Feedback Banner and Explanation Card
                AnimatedVisibility(
                    visible = hasAnswered,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Feedback Status Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isCorrectChoice) Color(0xFF10B981).copy(alpha = 0.18f)
                                    else Color(0xFFEF4444).copy(alpha = 0.18f)
                                )
                                .border(
                                    1.dp,
                                    if (isCorrectChoice) Color(0xFF10B981).copy(alpha = 0.4f)
                                    else Color(0xFFEF4444).copy(alpha = 0.4f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (isCorrectChoice) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = if (isCorrectChoice) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                if (isCorrectChoice) "Correct! Excellent job." else "Incorrect. Review the explanation below.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isCorrectChoice) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }

                        // Explanation Card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Explanation",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7C3AED)
                                    )
                                }
                                Text(
                                    currentQ.explanation.ifBlank { "Answer: ${currentQ.correctAnswer}" },
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Next / Finish Button
                        GlassButton(
                            onClick = {
                                haptics.perform(LiquidHapticType.LightClick)
                                if (currentQuestionIndex < totalQuestions - 1) {
                                    viewModel.nextQuestion()
                                } else {
                                    viewModel.finishQuiz {
                                        onFinishQuiz()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    if (currentQuestionIndex < totalQuestions - 1) "Next" else "Finish Quiz",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
