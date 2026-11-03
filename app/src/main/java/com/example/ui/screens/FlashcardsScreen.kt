package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FlashcardEntity
import com.example.data.local.FlashcardSetEntity
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.FlashcardsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: FlashcardsViewModel?,
    onNavigateHome: () -> Unit = {}
) {
    val haptics = rememberLiquidHaptics()

    val subjects by viewModel?.subjects?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val materials by viewModel?.materials?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val flashcardSets by viewModel?.flashcardSets?.collectAsStateWithLifecycle(initialValue = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val selectedSubject by viewModel?.selectedSubject?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val selectedMaterial by viewModel?.selectedMaterial?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val flashcards by viewModel?.flashcards?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val numCards by viewModel?.numCards?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(10) }
    val difficulty by viewModel?.difficulty?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("Medium") }
    val currentIndex by viewModel?.currentIndex?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0) }
    val isLoading by viewModel?.isLoading?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val errorMessage by viewModel?.errorMessage?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val isFlipped by viewModel?.isFlipped?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val isFinished by viewModel?.isFinished?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val masteredIndices by viewModel?.masteredIndices?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptySet()) }
    val learningIndices by viewModel?.learningIndices?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptySet()) }
    val deckTitle by viewModel?.deckTitle?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("Study Deck") }
    val sessionDurationSeconds by viewModel?.sessionDurationSeconds?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0L) }

    var showCreateDialog by remember { mutableStateOf(false) }

    if (flashcards.isNotEmpty()) {
        if (isFinished) {
            // 1. Session Complete / Study Progress Summary Screen
            FlashcardStudySummaryScreen(
                totalCards = flashcards.size,
                masteredCount = masteredIndices.size,
                learningCount = learningIndices.size,
                durationSeconds = sessionDurationSeconds,
                flashcards = flashcards,
                masteredIndices = masteredIndices,
                learningIndices = learningIndices,
                onStudyAgain = {
                    haptics.perform(LiquidHapticType.LightClick)
                    viewModel?.studyAgain()
                },
                onReviewMissed = {
                    haptics.perform(LiquidHapticType.LightClick)
                    viewModel?.reviewMissedCards()
                },
                onReturnToLibrary = {
                    haptics.perform(LiquidHapticType.LightClick)
                    viewModel?.resetSession()
                },
                onNavigateHome = onNavigateHome
            )
        } else {
            // 2. Active Swipeable Flashcard Study Session
            FlashcardActiveStudyScreen(
                deckTitle = deckTitle,
                flashcards = flashcards,
                currentIndex = currentIndex,
                isFlipped = isFlipped,
                masteredCount = masteredIndices.size,
                learningCount = learningIndices.size,
                onSwipeRight = {
                    haptics.perform(LiquidHapticType.Success)
                    viewModel?.swipeRight()
                },
                onSwipeLeft = {
                    haptics.perform(LiquidHapticType.LightClick)
                    viewModel?.swipeLeft()
                },
                onFlip = {
                    haptics.perform(LiquidHapticType.Toggle)
                    viewModel?.flipCard()
                },
                onPrevious = {
                    haptics.perform(LiquidHapticType.LightClick)
                    viewModel?.prevCard()
                },
                onExit = {
                    haptics.perform(LiquidHapticType.LightClick)
                    viewModel?.resetSession()
                }
            )
        }
    } else if (showCreateDialog) {
        // 3. AI Flashcard Generator Dialog / Screen
        FlashcardCreateScreen(
            subjects = subjects,
            materials = materials,
            selectedSubject = selectedSubject,
            selectedMaterial = selectedMaterial,
            numCards = numCards,
            difficulty = difficulty,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onSelectSubject = { viewModel?.selectSubject(it); viewModel?.selectMaterial(null) },
            onSelectMaterial = { viewModel?.selectMaterial(it) },
            onSetNumCards = { viewModel?.setNumCards(it) },
            onSetDifficulty = { viewModel?.setDifficulty(it) },
            onGenerate = {
                haptics.perform(LiquidHapticType.Success)
                viewModel?.generateFlashcards()
            },
            onDismiss = { showCreateDialog = false }
        )
    } else {
        // 4. Flashcards Library / Main Screen with Quick Practice Deck
        FlashcardLibraryScreen(
            flashcardSets = flashcardSets,
            subjects = subjects,
            materials = materials,
            onCreateClick = {
                viewModel?.clearError()
                showCreateDialog = true
            },
            onStartQuickPractice = {
                haptics.perform(LiquidHapticType.Success)
                viewModel?.startQuickPractice()
            },
            onPlaySet = { set ->
                haptics.perform(LiquidHapticType.LightClick)
                val subjectName = subjects.find { it.id == set.subjectId }?.subjectName ?: "Study Deck"
                val materialTitle = materials.find { it.id == set.materialId }?.title ?: "Notes"
                viewModel?.loadFlashcardSet(set.id, "$subjectName • $materialTitle")
            },
            onDeleteSet = { set ->
                haptics.perform(LiquidHapticType.Error)
                viewModel?.deleteFlashcardSet(set.id)
            }
        )
    }
}

