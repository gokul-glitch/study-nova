package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.QuizEntity
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.QuizViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizGeneratorScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToResult: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val selectedMaterial by viewModel.selectedMaterial.collectAsStateWithLifecycle()
    val isAllMaterialsSelected by viewModel.isAllMaterialsSelected.collectAsStateWithLifecycle()
    val numQuestions by viewModel.numQuestions.collectAsStateWithLifecycle()
    val difficulty by viewModel.difficulty.collectAsStateWithLifecycle()
    val questionType by viewModel.questionType.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val quizHistory by viewModel.quizHistory.collectAsStateWithLifecycle()

    val availableMaterials = remember(materials, selectedSubject) {
        if (selectedSubject != null) {
            materials.filter { it.subjectId == selectedSubject?.id }
        } else emptyList()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        "Quiz Generator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Hero Header Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                "AI QUIZ GENERATOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF2563EB)
                            )
                        }
                        Text(
                            "Create Your Custom Quiz",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Generate structured questions derived strictly from your course notes, textbooks, and summaries with instant feedback.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error Banner with Try Again
            if (errorMessage != null) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(GlassMaterial.DefaultCornerRadius))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "Quiz generation failed. Please try again.",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.clearError()
                                        viewModel.generateQuiz { onNavigateToQuiz() }
                                    }
                                ) {
                                    Text("Try Again", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Check Empty State: No subjects at all
            if (subjects.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Folder,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                "No subjects available",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Add a subject to organize your study notes and generate tailored exams.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            GlassButton(
                                onClick = onNavigateToSubjects,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage Subjects", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Configuration Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Quiz Configuration",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // 1. Subject Selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Select Subject",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                var subjectExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = subjectExpanded,
                                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                                ) {
                                    GlassTextField(
                                        value = selectedSubject?.subjectName ?: "Select Subject",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = subjectExpanded,
                                        onDismissRequest = { subjectExpanded = false }
                                    ) {
                                        subjects.forEach { subject ->
                                            DropdownMenuItem(
                                                text = { Text(subject.subjectName) },
                                                onClick = {
                                                    haptics.perform(LiquidHapticType.Selection)
                                                    viewModel.selectSubject(subject)
                                                    subjectExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Study Material Selector
                            if (selectedSubject != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        "Select Material",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (availableMaterials.isEmpty()) {
                                        // Empty state for material
                                        GlassCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(GlassMaterial.DefaultCornerRadius))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    "No study material available for this subject.",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                GlassButton(
                                                    onClick = onNavigateToLibrary,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(
                                                        Icons.Filled.UploadFile,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Upload Notes", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    } else {
                                        var materialExpanded by remember { mutableStateOf(false) }
                                        val displayMaterialText = when {
                                            isAllMaterialsSelected -> "All materials for ${selectedSubject?.subjectName}"
                                            selectedMaterial != null -> selectedMaterial?.title ?: "Select Material"
                                            else -> "Select Material"
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = materialExpanded,
                                            onExpandedChange = { materialExpanded = !materialExpanded }
                                        ) {
                                            GlassTextField(
                                                value = displayMaterialText,
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialExpanded)
                                                },
                                                modifier = Modifier
                                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                    .fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = materialExpanded,
                                                onDismissRequest = { materialExpanded = false }
                                            ) {
                                                // Option: All materials
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            "All materials for this subject (${availableMaterials.size})",
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF2563EB)
                                                        )
                                                    },
                                                    onClick = {
                                                        haptics.perform(LiquidHapticType.Selection)
                                                        viewModel.selectAllMaterialsForSubject(true)
                                                        materialExpanded = false
                                                    }
                                                )
                                                HorizontalDivider()
                                                availableMaterials.forEach { material ->
                                                    DropdownMenuItem(
                                                        text = { Text(material.title) },
                                                        onClick = {
                                                            haptics.perform(LiquidHapticType.Selection)
                                                            viewModel.selectMaterial(material)
                                                            materialExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Number of Questions (5, 10, 15, 20)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Number of Questions",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(5, 10, 15, 20).forEach { count ->
                                        val isSelected = numQuestions == count
                                        PillSelector(
                                            text = "$count",
                                            isSelected = isSelected,
                                            onClick = {
                                                haptics.perform(LiquidHapticType.Selection)
                                                viewModel.setNumQuestions(count)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // 4. Difficulty (Easy, Medium, Hard)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Difficulty",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                                        val isSelected = difficulty.equals(diff, ignoreCase = true)
                                        PillSelector(
                                            text = diff,
                                            isSelected = isSelected,
                                            onClick = {
                                                haptics.perform(LiquidHapticType.Selection)
                                                viewModel.setDifficulty(diff)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // 5. Question Type (Multiple Choice, True / False, Mixed)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Question Type",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Multiple Choice", "True / False", "Mixed").forEach { type ->
                                        val isSelected = questionType == type
                                        val label = when (type) {
                                            "Multiple Choice" -> "MCQ"
                                            "True / False" -> "True / False"
                                            else -> "Mixed"
                                        }
                                        PillSelector(
                                            text = label,
                                            isSelected = isSelected,
                                            onClick = {
                                                haptics.perform(LiquidHapticType.Selection)
                                                viewModel.setQuestionType(type)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Generate Quiz Action Button
                item {
                    val canGenerate = selectedSubject != null &&
                            (selectedMaterial != null || isAllMaterialsSelected) &&
                            availableMaterials.isNotEmpty() &&
                            !isLoading

                    GlassButton(
                        onClick = {
                            haptics.perform(LiquidHapticType.LightClick)
                            viewModel.generateQuiz {
                                onNavigateToQuiz()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canGenerate
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Generating your quiz...", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Quiz", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Quiz History Section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "QUIZ HISTORY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (quizHistory.isNotEmpty()) {
                        Text(
                            "${quizHistory.size} completed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (quizHistory.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No quiz history yet. Generate your first quiz above!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(quizHistory) { quiz ->
                    QuizHistoryCard(
                        quiz = quiz,
                        onClick = {
                            haptics.perform(LiquidHapticType.LightClick)
                            viewModel.loadCompletedQuiz(quiz) {
                                onNavigateToResult()
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }
}

@Composable
fun PillSelector(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF2563EB)
                else (if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF))
            )
            .border(
                1.dp,
                if (isSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else (if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A))
        )
    }
}

@Composable
fun QuizHistoryCard(
    quiz: QuizEntity,
    onClick: () -> Unit
) {
    val dateString = remember(quiz.createdAt) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(quiz.createdAt))
    }
    val percentage = quiz.percentage.toInt().takeIf { it > 0 }
        ?: if (quiz.totalQuestions > 0) ((quiz.score.toFloat() / quiz.totalQuestions) * 100).toInt() else 0
    val scoreColor = if (percentage >= 80) Color(0xFF10B981) else if (percentage >= 50) Color(0xFFF59E0B) else Color(0xFFEF4444)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.18f))
                    .border(1.dp, scoreColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$percentage%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    quiz.quizTitle.ifBlank { "Quiz" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        quiz.subjectName.ifBlank { "General" },
                        fontSize = 12.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium
                    )
                    Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        quiz.difficulty,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${quiz.correctCount}/${quiz.totalQuestions} correct",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    dateString,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open quiz review",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
