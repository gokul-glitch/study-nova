package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.SetStudyGoalsDialog
import com.example.ui.components.StudyGoalsCard
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.QuizTrendPoint
import com.example.viewmodel.StatisticsViewModel
import com.example.viewmodel.SubjectPerformance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit,
    userId: String,
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToAITutor: () -> Unit = {},
    onNavigateToFlashcards: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSetGoalsDialog by remember { mutableStateOf(false) }

    if (showSetGoalsDialog) {
        SetStudyGoalsDialog(
            initialGoal = uiState.studyGoal,
            currentCadence = uiState.activeGoalPeriod,
            onDismiss = { showSetGoalsDialog = false },
            onSaveGoals = { dailyTime, dailyAccuracy, weeklyTime, weeklyAccuracy, cadence ->
                viewModel.saveStudyGoals(
                    dailyStudyTime = dailyTime,
                    dailyQuizAccuracy = dailyAccuracy,
                    weeklyStudyTime = weeklyTime,
                    weeklyQuizAccuracy = weeklyAccuracy,
                    period = cadence
                )
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Study Analytics",
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (uiState.totalMaterials == 0 && uiState.totalQuizzes == 0 && uiState.recentActivities.isEmpty()) {
            // Empty State for Brand New Student - allow setting targets and seeing rings
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                item {
                    StudyGoalsCard(
                        progress = uiState.activeGoalProgress,
                        activePeriod = uiState.activeGoalPeriod,
                        onPeriodChanged = { viewModel.setGoalPeriod(it) },
                        onEditGoalsClicked = { showSetGoalsDialog = true }
                    )
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.School,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                "Start your study journey",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Configure your daily & weekly targets above. As you study notes or take practice quizzes, your progress rings and performance trends will update dynamically.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            GlassButton(
                                onClick = onNavigateToLibrary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Upload Notes & Study", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                // 1. Study Streak Card
                item {
                    StreakCard(
                        currentStreak = uiState.streakInfo.currentStreak,
                        longestStreak = uiState.streakInfo.longestStreak,
                        activeDays = uiState.streakInfo.activeDays
                    )
                }

                // 2. Study Goals & Progress Rings Card
                item {
                    StudyGoalsCard(
                        progress = uiState.activeGoalProgress,
                        activePeriod = uiState.activeGoalPeriod,
                        onPeriodChanged = { viewModel.setGoalPeriod(it) },
                        onEditGoalsClicked = { showSetGoalsDialog = true }
                    )
                }

                // 3. Personalized Study Recommendation Card
                uiState.recommendation?.let { rec ->
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "RECOMMENDED FOR YOU",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Text(
                                    rec.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    rec.description,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                GlassOutlinedButton(
                                    onClick = {
                                        haptics.perform(LiquidHapticType.LightClick)
                                        when (rec.actionType) {
                                            "QUIZ" -> onNavigateToQuiz()
                                            "LIBRARY" -> onNavigateToLibrary()
                                            "AI_TUTOR" -> onNavigateToAITutor()
                                            "FLASHCARDS" -> onNavigateToFlashcards()
                                            else -> onNavigateToQuiz()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(rec.actionLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Overall Progress Overview Grid
                item {
                    Text(
                        "OVERALL PROGRESS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val formattedStudyTime = if (uiState.totalStudyTimeMinutes >= 60) {
                        "${uiState.totalStudyTimeMinutes / 60}h ${uiState.totalStudyTimeMinutes % 60}m"
                    } else {
                        "${uiState.totalStudyTimeMinutes} min"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Study Time",
                            value = formattedStudyTime,
                            icon = Icons.Filled.Timer,
                            iconTint = Color(0xFF2563EB),
                            iconBg = Color(0xFFDBEAFE)
                        )
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Quiz Accuracy",
                            value = "${uiState.quizAverageScore}%",
                            icon = Icons.Filled.AutoAwesome,
                            iconTint = Color(0xFF10B981),
                            iconBg = Color(0xFFD1FAE5)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Quizzes Taken",
                            value = "${uiState.totalQuizzes}",
                            icon = Icons.Filled.Psychology,
                            iconTint = Color(0xFF7C3AED),
                            iconBg = Color(0xFFEDE9FE)
                        )
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Questions",
                            value = "${uiState.totalQuestionsAnswered}",
                            icon = Icons.Filled.CheckCircle,
                            iconTint = Color(0xFFEA580C),
                            iconBg = Color(0xFFFFEDD5)
                        )
                    }
                }

                // 4. Performance Trend Chart
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    PerformanceTrendSection(
                        selectedRange = uiState.selectedTrendRange,
                        onRangeSelected = { viewModel.setSelectedTrendRange(it) },
                        trendPoints = if (uiState.selectedTrendRange == "7D") uiState.trendPoints7Days else uiState.trendPoints30Days
                    )
                }

                // 5. Subject Performance Breakdown
                if (uiState.subjectPerformance.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SUBJECT PERFORMANCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${uiState.subjectPerformance.size} subjects",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(uiState.subjectPerformance) { subject ->
                        SubjectPerformanceCard(
                            performance = subject,
                            onTakeQuiz = { onNavigateToQuiz() }
                        )
                    }
                }

                // 6. AI Study Analysis Section
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    AIStudyAnalysisSection(
                        analysis = uiState.aiAnalysis,
                        isLoading = uiState.isAnalyzingAi,
                        error = uiState.aiAnalysisError,
                        onRequestAnalysis = {
                            haptics.perform(LiquidHapticType.LightClick)
                            viewModel.requestAiStudyAnalysis()
                        }
                    )
                }

                // 7. Recent Activity List
                if (uiState.recentActivities.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "RECENT ACTIVITY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(uiState.recentActivities.take(8)) { activity ->
                        val dateString = remember(activity.timestamp) {
                            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(activity.timestamp))
                        }
                        val (title, icon, color) = when (activity.activityType) {
                            "COMPLETE_QUIZ" -> Triple("Completed AI Quiz", Icons.Filled.Psychology, Color(0xFF2563EB))
                            "GENERATE_SUMMARY" -> Triple("Generated AI Summary", Icons.Filled.Description, Color(0xFF7C3AED))
                            "STUDY_FLASHCARDS" -> Triple("Reviewed Flashcards", Icons.Filled.Style, Color(0xFFF43F5E))
                            "USE_AI_TUTOR" -> Triple("AI Tutor Chat Session", Icons.Filled.AutoAwesome, Color(0xFF06B6D4))
                            "UPLOAD_MATERIAL" -> Triple("Uploaded Study Document", Icons.Filled.UploadFile, Color(0xFF10B981))
                            else -> Triple("Study Session", Icons.Filled.Check, Color(0xFF2563EB))
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(color.copy(alpha = 0.15f))
                                        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        dateString,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun StreakCard(currentStreak: Int, longestStreak: Int, activeDays: Int) {
    val isDark = isSystemInDarkTheme()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔥", fontSize = 22.sp)
                    Text(
                        "$currentStreak Days",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )
                }
                Text(
                    "Current Study Streak",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$longestStreak d",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Longest",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$activeDays d",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                    Text(
                        "Active Days",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color
) {
    val isDark = isSystemInDarkTheme()
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (isDark) 0.12f else 0.45f))
                    .border(
                        0.5.dp,
                        Color.White.copy(alpha = if (isDark) 0.25f else 0.80f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PerformanceTrendSection(
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
    trendPoints: List<QuizTrendPoint>
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "PERFORMANCE TREND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF2563EB)
                    )
                    Text(
                        "Quiz Accuracy Over Time",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Range Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                        .padding(2.dp)
                ) {
                    listOf("7D", "30D").forEach { range ->
                        val isSelected = selectedRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                .clickable {
                                    haptics.perform(LiquidHapticType.Selection)
                                    onRangeSelected(range)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (range == "7D") "7 Days" else "30 Days",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (trendPoints.size < 2) {
                // Not enough data state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.02f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Not enough data yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Complete at least 2 quizzes in this period to view your trend curve.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Render Canvas Chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val leftPadding = 36.dp.toPx()
                    val rightPadding = 16.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 24.dp.toPx()

                    val chartWidth = w - leftPadding - rightPadding
                    val chartHeight = h - topPadding - bottomPadding

                    // Draw Horizontal Guidelines (100%, 50%, 0%)
                    val guidelines = listOf(1.0f, 0.5f, 0.0f)
                    guidelines.forEach { fraction ->
                        val y = topPadding + (1f - fraction) * chartHeight
                        drawLine(
                            color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                            start = Offset(leftPadding, y),
                            end = Offset(w - rightPadding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build Curve Points
                    val points = trendPoints.mapIndexed { index, point ->
                        val x = leftPadding + (index.toFloat() / (trendPoints.size - 1)) * chartWidth
                        val y = topPadding + (1f - (point.percentage.coerceIn(0f, 100f) / 100f)) * chartHeight
                        Offset(x, y)
                    }

                    // Fill Gradient Area Under Curve
                    val fillPath = Path().apply {
                        moveTo(points.first().x, h - bottomPadding)
                        lineTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                        lineTo(points.last().x, h - bottomPadding)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2563EB).copy(alpha = 0.35f),
                                Color(0xFF2563EB).copy(alpha = 0.02f)
                            ),
                            startY = topPadding,
                            endY = h - bottomPadding
                        )
                    )

                    // Draw Stroke Path
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = Color(0xFF2563EB),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Point Dots
                    points.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF2563EB),
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // X-Axis Date Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        trendPoints.first().formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (trendPoints.size > 2) {
                        Text(
                            trendPoints[trendPoints.size / 2].formattedDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        trendPoints.last().formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectPerformanceCard(
    performance: SubjectPerformance,
    onTakeQuiz: () -> Unit
) {
    val activityColor = when (performance.studyActivityLevel) {
        "High" -> Color(0xFF10B981)
        "Medium" -> Color(0xFF2563EB)
        "Low" -> Color(0xFFF59E0B)
        else -> Color.Gray
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        performance.subjectName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${performance.materialsCount} study materials",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Activity Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(activityColor.copy(alpha = 0.15f))
                        .border(1.dp, activityColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Activity: ${performance.studyActivityLevel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = activityColor
                    )
                }
            }

            // Accuracy Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quiz Accuracy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (performance.quizzesCompleted > 0) "${performance.accuracy}%" else "No quizzes yet",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (performance.accuracy >= 75) Color(0xFF10B981) else Color(0xFF2563EB)
                    )
                }
                LinearProgressIndicator(
                    progress = { (performance.accuracy.toFloat() / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (performance.accuracy >= 75) Color(0xFF10B981) else Color(0xFF2563EB),
                    trackColor = Color(0xFF2563EB).copy(alpha = 0.12f)
                )
            }

            // Metrics Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Quizzes: ${performance.quizzesCompleted}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Questions: ${performance.questionsAnswered}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Correct: ${performance.correctAnswers}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AIStudyAnalysisSection(
    analysis: com.example.service.AIStudyAnalysis?,
    isLoading: Boolean,
    error: String?,
    onRequestAnalysis: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "AI STUDY ANALYSIS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7C3AED)
                    )
                }
                if (analysis != null) {
                    IconButton(
                        onClick = onRequestAnalysis,
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh Analysis",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (analysis != null) {
                // Strengths
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Key Strengths",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    analysis.strengths.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Text(
                                item,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Areas to Improve
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Areas to Improve",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )
                    analysis.areasToImprove.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Text(
                                item,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Recommended Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF7C3AED).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Recommended Next Action",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                        Text(
                            analysis.recommendedAction,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    "Receive an instant AI evaluation of your strengths, subject weak points, and actionable next steps based on your actual study activities.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (error != null) {
                    Text(
                        error,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                GlassButton(
                    onClick = onRequestAnalysis,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing your study records...")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Study Analysis", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
