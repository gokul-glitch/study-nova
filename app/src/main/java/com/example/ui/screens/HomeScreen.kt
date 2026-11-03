package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.PersonalizedRecommendation
import com.example.viewmodel.StatisticsViewModel
import com.example.viewmodel.SubjectsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSubjects: () -> Unit,
    subjectsViewModel: SubjectsViewModel?,
    statisticsViewModel: StatisticsViewModel?,
    authViewModel: AuthViewModel?,
    onNavigateToProfile: () -> Unit,
    onNavigateToAITutor: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToStats: () -> Unit,
    onImportDocument: (Uri, Long?) -> Unit,
    onNavigateToQuizForSubject: (Long) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    val subjects by subjectsViewModel?.subjects?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val statsState by statisticsViewModel?.uiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(com.example.viewmodel.StatisticsUiState()) }
    val userProfile by authViewModel?.userProfile?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    var pendingFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                pendingFileUri = uri
            }
        }
    )

    // Subject selection dialog for document import
    if (pendingFileUri != null) {
        AlertDialog(
            onDismissRequest = { pendingFileUri = null },
            title = {
                Text(
                    "Assign to Subject",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Choose a subject folder for this document or continue with general:",
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    subjects.forEach { subject ->
                        TextButton(
                            onClick = {
                                onImportDocument(pendingFileUri!!, subject.id)
                                pendingFileUri = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    subject.subjectName,
                                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onImportDocument(pendingFileUri!!, null)
                    pendingFileUri = null
                }) {
                    Text("Skip / General", color = Color(0xFF2563EB))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingFileUri = null }) {
                    Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Column {
                        Text(
                            "Welcome back,",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        Text(
                            userProfile?.name?.takeIf { it.isNotBlank() } ?: "Student",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile?.profilePictureUri != null) {
                                AsyncImage(
                                    model = userProfile?.profilePictureUri,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Today's Study Activity Card (Single transparent glass panel)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "TODAY'S STUDY ACTIVITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Daily Progress",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            )
                        }

                        // Minimal Streak indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🔥", fontSize = 14.sp)
                            Text(
                                "${statsState.streakInfo.currentStreak}d Streak",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA580C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4 Key Daily Metrics Row (Horizontal statistics with subtle dividers, no boxes)
                    val todayMinutesFormatted = if (statsState.todayStudyStats.studyTimeMinutes >= 60) {
                        "${statsState.todayStudyStats.studyTimeMinutes / 60}h ${statsState.todayStudyStats.studyTimeMinutes % 60}m"
                    } else {
                        "${statsState.todayStudyStats.studyTimeMinutes}m"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatMetricItem(
                            icon = Icons.Filled.Timer,
                            value = todayMinutesFormatted,
                            label = "Study Time",
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(0.5.dp)
                                .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF0F172A).copy(alpha = 0.08f))
                        )
                        StatMetricItem(
                            icon = Icons.Filled.Description,
                            value = "${statsState.todayStudyStats.materialsStudied}",
                            label = "Materials",
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(0.5.dp)
                                .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF0F172A).copy(alpha = 0.08f))
                        )
                        StatMetricItem(
                            icon = Icons.Filled.Psychology,
                            value = "${statsState.todayStudyStats.questionsAnswered}",
                            label = "Questions",
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(0.5.dp)
                                .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF0F172A).copy(alpha = 0.08f))
                        )
                        StatMetricItem(
                            icon = Icons.Filled.AutoAwesome,
                            value = if (statsState.todayStudyStats.quizAccuracy != null) "${statsState.todayStudyStats.quizAccuracy}%" else "—",
                            label = "Accuracy",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Subtle separator line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFF0F172A).copy(alpha = 0.06f))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Goal (Clean glass row element, no nested box, no grey rectangle)
                    val dailyGoal = statsState.dailyGoalProgress
                    val targetStudyTimeStr = if (dailyGoal.studyTimeMinutesTarget >= 60) {
                        val h = dailyGoal.studyTimeMinutesTarget / 60
                        val m = dailyGoal.studyTimeMinutesTarget % 60
                        if (m > 0) "${h}h ${m}m" else "${h}h"
                    } else "${dailyGoal.studyTimeMinutesTarget}m"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToStats() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.RadioButtonChecked,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                "Daily Goal: $todayMinutesFormatted / $targetStudyTimeStr",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            )
                            Text(
                                "• ${dailyGoal.overallCompletionPercentage}% Rings Filled",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Tap to view",
                                fontSize = 12.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Goals",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subtle separator line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFF0F172A).copy(alpha = 0.06f))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // View Full Analytics Button (Transparent glass button, thin border, dark text, small arrow)
                    GlassOutlinedButton(
                        onClick = onNavigateToStats,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "View Full Analytics & Trends",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Study Recommendation Card (Single transparent glass panel, no nested boxes)
            val rec = statsState.recommendation ?: PersonalizedRecommendation(
                title = "Start Your Study Journey",
                description = "Upload notes or lecture slides to enable automated AI summaries, flashcards, and tailored practice quizzes.",
                actionLabel = "Upload Notes",
                actionType = "LIBRARY"
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            "YOUR STUDY RECOMMENDATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                    Text(
                        rec.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    )
                    Text(
                        rec.description,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                haptics.perform(LiquidHapticType.LightClick)
                                when (rec.actionType) {
                                    "QUIZ" -> {
                                        if (rec.subjectId != null) onNavigateToQuizForSubject(rec.subjectId)
                                        else onNavigateToQuiz()
                                    }
                                    "LIBRARY" -> onNavigateToLibrary()
                                    "AI_TUTOR" -> onNavigateToAITutor()
                                    "FLASHCARDS" -> onNavigateToFlashcards()
                                    else -> onNavigateToQuiz()
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                rec.actionLabel,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Quick Actions Section Header
            Text(
                "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Four Lightweight Floating Glass Tiles (Directly on background, no parent box!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Psychology,
                    title = "Quiz Generator",
                    subtitle = "AI practice quiz",
                    onClick = onNavigateToQuiz
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.AutoAwesome,
                    title = "AI Tutor",
                    subtitle = "Search Grounded",
                    onClick = onNavigateToAITutor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.UploadFile,
                    title = "Upload Notes",
                    subtitle = "PDF, Word, Text",
                    onClick = { filePickerLauncher.launch(arrayOf("*/*")) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Style,
                    title = "Flashcards",
                    subtitle = "Spaced repetition",
                    onClick = onNavigateToFlashcards
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Recent Activity Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT ACTIVITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Text(
                    "View Stats",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier
                        .clickable { onNavigateToStats() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student's Actual Recent Activities (Real Data)
            if (statsState.recentActivities.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No study activity yet. Take a quiz or review notes to begin tracking your progress!",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            } else {
                statsState.recentActivities.take(4).forEach { activity ->
                    val dateString = remember(activity.timestamp) {
                        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(activity.timestamp))
                    }
                    val (title, subtitle, icon, action) = when (activity.activityType) {
                        "COMPLETE_QUIZ" -> Tuple4(
                            "Completed AI Quiz",
                            "Score recorded • $dateString",
                            Icons.Filled.Psychology,
                            onNavigateToQuiz
                        )
                        "GENERATE_SUMMARY" -> Tuple4(
                            "Generated Study Summary",
                            "Document processed • $dateString",
                            Icons.Filled.Description,
                            onNavigateToLibrary
                        )
                        "STUDY_FLASHCARDS" -> Tuple4(
                            "Flashcards Study Session",
                            "Spaced repetition • $dateString",
                            Icons.Filled.Style,
                            onNavigateToFlashcards
                        )
                        "USE_AI_TUTOR" -> Tuple4(
                            "AI Tutor Conversation",
                            "Grounded learning • $dateString",
                            Icons.Filled.AutoAwesome,
                            onNavigateToAITutor
                        )
                        "UPLOAD_MATERIAL" -> Tuple4(
                            "Uploaded Study Material",
                            "Added to Library • $dateString",
                            Icons.Filled.UploadFile,
                            onNavigateToLibrary
                        )
                        else -> Tuple4(
                            "Study Session",
                            dateString,
                            Icons.Filled.CheckCircle,
                            onNavigateToStats
                        )
                    }

                    RecentActivityItem(
                        icon = icon,
                        title = title,
                        subtitle = subtitle,
                        onClick = action
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val a: A, val b: B, val c: C, val d: D
)

@Composable
fun StatMetricItem(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
            modifier = Modifier.size(18.dp)
        )
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        )
        Text(
            label,
            fontSize = 11.sp,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun RecentActivityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
