package com.example.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.liquidGlassNavigation
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics
import com.example.viewmodel.StudyMaterialViewModel
import com.example.ui.StatisticsScreen

sealed class Screen(
    val route: String,
    val title: String,
    val iconFilled: ImageVector? = null,
    val iconOutlined: ImageVector? = null
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Subjects : Screen("subjects", "Subjects")
    object Library : Screen("library", "Library", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks)
    object Stats : Screen("stats", "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object Profile : Screen("profile", "Profile")
    object AITutor : Screen("ai_tutor", "AI Tutor")
    object QuizGenerator : Screen("quiz_generator", "Quiz Generator")
    object Quiz : Screen("quiz", "Quiz")
    object QuizResult : Screen("quiz_result", "Quiz Result")
    object QuizReview : Screen("quiz_review", "Review Answers")
    object Flashcards : Screen("flashcards", "Flashcards")
    object Summary : Screen("summary/{materialId}", "Summary") {
        fun createRoute(materialId: Long) = "summary/$materialId"
    }
    object AuthCheck : Screen("auth_check", "Check")
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    toggleTheme: () -> Unit,
    studyViewModel: StudyMaterialViewModel,
    summaryViewModel: com.example.viewmodel.SummaryViewModel,
    authViewModel: com.example.viewmodel.AuthViewModel,
    subjectsViewModel: com.example.viewmodel.SubjectsViewModel,
    aiTutorViewModel: com.example.viewmodel.AITutorViewModel,
    quizViewModel: com.example.viewmodel.QuizViewModel,
    flashcardsViewModel: com.example.viewmodel.FlashcardsViewModel,
    statisticsViewModel: com.example.viewmodel.StatisticsViewModel
) {
    val navController = rememberNavController()

    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()
    val isFirebaseConfigured = authViewModel.isFirebaseConfigured

    LaunchedEffect(currentUserId) {
        val uid = currentUserId
        if (uid != null) {
            studyViewModel.setUserId(uid)
            summaryViewModel.setUserId(uid)
            subjectsViewModel.setUserId(uid)
            aiTutorViewModel.setUserId(uid)
            quizViewModel.setUserId(uid)
            flashcardsViewModel.setUserId(uid)
            statisticsViewModel.setUserId(uid)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Only show bottom bar on top-level destinations
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Home.route, Screen.Library.route, Screen.Stats.route, Screen.Settings.route
    )

    Scaffold(
        containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF),
                    tonalElevation = 2.dp,
                    modifier = Modifier.border(
                        BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
                    )
                ) {
                    val screens = listOf(Screen.Home, Screen.Library, Screen.Stats, Screen.Settings)
                    screens.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val icon = if (isSelected) (screen.iconFilled ?: screen.iconOutlined) else (screen.iconOutlined ?: screen.iconFilled)
                                if (icon != null) {
                                    Icon(
                                        icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2563EB),
                                selectedTextColor = Color(0xFF2563EB),
                                indicatorColor = if (isDarkTheme) Color(0xFF1E3A8A) else Color(0xFFEFF6FF),
                                unselectedIconColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                unselectedTextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.AuthCheck.route,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.AuthCheck.route) {
                LaunchedEffect(Unit) {
                    if (currentUserId != null) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.AuthCheck.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AuthCheck.route) { inclusive = true }
                        }
                    }
                }
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    subjectsViewModel = subjectsViewModel,
                    statisticsViewModel = statisticsViewModel,
                    authViewModel = authViewModel,
                    onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) }, 
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToAITutor = { navController.navigate(Screen.AITutor.route) },
                    onNavigateToQuiz = { navController.navigate(Screen.QuizGenerator.route) },
                    onNavigateToFlashcards = { navController.navigate(Screen.Flashcards.route) },
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onImportDocument = { uri, subjectId -> studyViewModel.importDocument(uri, subjectId) },
                    onNavigateToQuizForSubject = { subjectId ->
                        quizViewModel.selectSubjectById(subjectId)
                        navController.navigate(Screen.QuizGenerator.route)
                    }
                )
            }
            composable(Screen.Subjects.route) {
                SubjectsScreen(subjectsViewModel = subjectsViewModel, studyViewModel = studyViewModel, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    subjectsViewModel = subjectsViewModel,
                    viewModel = studyViewModel,
                    onNavigateToSummary = { id -> navController.navigate(Screen.Summary.createRoute(id)) }
                )
            }
            composable(Screen.Stats.route) { 
                StatisticsScreen(
                    viewModel = statisticsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    userId = currentUserId ?: "guest_user",
                    onNavigateToQuiz = { navController.navigate(Screen.QuizGenerator.route) },
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                    onNavigateToAITutor = { navController.navigate(Screen.AITutor.route) },
                    onNavigateToFlashcards = { navController.navigate(Screen.Flashcards.route) }
                )
            }
            composable(Screen.Settings.route) { SettingsScreen(isDarkTheme = isDarkTheme, toggleTheme = toggleTheme) }
            composable(Screen.Profile.route) { 
                ProfileScreen(
                    viewModel = authViewModel,
                    isDarkTheme = isDarkTheme,
                    toggleTheme = toggleTheme,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }
            composable(Screen.AITutor.route) { 
                AITutorScreen(
                    viewModel = aiTutorViewModel,
                    onNavigateBack = { navController.popBackStack() }
                ) 
            }
            composable(Screen.QuizGenerator.route) {
                QuizGeneratorScreen(
                    viewModel = quizViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) },
                    onNavigateToResult = { navController.navigate(Screen.QuizResult.route) },
                    onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) },
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) }
                )
            }
            composable(Screen.Quiz.route) { 
                QuizScreen(
                    viewModel = quizViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onFinishQuiz = {
                        navController.navigate(Screen.QuizResult.route) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    }
                ) 
            }
            composable(Screen.QuizResult.route) {
                QuizResultScreen(
                    viewModel = quizViewModel,
                    onReviewAnswers = { navController.navigate(Screen.QuizReview.route) },
                    onRetryQuiz = {
                        navController.navigate(Screen.Quiz.route) {
                            popUpTo(Screen.QuizResult.route) { inclusive = true }
                        }
                    },
                    onGenerateNewQuiz = {
                        navController.navigate(Screen.QuizGenerator.route) {
                            popUpTo(Screen.QuizGenerator.route) { inclusive = true }
                        }
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.QuizReview.route) {
                QuizReviewScreen(
                    viewModel = quizViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Flashcards.route) { 
                FlashcardsScreen(
                    viewModel = flashcardsViewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                ) 
            }
            composable(Screen.Summary.route) { backStackEntry ->
                val materialId = backStackEntry.arguments?.getString("materialId")?.toLongOrNull() ?: 0L
                SummaryScreen(
                    materialId = materialId,
                    viewModel = summaryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