/**
 * Active study screen featuring swipeable physics cards, tap-to-flip, and real-time progress indicators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashcardActiveStudyScreen(
    deckTitle: String,
    flashcards: List<FlashcardEntity>,
    currentIndex: Int,
    isFlipped: Boolean,
    masteredCount: Int,
    learningCount: Int,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onFlip: () -> Unit,
    onPrevious: () -> Unit,
    onExit: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val currentCard = flashcards.getOrNull(currentIndex) ?: return
    val nextCard = flashcards.getOrNull(currentIndex + 1)
    val total = flashcards.size
    val progress = (currentIndex.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column {
                        Text(
                            text = deckTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Card ${currentIndex + 1} of $total",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Deck")
                    }
                },
                actions = {
                    // Current mastery indicator chip
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f))
                            .border(0.5.dp, Color(0xFF2563EB).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Style,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "${((currentIndex.toFloat() / total) * 100).toInt()}% Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF2563EB),
                trackColor = Color(0xFF2563EB).copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Progress Tracker Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Need Review Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                        .border(0.5.dp, Color(0xFFF59E0B).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "$learningCount Review",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }

                // Interactive Hint
                Text(
                    text = "👈 Swipe to rate 👉",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                // Mastered Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.12f))
                        .border(0.5.dp, Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "$masteredCount Mastered",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Swipeable Card Stack Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Peek Background Card (stack effect)
                if (nextCard != null) {
                    PeekFlashcard(
                        card = nextCard,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(380.dp)
                            .offset(y = 14.dp)
                    )
                }

                // Top Active Swipeable & Flippable Flashcard
                SwipeableFlashcard(
                    card = currentCard,
                    isFlipped = isFlipped,
                    onTapToFlip = onFlip,
                    onSwipeRight = onSwipeRight,
                    onSwipeLeft = onSwipeLeft,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Control Dock (Touch targets & accessible alternatives to swiping)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Undo / Previous Card Button
                IconButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF))
                        .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo previous",
                        tint = if (currentIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 2. Swipe Left / Still Learning Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onSwipeLeft,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0xFFF43F5E).copy(alpha = 0.25f))
                            .clip(CircleShape)
                            .background(Color(0xFFF43F5E).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Still Learning / Swipe Left",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Learning",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF43F5E)
                    )
                }

                // 3. Tap to Flip Center Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onFlip,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f))
                            .border(0.8.dp, Color(0xFF2563EB).copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.FlipCameraAndroid,
                            contentDescription = "Flip Card",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Flip",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                }

                // 4. Swipe Right / Mastered Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onSwipeRight,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0xFF10B981).copy(alpha = 0.25f))
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Mastered / Swipe Right",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Mastered",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

/**
 * Top interactive flashcard with horizontal drag gesture physics, dynamic tilt, visual rating badges,
 * and 3D flip animation between Term and Definition.
 */
