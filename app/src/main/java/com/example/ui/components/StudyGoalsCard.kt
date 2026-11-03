package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.StudyGoalEntity
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.StudyGoalProgress

@Composable
fun StudyGoalsCard(
    progress: StudyGoalProgress,
    activePeriod: String,
    onPeriodChanged: (String) -> Unit,
    onEditGoalsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_goals_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title + Cadence Selector + Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.TrackChanges,
                            contentDescription = "Study Goals",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "STUDY GOALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF2563EB)
                        )
                        Text(
                            if (activePeriod == "WEEKLY") "Weekly Targets" else "Daily Targets",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cadence Toggle Pills
                    CadencePillSelector(
                        selectedCadence = activePeriod,
                        onCadenceSelected = {
                            haptics.perform(LiquidHapticType.LightClick)
                            onPeriodChanged(it)
                        },
                        isDark = isDark
                    )

                    // Edit Goals Button
                    IconButton(
                        onClick = {
                            haptics.perform(LiquidHapticType.LightClick)
                            onEditGoalsClicked()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (isDark) 0.10f else 0.40f))
                            .border(
                                0.6.dp,
                                Color.White.copy(alpha = if (isDark) 0.20f else 0.70f),
                                CircleShape
                            )
                            .testTag("edit_study_goals_button")
                    ) {
                        Icon(
                            Icons.Filled.Tune,
                            contentDescription = "Edit Goals",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Progress Rings & Targets Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dual Concentric Progress Rings
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ConcentricProgressRings(
                        studyTimeProgress = progress.studyTimeProgress,
                        quizAccuracyProgress = progress.quizAccuracyProgress,
                        overallPercentage = progress.overallCompletionPercentage,
                        isAllMet = progress.isStudyTimeMet && progress.isQuizAccuracyMet,
                        isDark = isDark
                    )
                }

                // Metric Breakdown Rows
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Study Time Ring Tile
                    GoalMetricTile(
                        title = "Study Time",
                        current = formatStudyMinutes(progress.studyTimeMinutesCurrent),
                        target = formatStudyMinutes(progress.studyTimeMinutesTarget),
                        progress = progress.studyTimeProgress,
                        isMet = progress.isStudyTimeMet,
                        primaryColor = Color(0xFF0284C7),
                        accentColor = Color(0xFF38BDF8),
                        isDark = isDark,
                        statusSubtitle = if (progress.isStudyTimeMet) {
                            "Target Met! 🌟"
                        } else {
                            val remaining = (progress.studyTimeMinutesTarget - progress.studyTimeMinutesCurrent).coerceAtLeast(0)
                            "${formatStudyMinutes(remaining)} to go"
                        }
                    )

                    // 2. Quiz Accuracy Ring Tile
                    val accuracySubtitle = when {
                        progress.quizAccuracyCurrent == null -> "Take a quiz to score"
                        progress.isQuizAccuracyMet -> "Exceeding target! 🎯"
                        else -> {
                            val diff = progress.quizAccuracyTarget - (progress.quizAccuracyCurrent ?: 0)
                            "+$diff% to reach goal"
                        }
                    }

                    GoalMetricTile(
                        title = "Quiz Accuracy",
                        current = if (progress.quizAccuracyCurrent != null) "${progress.quizAccuracyCurrent}%" else "—",
                        target = "${progress.quizAccuracyTarget}%",
                        progress = progress.quizAccuracyProgress,
                        isMet = progress.isQuizAccuracyMet,
                        primaryColor = Color(0xFF059669),
                        accentColor = Color(0xFF34D399),
                        isDark = isDark,
                        statusSubtitle = accuracySubtitle
                    )
                }
            }

            // Motivational status banner
            val bannerText = when {
                progress.isStudyTimeMet && progress.isQuizAccuracyMet ->
                    "🎉 All targets completed! Superb dedication to your learning goals."
                progress.isStudyTimeMet ->
                    "⏱️ Study time target achieved! Complete a quiz to fill your accuracy ring."
                progress.isQuizAccuracyMet ->
                    "🎯 High quiz accuracy! Keep learning to complete your study time ring."
                progress.studyTimeMinutesCurrent > 0 || progress.quizAccuracyCurrent != null ->
                    "⚡ Great momentum! Keep studying to complete today's targets."
                else ->
                    "🚀 Start studying your notes or take a quiz to fill your progress rings!"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.40f))
                    .border(
                        0.5.dp,
                        Color.White.copy(alpha = if (isDark) 0.18f else 0.65f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        bannerText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ConcentricProgressRings(
    studyTimeProgress: Float,
    quizAccuracyProgress: Float,
    overallPercentage: Int,
    isAllMet: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedTimeProgress by animateFloatAsState(
        targetValue = studyTimeProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "timeProgress"
    )

    val animatedAccuracyProgress by animateFloatAsState(
        targetValue = quizAccuracyProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "accuracyProgress"
    )

    val ringTrackColorTime = if (isDark) Color(0xFF0284C7).copy(alpha = 0.20f) else Color(0xFF38BDF8).copy(alpha = 0.20f)
    val ringTrackColorAccuracy = if (isDark) Color(0xFF059669).copy(alpha = 0.20f) else Color(0xFF34D399).copy(alpha = 0.20f)

    val timeRingGradient = Brush.sweepGradient(
        listOf(
            Color(0xFF06B6D4),
            Color(0xFF2563EB),
            Color(0xFF3B82F6),
            Color(0xFF06B6D4)
        )
    )

    val accuracyRingGradient = Brush.sweepGradient(
        listOf(
            Color(0xFF34D399),
            Color(0xFF10B981),
            Color(0xFF059669),
            Color(0xFF34D399)
        )
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val outerRadius = (size.minDimension - strokeWidth) / 2
            val innerRadius = outerRadius - strokeWidth - 6.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)

            // Outer Ring Track (Study Time)
            drawCircle(
                color = ringTrackColorTime,
                radius = outerRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Outer Ring Progress (Study Time)
            if (animatedTimeProgress > 0f) {
                drawArc(
                    brush = timeRingGradient,
                    startAngle = -90f,
                    sweepAngle = animatedTimeProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Inner Ring Track (Quiz Accuracy)
            drawCircle(
                color = ringTrackColorAccuracy,
                radius = innerRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Inner Ring Progress (Quiz Accuracy)
            if (animatedAccuracyProgress > 0f) {
                drawArc(
                    brush = accuracyRingGradient,
                    startAngle = -90f,
                    sweepAngle = animatedAccuracyProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                    size = Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isAllMet) {
                Icon(
                    Icons.Filled.Stars,
                    contentDescription = "Target Met",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "DONE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFFF59E0B)
                )
            } else {
                Text(
                    "$overallPercentage%",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "GOAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GoalMetricTile(
    title: String,
    current: String,
    target: String,
    progress: Float,
    isMet: Boolean,
    primaryColor: Color,
    accentColor: Color,
    isDark: Boolean,
    statusSubtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = if (isDark) 0.06f else 0.35f))
            .border(
                0.5.dp,
                Color.White.copy(alpha = if (isDark) 0.15f else 0.65f),
                RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isMet) primaryColor else accentColor)
                )
                Text(
                    title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                "$current / $target",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMet) primaryColor else MaterialTheme.colorScheme.onSurface
            )
        }

        // Mini Linear Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(primaryColor.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(listOf(primaryColor, accentColor))
                    )
            )
        }

        Text(
            statusSubtitle,
            fontSize = 10.sp,
            color = if (isMet) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CadencePillSelector(
    selectedCadence: String,
    onCadenceSelected: (String) -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = if (isDark) 0.10f else 0.40f))
            .border(
                0.6.dp,
                Color.White.copy(alpha = if (isDark) 0.20f else 0.70f),
                RoundedCornerShape(20.dp)
            )
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf("DAILY" to "Daily", "WEEKLY" to "Weekly").forEach { (key, label) ->
            val isSelected = selectedCadence == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) {
                            if (isDark) Color(0xFF2563EB).copy(alpha = 0.6f)
                            else Color.White.copy(alpha = 0.9f)
                        } else Color.Transparent
                    )
                    .clickable { onCadenceSelected(key) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        if (isDark) Color.White else Color(0xFF2563EB)
                    } else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetStudyGoalsDialog(
    initialGoal: StudyGoalEntity,
    currentCadence: String,
    onDismiss: () -> Unit,
    onSaveGoals: (dailyTime: Int, dailyAccuracy: Int, weeklyTime: Int, weeklyAccuracy: Int, cadence: String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    var activeTab by remember { mutableStateOf(currentCadence) }
    var dailyMinutes by remember { mutableIntStateOf(initialGoal.dailyStudyTimeMinutes) }
    var dailyAccuracy by remember { mutableIntStateOf(initialGoal.dailyQuizAccuracy) }
    var weeklyMinutes by remember { mutableIntStateOf(initialGoal.weeklyStudyTimeMinutes) }
    var weeklyAccuracy by remember { mutableIntStateOf(initialGoal.weeklyQuizAccuracy) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "STUDY GOALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF2563EB)
                        )
                        Text(
                            "Set Learning Targets",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Cadence Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.40f))
                        .border(
                            0.5.dp,
                            Color.White.copy(alpha = if (isDark) 0.18f else 0.70f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("DAILY" to "Daily Targets", "WEEKLY" to "Weekly Targets").forEach { (tabKey, label) ->
                        val selected = activeTab == tabKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) {
                                        if (isDark) Color(0xFF2563EB).copy(alpha = 0.6f)
                                        else Color.White.copy(alpha = 0.95f)
                                    } else Color.Transparent
                                )
                                .clickable {
                                    haptics.perform(LiquidHapticType.LightClick)
                                    activeTab = tabKey
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) {
                                    if (isDark) Color.White else Color(0xFF2563EB)
                                } else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "goalInputs"
                ) { target ->
                    if (target == "DAILY") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Daily Study Time Setting
                            GoalSettingSection(
                                title = "Daily Study Time",
                                valueText = formatStudyMinutes(dailyMinutes),
                                icon = Icons.Filled.Timer,
                                iconColor = Color(0xFF2563EB),
                                onMinus = {
                                    if (dailyMinutes > 10) {
                                        dailyMinutes -= 5
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                onPlus = {
                                    if (dailyMinutes < 300) {
                                        dailyMinutes += 5
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                presets = listOf(15, 30, 45, 60, 90, 120),
                                presetLabel = { "${it}m" },
                                currentSelected = dailyMinutes,
                                onSelectPreset = {
                                    dailyMinutes = it
                                    haptics.perform(LiquidHapticType.LightClick)
                                },
                                isDark = isDark
                            )

                            // Daily Quiz Accuracy Setting
                            GoalSettingSection(
                                title = "Quiz Accuracy Target",
                                valueText = "$dailyAccuracy%",
                                icon = Icons.Filled.AutoAwesome,
                                iconColor = Color(0xFF10B981),
                                onMinus = {
                                    if (dailyAccuracy > 50) {
                                        dailyAccuracy -= 5
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                onPlus = {
                                    if (dailyAccuracy < 98) {
                                        dailyAccuracy += 5
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                presets = listOf(60, 70, 75, 80, 85, 90, 95),
                                presetLabel = { "${it}%" },
                                currentSelected = dailyAccuracy,
                                onSelectPreset = {
                                    dailyAccuracy = it
                                    haptics.perform(LiquidHapticType.LightClick)
                                },
                                isDark = isDark
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Weekly Study Time Setting
                            GoalSettingSection(
                                title = "Weekly Study Time",
                                valueText = formatStudyMinutes(weeklyMinutes),
                                icon = Icons.Filled.Timer,
                                iconColor = Color(0xFF2563EB),
                                onMinus = {
                                    if (weeklyMinutes > 30) {
                                        weeklyMinutes -= 15
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                onPlus = {
                                    if (weeklyMinutes < 1800) {
                                        weeklyMinutes += 15
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                presets = listOf(120, 180, 240, 300, 420, 600),
                                presetLabel = { formatStudyMinutes(it) },
                                currentSelected = weeklyMinutes,
                                onSelectPreset = {
                                    weeklyMinutes = it
                                    haptics.perform(LiquidHapticType.LightClick)
                                },
                                isDark = isDark
                            )

                            // Weekly Quiz Accuracy Setting
                            GoalSettingSection(
                                title = "Weekly Accuracy Target",
                                valueText = "$weeklyAccuracy%",
                                icon = Icons.Filled.AutoAwesome,
                                iconColor = Color(0xFF10B981),
                                onMinus = {
                                    if (weeklyAccuracy > 50) {
                                        weeklyAccuracy -= 5
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                onPlus = {
                                    if (weeklyAccuracy < 98) {
                                        weeklyAccuracy += 5
                                        haptics.perform(LiquidHapticType.LightClick)
                                    }
                                },
                                presets = listOf(60, 70, 75, 80, 85, 90, 95),
                                presetLabel = { "${it}%" },
                                currentSelected = weeklyAccuracy,
                                onSelectPreset = {
                                    weeklyAccuracy = it
                                    haptics.perform(LiquidHapticType.LightClick)
                                },
                                isDark = isDark
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    GlassButton(
                        onClick = {
                            haptics.perform(LiquidHapticType.Success)
                            onSaveGoals(
                                dailyMinutes,
                                dailyAccuracy,
                                weeklyMinutes,
                                weeklyAccuracy,
                                activeTab
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_study_goals_button")
                    ) {
                        Text("Save Goals", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSettingSection(
    title: String,
    valueText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    presets: List<Int>,
    presetLabel: (Int) -> String,
    currentSelected: Int,
    onSelectPreset: (Int) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = if (isDark) 0.05f else 0.35f))
            .border(
                0.5.dp,
                Color.White.copy(alpha = if (isDark) 0.15f else 0.65f),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onMinus,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (isDark) 0.12f else 0.50f))
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Decrease",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    valueText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.widthIn(min = 52.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onPlus,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (isDark) 0.12f else 0.50f))
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Increase",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Quick Preset Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.take(6).forEach { preset ->
                val isSelected = preset == currentSelected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                iconColor.copy(alpha = if (isDark) 0.40f else 0.20f)
                            } else {
                                Color.White.copy(alpha = if (isDark) 0.08f else 0.40f)
                            }
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) iconColor else Color.White.copy(alpha = if (isDark) 0.18f else 0.65f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectPreset(preset) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        presetLabel(preset),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) iconColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatStudyMinutes(minutes: Int): String {
    return if (minutes >= 60) {
        val h = minutes / 60
        val m = minutes % 60
        if (m > 0) "${h}h ${m}m" else "${h}h"
    } else {
        "${minutes}m"
    }
}