@Composable
private fun SwipeableFlashcard(
    card: FlashcardEntity,
    isFlipped: Boolean,
    onTapToFlip: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // 3D Flip animation
    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 380),
        label = "flipRotation"
    )
    val isBackShowing = flipRotation > 90f

    // Physics calculations
    val tiltAngle = (dragOffset.value.x / 22f).coerceIn(-18f, 18f)
    val swipeRightAlpha = (dragOffset.value.x / 140f).coerceIn(0f, 1f)
    val swipeLeftAlpha = (-dragOffset.value.x / 140f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .offset(x = dragOffset.value.x.dp, y = (dragOffset.value.y * 0.15f).dp)
            .graphicsLayer {
                rotationZ = tiltAngle
                rotationY = flipRotation
                cameraDistance = 16f * density
            }
            .pointerInput(card.id, card.front) {
                var totalDragDistance = 0f
                detectDragGestures(
                    onDragStart = { totalDragDistance = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragDistance += abs(dragAmount.x) + abs(dragAmount.y)
                        scope.launch {
                            dragOffset.snapTo(
                                Offset(
                                    x = dragOffset.value.x + dragAmount.x * 0.85f,
                                    y = dragOffset.value.y + dragAmount.y * 0.4f
                                )
                            )
                        }
                    },
                    onDragEnd = {
                        val threshold = 110f
                        if (totalDragDistance < 15f) {
                            // Tap detected!
                            onTapToFlip()
                        } else if (dragOffset.value.x > threshold) {
                            // Swiped right -> Mastered
                            scope.launch {
                                dragOffset.animateTo(
                                    Offset(800f, 0f),
                                    animationSpec = tween(180)
                                )
                                onSwipeRight()
                                dragOffset.snapTo(Offset.Zero)
                            }
                        } else if (dragOffset.value.x < -threshold) {
                            // Swiped left -> Still Learning
                            scope.launch {
                                dragOffset.animateTo(
                                    Offset(-800f, 0f),
                                    animationSpec = tween(180)
                                )
                                onSwipeLeft()
                                dragOffset.snapTo(Offset.Zero)
                            }
                        } else {
                            // Spring back to center
                            scope.launch {
                                dragOffset.animateTo(
                                    Offset.Zero,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            dragOffset.animateTo(Offset.Zero)
                        }
                    }
                )
            }
    ) {
        GlassCard(
            modifier = Modifier.fillMaxSize(),
            onClick = onTapToFlip
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Content layer (inverts rotation on back side so text is right-side up)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (isBackShowing) {
                                rotationY = 180f
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Tag Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isBackShowing) Color(0xFF7C3AED).copy(alpha = 0.15f)
                                    else Color(0xFF2563EB).copy(alpha = 0.15f)
                                )
                                .border(
                                    0.5.dp,
                                    if (isBackShowing) Color(0xFF7C3AED).copy(alpha = 0.35f)
                                    else Color(0xFF2563EB).copy(alpha = 0.35f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (isBackShowing) Icons.Filled.Lightbulb else Icons.Filled.School,
                                    contentDescription = null,
                                    tint = if (isBackShowing) Color(0xFF7C3AED) else Color(0xFF2563EB),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    if (isBackShowing) "DEFINITION" else "TERM / CONCEPT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (isBackShowing) Color(0xFF7C3AED) else Color(0xFF2563EB)
                                )
                            }
                        }

                        // Hint pill
                        Text(
                            text = if (isBackShowing) "Answer" else "Prompt",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    // Main Term or Definition Typography
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isBackShowing) {
                            Text(
                                text = card.front,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7C3AED),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = card.back,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = card.front,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Bottom interaction instruction
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.40f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isBackShowing) "Tap to view term" else "Tap card to flip definition",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Dynamic Swipe Stamp: "MASTERED" (appears when dragged right)
                if (swipeRightAlpha > 0.05f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .rotate(-15f)
                            .graphicsLayer {
                                alpha = swipeRightAlpha
                                if (isBackShowing) rotationY = 180f
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.25f))
                            .border(2.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "MASTERED",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981),
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Dynamic Swipe Stamp: "NEED REVIEW" (appears when dragged left)
                if (swipeLeftAlpha > 0.05f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .rotate(15f)
                            .graphicsLayer {
                                alpha = swipeLeftAlpha
                                if (isBackShowing) rotationY = 180f
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF43F5E).copy(alpha = 0.25f))
                            .border(2.dp, Color(0xFFF43F5E), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "NEED REVIEW",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF43F5E),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Static peek card rendered underneath the active card to provide a tactile physical stack look.
 */
@Composable
private fun PeekFlashcard(
    card: FlashcardEntity,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .graphicsLayer {
                scaleX = 0.94f
                scaleY = 0.94f
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.front,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Study Progress Summary Screen displaying session mastery stats, review breakdowns,
 * and options to review missed cards or repeat the deck.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashcardStudySummaryScreen(
    totalCards: Int,
    masteredCount: Int,
    learningCount: Int,
    durationSeconds: Long,
    flashcards: List<FlashcardEntity>,
    masteredIndices: Set<Int>,
    learningIndices: Set<Int>,
    onStudyAgain: () -> Unit,
    onReviewMissed: () -> Unit,
    onReturnToLibrary: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val masteryRate = if (totalCards > 0) ((masteredCount.toFloat() / totalCards.toFloat()) * 100).toInt() else 0
    val durationText = remember(durationSeconds) {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Study Progress Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onReturnToLibrary) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Mastery Dial Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "SESSION MASTERY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (masteryRate >= 80) "Outstanding Retention!" else if (masteryRate >= 50) "Great Progress!" else "Keep Practicing!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Circular Score Badge
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(12.dp, CircleShape, spotColor = Color(0xFF10B981).copy(alpha = 0.25f))
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(
                                width = 1.5.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.85f), Color(0xFF10B981).copy(alpha = 0.5f))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$masteryRate%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                "Mastery",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryStatItem(
                            icon = Icons.Filled.CheckCircle,
                            iconTint = Color(0xFF10B981),
                            value = "$masteredCount",
                            label = "Mastered"
                        )
                        SummaryStatItem(
                            icon = Icons.Filled.Refresh,
                            iconTint = Color(0xFFF59E0B),
                            value = "$learningCount",
                            label = "To Review"
                        )
                        SummaryStatItem(
                            icon = Icons.Filled.Timer,
                            iconTint = Color(0xFF3B82F6),
                            value = durationText,
                            label = "Study Time"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            if (learningCount > 0) {
                GlassButton(
                    onClick = onReviewMissed,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            "Review Missed Terms ($learningCount cards)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassOutlinedButton(
                    onClick = onStudyAgain,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Study Entire Deck")
                }
                GlassOutlinedButton(
                    onClick = onReturnToLibrary,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("All Decks")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms Breakdown Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TERMS BREAKDOWN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$totalCards Terms Reviewed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card list with status
            flashcards.forEachIndexed { index, card ->
                val isMastered = index in masteredIndices

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
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
                                .clip(CircleShape)
                                .background(
                                    if (isMastered) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isMastered) Icons.Filled.Check else Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = if (isMastered) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.front,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = card.back,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isMastered) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isMastered) "Mastered" else "Review",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMastered) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Home", color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SummaryStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Flashcards Library screen with instant practice starter deck and deck management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashcardLibraryScreen(
    flashcardSets: List<FlashcardSetEntity>,
    subjects: List<com.example.data.local.SubjectEntity>,
    materials: List<com.example.data.local.StudyMaterialEntity>,
    onCreateClick: () -> Unit,
    onStartQuickPractice: () -> Unit,
    onPlaySet: (FlashcardSetEntity) -> Unit,
    onDeleteSet: (FlashcardSetEntity) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Flashcards", fontWeight = FontWeight.Bold, fontSize = 22.sp) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Flashcards")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Quick Practice Featured Hero Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                onClick = onStartQuickPractice
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                            .border(0.5.dp, Color(0xFF2563EB).copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "FEATURED DECK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF2563EB)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Instant", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                        Text(
                            "Study Skills & Cognitive Science",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "8 High-yield cards with swipeable physics",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Start",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                "SAVED DECKS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (flashcardSets.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Style,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Create Your First Custom Deck",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tap the + button to generate a smart flashcard deck from your uploaded study materials using AI.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GlassButton(onClick = onCreateClick) {
                            Text("Generate AI Deck")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(flashcardSets) { set ->
                        FlashcardSetItem(
                            set = set,
                            subjectName = subjects.find { it.id == set.subjectId }?.subjectName ?: "General",
                            materialName = materials.find { it.id == set.materialId }?.title ?: "Study Notes",
                            onPlay = { onPlaySet(set) },
                            onDelete = { onDeleteSet(set) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun FlashcardSetItem(
    set: FlashcardSetEntity,
    subjectName: String,
    materialName: String,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Flashcards") },
            text = { Text("Are you sure you want to delete this flashcard deck?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Delete", color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Style, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    subjectName.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF2563EB)
                )
                Text(
                    materialName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(set.createdAt))
                Text(
                    "${set.totalCards} cards • $dateStr",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Study", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Creation / Generation screen to configure and generate AI flashcards from materials.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashcardCreateScreen(
    subjects: List<com.example.data.local.SubjectEntity>,
    materials: List<com.example.data.local.StudyMaterialEntity>,
    selectedSubject: com.example.data.local.SubjectEntity?,
    selectedMaterial: com.example.data.local.StudyMaterialEntity?,
    numCards: Int,
    difficulty: String,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectSubject: (com.example.data.local.SubjectEntity) -> Unit,
    onSelectMaterial: (com.example.data.local.StudyMaterialEntity) -> Unit,
    onSetNumCards: (Int) -> Unit,
    onSetDifficulty: (String) -> Unit,
    onGenerate: () -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Generate Flashcards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "AI FLASHCARD GENERATOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF2563EB)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Create Smart Cards",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Select a subject and study document to automatically generate a flashcard deck.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            errorMessage?.let { error ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFFEF4444)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Deck Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    // Subject Selection
                    var subjectExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = !subjectExpanded }
                    ) {
                        GlassTextField(
                            value = selectedSubject?.subjectName ?: "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
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
                                        onSelectSubject(subject)
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Material Selection
                    var materialExpanded by remember { mutableStateOf(false) }
                    val filteredMaterials = materials.filter { it.subjectId == selectedSubject?.id }

                    ExposedDropdownMenuBox(
                        expanded = materialExpanded,
                        onExpandedChange = { if (selectedSubject != null) materialExpanded = !materialExpanded }
                    ) {
                        GlassTextField(
                            value = selectedMaterial?.title ?: "Select Material",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            enabled = selectedSubject != null
                        )
                        ExposedDropdownMenu(
                            expanded = materialExpanded,
                            onDismissRequest = { materialExpanded = false }
                        ) {
                            filteredMaterials.forEach { material ->
                                DropdownMenuItem(
                                    text = { Text(material.title) },
                                    onClick = {
                                        onSelectMaterial(material)
                                        materialExpanded = false
                                    }
                                )
                            }
                            if (filteredMaterials.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No materials in this subject") },
                                    onClick = { materialExpanded = false }
                                )
                            }
                        }
                    }

                    // Number of Cards
                    var numExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = numExpanded,
                        onExpandedChange = { numExpanded = !numExpanded }
                    ) {
                        GlassTextField(
                            value = "$numCards Flashcards",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = numExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = numExpanded,
                            onDismissRequest = { numExpanded = false }
                        ) {
                            listOf(5, 10, 15, 20).forEach { num ->
                                DropdownMenuItem(
                                    text = { Text("$num Flashcards") },
                                    onClick = {
                                        onSetNumCards(num)
                                        numExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Difficulty
                    var diffExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = diffExpanded,
                        onExpandedChange = { diffExpanded = !diffExpanded }
                    ) {
                        GlassTextField(
                            value = difficulty,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = diffExpanded,
                            onDismissRequest = { diffExpanded = false }
                        ) {
                            listOf("Easy", "Medium", "Hard").forEach { diff ->
                                DropdownMenuItem(
                                    text = { Text(diff) },
                                    onClick = {
                                        onSetDifficulty(diff)
                                        diffExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GlassButton(
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMaterial != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Generate Flashcards", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassOutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
