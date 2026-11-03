package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyApplication
import com.example.data.local.*
import com.example.service.AIStudyAnalysis
import com.example.service.StudyAnalyticsService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class TodayStudyStats(
    val studyTimeMinutes: Int = 0,
    val materialsStudied: Int = 0,
    val questionsAnswered: Int = 0,
    val quizAccuracy: Int? = null,
    val activitiesCount: Int = 0
)

data class WeeklyStudyStats(
    val studyTimeMinutes: Int = 0,
    val materialsStudied: Int = 0,
    val questionsAnswered: Int = 0,
    val quizAccuracy: Int? = null,
    val quizzesCount: Int = 0,
    val activitiesCount: Int = 0
)

data class StudyGoalProgress(
    val period: String = "DAILY", // "DAILY" or "WEEKLY"
    val studyTimeMinutesCurrent: Int = 0,
    val studyTimeMinutesTarget: Int = 45,
    val studyTimeProgress: Float = 0f, // 0.0 to 1.0 (or > 1.0 if exceeded)
    val quizAccuracyCurrent: Int? = null,
    val quizAccuracyTarget: Int = 80,
    val quizAccuracyProgress: Float = 0f,
    val isStudyTimeMet: Boolean = false,
    val isQuizAccuracyMet: Boolean = false,
    val overallCompletionPercentage: Int = 0
)

data class SubjectPerformance(
    val subjectId: Long,
    val subjectName: String,
    val quizzesCompleted: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val accuracy: Int,
    val studyActivityLevel: String, // "High", "Medium", "Low", "None"
    val materialsCount: Int
)

data class QuizTrendPoint(
    val quizId: Long,
    val title: String,
    val timestamp: Long,
    val percentage: Float,
    val formattedDate: String
)

data class PersonalizedRecommendation(
    val title: String,
    val description: String,
    val actionLabel: String,
    val actionType: String, // "QUIZ", "LIBRARY", "AI_TUTOR", "FLASHCARDS"
    val subjectId: Long? = null,
    val subjectName: String? = null,
    val materialId: Long? = null
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val activeDays: Int
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val totalMaterials: Int = 0,
    val totalSubjects: Int = 0,
    val totalQuizzes: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val quizAverageScore: Int = 0,
    val bestQuizScore: Int = 0,
    val totalStudyTimeMinutes: Int = 0,
    val totalFlashcardSets: Int = 0,
    val tutorConversations: Int = 0,
    val summariesGenerated: Int = 0,
    val flashcardsReviewed: Int = 0,
    val todayStudyStats: TodayStudyStats = TodayStudyStats(),
    val weeklyStudyStats: WeeklyStudyStats = WeeklyStudyStats(),
    val studyGoal: StudyGoalEntity = StudyGoalEntity(userId = ""),
    val activeGoalPeriod: String = "DAILY", // "DAILY" or "WEEKLY"
    val dailyGoalProgress: StudyGoalProgress = StudyGoalProgress(period = "DAILY"),
    val weeklyGoalProgress: StudyGoalProgress = StudyGoalProgress(period = "WEEKLY"),
    val activeGoalProgress: StudyGoalProgress = StudyGoalProgress(period = "DAILY"),
    val subjectPerformance: List<SubjectPerformance> = emptyList(),
    val streakInfo: StreakInfo = StreakInfo(0, 0, 0),
    val trendPoints7Days: List<QuizTrendPoint> = emptyList(),
    val trendPoints30Days: List<QuizTrendPoint> = emptyList(),
    val recentActivities: List<StudyActivityEntity> = emptyList(),
    val recommendation: PersonalizedRecommendation? = null,
    val aiAnalysis: AIStudyAnalysis? = null,
    val isAnalyzingAi: Boolean = false,
    val aiAnalysisError: String? = null,
    val selectedTrendRange: String = "7D", // "7D" or "30D"
    val error: String? = null
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val subjectRepository = (application as StudyApplication).subjectRepository
    private val materialRepository = (application as StudyApplication).repository
    private val quizRepository = (application as StudyApplication).quizRepository
    private val flashcardRepository = (application as StudyApplication).flashcardRepository
    private val activityRepository = (application as StudyApplication).studyActivityRepository
    private val goalRepository = (application as StudyApplication).studyGoalRepository
    private val aiAnalyticsService = StudyAnalyticsService()

    private val _currentUserId = MutableStateFlow("guest_user")
    fun setUserId(userId: String) {
        if (_currentUserId.value != userId) {
            _currentUserId.value = userId
            _aiAnalysis.value = null
            _aiAnalysisError.value = null
        }
    }

    private val _aiAnalysis = MutableStateFlow<AIStudyAnalysis?>(null)
    private val _isAnalyzingAi = MutableStateFlow(false)
    private val _aiAnalysisError = MutableStateFlow<String?>(null)
    private val _selectedTrendRange = MutableStateFlow("7D")
    private val _selectedGoalPeriod = MutableStateFlow<String?>("DAILY")

    fun setSelectedTrendRange(range: String) {
        _selectedTrendRange.value = range
    }

    fun setGoalPeriod(period: String) {
        _selectedGoalPeriod.value = period
    }

    fun saveStudyGoals(
        dailyStudyTime: Int,
        dailyQuizAccuracy: Int,
        weeklyStudyTime: Int,
        weeklyQuizAccuracy: Int,
        period: String
    ) {
        viewModelScope.launch {
            val uid = _currentUserId.value
            val goal = StudyGoalEntity(
                userId = uid,
                period = period,
                dailyStudyTimeMinutes = dailyStudyTime,
                dailyQuizAccuracy = dailyQuizAccuracy,
                weeklyStudyTimeMinutes = weeklyStudyTime,
                weeklyQuizAccuracy = weeklyQuizAccuracy,
                updatedAt = System.currentTimeMillis()
            )
            goalRepository.saveStudyGoal(goal)
            _selectedGoalPeriod.value = period
        }
    }

    private data class CoreStatsData(
        val subjects: List<SubjectEntity>,
        val materials: List<StudyMaterialEntity>,
        val quizzes: List<QuizEntity>,
        val flashcardSets: List<FlashcardSetEntity>,
        val activities: List<StudyActivityEntity>,
        val studyGoal: StudyGoalEntity?
    )

    private data class AiStateData(
        val aiAnalysis: AIStudyAnalysis?,
        val isAnalyzing: Boolean,
        val aiError: String?,
        val trendRange: String,
        val goalPeriod: String?
    )

    private val aiStateFlow = combine(
        _aiAnalysis,
        _isAnalyzingAi,
        _aiAnalysisError,
        _selectedTrendRange,
        _selectedGoalPeriod
    ) { aiAnalysis, isAnalyzing, aiError, trendRange, goalPeriod ->
        AiStateData(aiAnalysis, isAnalyzing, aiError, trendRange, goalPeriod)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = _currentUserId
        .flatMapLatest { userId ->
            val firstGroupFlow = combine(
                subjectRepository.getSubjectsForUser(userId),
                materialRepository.getAllMaterialsForUser(userId),
                quizRepository.getQuizzesForUser(userId)
            ) { subjects, materials, quizzes ->
                Triple(subjects, materials, quizzes)
            }

            val secondGroupFlow = combine(
                flashcardRepository.getFlashcardSetsForUser(userId),
                activityRepository.getAllActivities(userId),
                goalRepository.getStudyGoal(userId)
            ) { flashcardSets, activities, goal ->
                Triple(flashcardSets, activities, goal)
            }

            val coreStatsFlow = combine(firstGroupFlow, secondGroupFlow) { first, second ->
                CoreStatsData(
                    subjects = first.first,
                    materials = first.second,
                    quizzes = first.third,
                    flashcardSets = second.first,
                    activities = second.second,
                    studyGoal = second.third
                )
            }

            combine(coreStatsFlow, aiStateFlow) { core, ai ->
                calculateStats(
                    subjects = core.subjects,
                    materials = core.materials,
                    quizzes = core.quizzes,
                    flashcardSets = core.flashcardSets,
                    activities = core.activities,
                    studyGoal = core.studyGoal,
                    aiAnalysis = ai.aiAnalysis,
                    isAnalyzing = ai.isAnalyzing,
                    aiError = ai.aiError,
                    trendRange = ai.trendRange,
                    selectedGoalPeriod = ai.goalPeriod
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            StatisticsUiState(isLoading = true)
        )

    private fun calculateStats(
        subjects: List<SubjectEntity>,
        materials: List<StudyMaterialEntity>,
        quizzes: List<QuizEntity>,
        flashcardSets: List<FlashcardSetEntity>,
        activities: List<StudyActivityEntity>,
        studyGoal: StudyGoalEntity?,
        aiAnalysis: AIStudyAnalysis?,
        isAnalyzing: Boolean,
        aiError: String?,
        trendRange: String,
        selectedGoalPeriod: String?
    ): StatisticsUiState {
        val tutorConversations = activities.count { it.activityType == "USE_AI_TUTOR" }
        val summariesGenerated = activities.count { it.activityType == "GENERATE_SUMMARY" }
        val flashcardsReviewed = activities.count { it.activityType == "STUDY_FLASHCARDS" }
        val materialsUploaded = activities.count { it.activityType == "UPLOAD_MATERIAL" }

        var totalQuestions = 0
        var correctAnswers = 0
        var incorrectAnswers = 0
        var totalScoreSum = 0
        var bestScore = 0

        for (quiz in quizzes) {
            totalQuestions += quiz.totalQuestions
            val correct = if (quiz.correctCount > 0 || quiz.incorrectCount > 0) {
                quiz.correctCount
            } else if (quiz.score <= quiz.totalQuestions) {
                quiz.score
            } else {
                (quiz.totalQuestions * (quiz.score / 100.0)).toInt()
            }
            correctAnswers += correct

            val incorrect = if (quiz.incorrectCount > 0 || quiz.correctCount > 0) {
                quiz.incorrectCount
            } else {
                maxOf(0, quiz.totalQuestions - correct)
            }
            incorrectAnswers += incorrect

            val percentageInt = if (quiz.percentage > 0) {
                quiz.percentage.toInt()
            } else if (quiz.totalQuestions > 0) {
                ((correct.toFloat() / quiz.totalQuestions) * 100).toInt()
            } else {
                quiz.score
            }
            totalScoreSum += percentageInt
            if (percentageInt > bestScore) bestScore = percentageInt
        }

        val overallQuizAvg = if (quizzes.isNotEmpty()) totalScoreSum / quizzes.size else 0

        // Study Time Calculation
        val totalStudyMinutes = (quizzes.size * 5) +
                (flashcardsReviewed * 4) +
                (tutorConversations * 5) +
                (summariesGenerated * 4) +
                (materialsUploaded * 2)

        // Today's Study Stats
        val calToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMidnight = calToday.timeInMillis

        val todayActivities = activities.filter { it.timestamp >= todayMidnight }
        val todayQuizzes = quizzes.filter { it.createdAt >= todayMidnight }
        val todayQuestions = todayQuizzes.sumOf { it.totalQuestions }
        val todayCorrect = todayQuizzes.sumOf { it.correctCount }
        val todayQuizAccuracy = if (todayQuizzes.isNotEmpty() && todayQuestions > 0) {
            ((todayCorrect.toFloat() / todayQuestions) * 100).toInt()
        } else null

        val todayMaterialsCount = todayActivities.count {
            it.activityType == "UPLOAD_MATERIAL" || it.activityType == "GENERATE_SUMMARY"
        }
        val todayMinutes = (todayQuizzes.size * 5) +
                (todayActivities.count { it.activityType == "STUDY_FLASHCARDS" } * 4) +
                (todayActivities.count { it.activityType == "USE_AI_TUTOR" } * 5) +
                (todayActivities.count { it.activityType == "GENERATE_SUMMARY" } * 4) +
                (todayActivities.count { it.activityType == "UPLOAD_MATERIAL" } * 2)

        val todayStudyStats = TodayStudyStats(
            studyTimeMinutes = maxOf(todayMinutes, if (todayActivities.isNotEmpty()) 10 else 0),
            materialsStudied = todayMaterialsCount,
            questionsAnswered = todayQuestions,
            quizAccuracy = todayQuizAccuracy,
            activitiesCount = todayActivities.size
        )

        // Subject Performance
        val subjectPerformanceList = subjects.map { subject ->
            val subMaterials = materials.count { it.subjectId == subject.id }
            val subQuizzes = quizzes.filter { q ->
                q.subjectId == subject.id || materials.find { it.id == q.materialId }?.subjectId == subject.id
            }
            val subQuestions = subQuizzes.sumOf { it.totalQuestions }
            val subCorrect = subQuizzes.sumOf { it.correctCount }
            val subAccuracy = if (subQuestions > 0) {
                ((subCorrect.toFloat() / subQuestions) * 100).toInt()
            } else if (subQuizzes.isNotEmpty()) {
                subQuizzes.map { it.percentage.toInt() }.average().toInt()
            } else 0

            val subActivitiesCount = activities.count { it.subjectId == subject.id }
            val activityLevel = when {
                subActivitiesCount >= 6 -> "High"
                subActivitiesCount >= 3 -> "Medium"
                subActivitiesCount >= 1 -> "Low"
                else -> "None"
            }

            SubjectPerformance(
                subjectId = subject.id,
                subjectName = subject.subjectName,
                quizzesCompleted = subQuizzes.size,
                questionsAnswered = subQuestions,
                correctAnswers = subCorrect,
                accuracy = subAccuracy,
                studyActivityLevel = activityLevel,
                materialsCount = subMaterials
            )
        }

        // Performance Trend (Sorted by date ascending)
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        val thirtyDaysAgo = now - TimeUnit.DAYS.toMillis(30)

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val allTrendPoints = quizzes.sortedBy { it.createdAt }.map { q ->
            val pct = if (q.percentage > 0) q.percentage
            else if (q.totalQuestions > 0) (q.score.toFloat() / q.totalQuestions) * 100f
            else q.score.toFloat()
            QuizTrendPoint(
                quizId = q.id,
                title = q.quizTitle,
                timestamp = q.createdAt,
                percentage = pct,
                formattedDate = dateFormat.format(Date(q.createdAt))
            )
        }

        val trend7Days = allTrendPoints.filter { it.timestamp >= sevenDaysAgo }
        val trend30Days = allTrendPoints.filter { it.timestamp >= thirtyDaysAgo }

        // Study Streak
        val streakInfo = calculateStreak(activities, quizzes)

        // Weekly Study Stats (past 7 days including today)
        val calWeek = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekMidnight = calWeek.timeInMillis

        val weekActivities = activities.filter { it.timestamp >= weekMidnight }
        val weekQuizzes = quizzes.filter { it.createdAt >= weekMidnight }
        val weekQuestions = weekQuizzes.sumOf { it.totalQuestions }
        val weekCorrect = weekQuizzes.sumOf { it.correctCount }
        val weekQuizAccuracy = if (weekQuizzes.isNotEmpty() && weekQuestions > 0) {
            ((weekCorrect.toFloat() / weekQuestions) * 100).toInt()
        } else if (weekQuizzes.isNotEmpty()) {
            weekQuizzes.map { it.percentage.toInt() }.average().toInt()
        } else null

        val weekMaterialsCount = weekActivities.count {
            it.activityType == "UPLOAD_MATERIAL" || it.activityType == "GENERATE_SUMMARY"
        }
        val weekMinutes = (weekQuizzes.size * 5) +
                (weekActivities.count { it.activityType == "STUDY_FLASHCARDS" } * 4) +
                (weekActivities.count { it.activityType == "USE_AI_TUTOR" } * 5) +
                (weekActivities.count { it.activityType == "GENERATE_SUMMARY" } * 4) +
                (weekActivities.count { it.activityType == "UPLOAD_MATERIAL" } * 2)

        val weeklyStudyStats = WeeklyStudyStats(
            studyTimeMinutes = maxOf(weekMinutes, if (weekActivities.isNotEmpty()) weekActivities.size * 4 else 0),
            materialsStudied = weekMaterialsCount,
            questionsAnswered = weekQuestions,
            quizAccuracy = weekQuizAccuracy,
            quizzesCount = weekQuizzes.size,
            activitiesCount = weekActivities.size
        )

        // Study Goals Progress Calculation
        val effectiveGoal = studyGoal ?: StudyGoalEntity(userId = "")
        val activePeriod = (selectedGoalPeriod ?: effectiveGoal.period).ifBlank { "DAILY" }

        // Daily Goal Progress
        val dailyTargetTime = effectiveGoal.dailyStudyTimeMinutes.coerceAtLeast(5)
        val dailyTargetAccuracy = effectiveGoal.dailyQuizAccuracy.coerceIn(1, 100)
        val dailyTimeProgress = (todayStudyStats.studyTimeMinutes.toFloat() / dailyTargetTime).coerceIn(0f, 2f)
        val dailyAccuracyProgress = if (todayStudyStats.quizAccuracy != null) {
            (todayStudyStats.quizAccuracy.toFloat() / dailyTargetAccuracy).coerceIn(0f, 2f)
        } else 0f
        val isDailyTimeMet = todayStudyStats.studyTimeMinutes >= dailyTargetTime
        val isDailyAccuracyMet = (todayStudyStats.quizAccuracy ?: 0) >= dailyTargetAccuracy && (todayStudyStats.quizAccuracy != null)
        val dailyOverallCompletion = (((dailyTimeProgress.coerceAtMost(1f) + dailyAccuracyProgress.coerceAtMost(1f)) / 2f) * 100).toInt()

        val dailyGoalProgress = StudyGoalProgress(
            period = "DAILY",
            studyTimeMinutesCurrent = todayStudyStats.studyTimeMinutes,
            studyTimeMinutesTarget = dailyTargetTime,
            studyTimeProgress = dailyTimeProgress,
            quizAccuracyCurrent = todayStudyStats.quizAccuracy,
            quizAccuracyTarget = dailyTargetAccuracy,
            quizAccuracyProgress = dailyAccuracyProgress,
            isStudyTimeMet = isDailyTimeMet,
            isQuizAccuracyMet = isDailyAccuracyMet,
            overallCompletionPercentage = dailyOverallCompletion
        )

        // Weekly Goal Progress
        val weeklyTargetTime = effectiveGoal.weeklyStudyTimeMinutes.coerceAtLeast(15)
        val weeklyTargetAccuracy = effectiveGoal.weeklyQuizAccuracy.coerceIn(1, 100)
        val weeklyTimeProgress = (weeklyStudyStats.studyTimeMinutes.toFloat() / weeklyTargetTime).coerceIn(0f, 2f)
        val weeklyAccuracyProgress = if (weeklyStudyStats.quizAccuracy != null) {
            (weeklyStudyStats.quizAccuracy.toFloat() / weeklyTargetAccuracy).coerceIn(0f, 2f)
        } else 0f
        val isWeeklyTimeMet = weeklyStudyStats.studyTimeMinutes >= weeklyTargetTime
        val isWeeklyAccuracyMet = (weeklyStudyStats.quizAccuracy ?: 0) >= weeklyTargetAccuracy && (weeklyStudyStats.quizAccuracy != null)
        val weeklyOverallCompletion = (((weeklyTimeProgress.coerceAtMost(1f) + weeklyAccuracyProgress.coerceAtMost(1f)) / 2f) * 100).toInt()

        val weeklyGoalProgress = StudyGoalProgress(
            period = "WEEKLY",
            studyTimeMinutesCurrent = weeklyStudyStats.studyTimeMinutes,
            studyTimeMinutesTarget = weeklyTargetTime,
            studyTimeProgress = weeklyTimeProgress,
            quizAccuracyCurrent = weeklyStudyStats.quizAccuracy,
            quizAccuracyTarget = weeklyTargetAccuracy,
            quizAccuracyProgress = weeklyAccuracyProgress,
            isStudyTimeMet = isWeeklyTimeMet,
            isQuizAccuracyMet = isWeeklyAccuracyMet,
            overallCompletionPercentage = weeklyOverallCompletion
        )

        val activeGoalProgress = if (activePeriod == "WEEKLY") weeklyGoalProgress else dailyGoalProgress

        // Personalized Study Recommendation
        val recommendation = generateRecommendation(
            subjects = subjectPerformanceList,
            materials = materials,
            quizzes = quizzes,
            streakInfo = streakInfo,
            overallQuizAvg = overallQuizAvg
        )

        return StatisticsUiState(
            isLoading = false,
            totalMaterials = materials.size,
            totalSubjects = subjects.size,
            totalQuizzes = quizzes.size,
            totalQuestionsAnswered = totalQuestions,
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            quizAverageScore = overallQuizAvg,
            bestQuizScore = bestScore,
            totalStudyTimeMinutes = totalStudyMinutes,
            totalFlashcardSets = flashcardSets.size,
            tutorConversations = tutorConversations,
            summariesGenerated = summariesGenerated,
            flashcardsReviewed = flashcardsReviewed,
            todayStudyStats = todayStudyStats,
            weeklyStudyStats = weeklyStudyStats,
            studyGoal = effectiveGoal,
            activeGoalPeriod = activePeriod,
            dailyGoalProgress = dailyGoalProgress,
            weeklyGoalProgress = weeklyGoalProgress,
            activeGoalProgress = activeGoalProgress,
            subjectPerformance = subjectPerformanceList,
            streakInfo = streakInfo,
            trendPoints7Days = trend7Days,
            trendPoints30Days = trend30Days,
            recentActivities = activities.take(15),
            recommendation = recommendation,
            aiAnalysis = aiAnalysis,
            isAnalyzingAi = isAnalyzing,
            aiAnalysisError = aiError,
            selectedTrendRange = trendRange,
            error = null
        )
    }

    private fun generateRecommendation(
        subjects: List<SubjectPerformance>,
        materials: List<StudyMaterialEntity>,
        quizzes: List<QuizEntity>,
        streakInfo: StreakInfo,
        overallQuizAvg: Int
    ): PersonalizedRecommendation {
        if (materials.isEmpty()) {
            return PersonalizedRecommendation(
                title = "Start Your Study Journey",
                description = "Upload notes or lecture slides to enable automated AI summaries, flashcards, and tailored practice quizzes.",
                actionLabel = "Upload Notes",
                actionType = "LIBRARY"
            )
        }

        if (quizzes.isEmpty()) {
            val firstMat = materials.firstOrNull()
            return PersonalizedRecommendation(
                title = "Test Your Understanding",
                description = "You have ${materials.size} study materials ready. Generate your first AI quiz to establish your baseline score.",
                actionLabel = "Take First Quiz",
                actionType = "QUIZ",
                materialId = firstMat?.id,
                subjectId = firstMat?.subjectId
            )
        }

        // Find subject with lowest quiz accuracy (< 75%)
        val subjectsWithQuizzes = subjects.filter { it.quizzesCompleted > 0 }
        val weakestSubject = subjectsWithQuizzes.minByOrNull { it.accuracy }

        if (weakestSubject != null && weakestSubject.accuracy < 75) {
            return PersonalizedRecommendation(
                title = "Focus on ${weakestSubject.subjectName}",
                description = "Your quiz accuracy in ${weakestSubject.subjectName} is currently ${weakestSubject.accuracy}%. Review your notes or practice a 5-question refresher quiz.",
                actionLabel = "Practice ${weakestSubject.subjectName}",
                actionType = "QUIZ",
                subjectId = weakestSubject.subjectId,
                subjectName = weakestSubject.subjectName
            )
        }

        // Check if there is a subject with materials but NO quizzes
        val unquizzedSubject = subjects.firstOrNull { it.materialsCount > 0 && it.quizzesCompleted == 0 }
        if (unquizzedSubject != null) {
            return PersonalizedRecommendation(
                title = "Explore ${unquizzedSubject.subjectName}",
                description = "You have notes in ${unquizzedSubject.subjectName} but haven't taken a quiz yet. Test yourself to reinforce your memory.",
                actionLabel = "Quiz on ${unquizzedSubject.subjectName}",
                actionType = "QUIZ",
                subjectId = unquizzedSubject.subjectId,
                subjectName = unquizzedSubject.subjectName
            )
        }

        // High performer
        return PersonalizedRecommendation(
            title = "Keep Your Momentum",
            description = "You're achieving an impressive $overallQuizAvg% average score across your quizzes. Maintain your ${streakInfo.currentStreak}-day study streak with a quick review.",
            actionLabel = "Ask AI Tutor",
            actionType = "AI_TUTOR"
        )
    }

    fun requestAiStudyAnalysis() {
        val currentState = uiState.value
        if (currentState.totalQuizzes == 0 && currentState.totalMaterials == 0) {
            _aiAnalysisError.value = "Complete at least one quiz or study session before generating an AI analysis."
            return
        }

        viewModelScope.launch {
            _isAnalyzingAi.value = true
            _aiAnalysisError.value = null
            try {
                val subjectsSummary = currentState.subjectPerformance.joinToString("\n") {
                    "- ${it.subjectName}: ${it.quizzesCompleted} quizzes, ${it.accuracy}% accuracy, Activity: ${it.studyActivityLevel}"
                }.ifBlank { "No specific subject quizzes yet." }

                val trendSummary = currentState.trendPoints7Days.takeLast(5).joinToString("\n") {
                    "- ${it.formattedDate}: ${it.title} -> ${it.percentage.toInt()}%"
                }.ifBlank { "Recent quiz average: ${currentState.quizAverageScore}%" }

                val analysis = aiAnalyticsService.generateAnalysis(
                    overallAccuracy = currentState.quizAverageScore,
                    totalQuizzes = currentState.totalQuizzes,
                    totalQuestions = currentState.totalQuestionsAnswered,
                    streakDays = currentState.streakInfo.currentStreak,
                    subjectsInfo = subjectsSummary,
                    trendInfo = trendSummary
                )
                _aiAnalysis.value = analysis
            } catch (e: Exception) {
                _aiAnalysisError.value = e.message ?: "Failed to generate AI analysis. Check your connection and try again."
            } finally {
                _isAnalyzingAi.value = false
            }
        }
    }

    private fun calculateStreak(activities: List<StudyActivityEntity>, quizzes: List<QuizEntity>): StreakInfo {
        val allTimestamps = activities.map { it.timestamp } + quizzes.map { it.createdAt }
        if (allTimestamps.isEmpty()) return StreakInfo(0, 0, 0)

        val activeDays = allTimestamps.map { time ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.distinct().sortedDescending()

        if (activeDays.isEmpty()) return StreakInfo(0, 0, 0)

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.timeInMillis
        val yesterday = today - TimeUnit.DAYS.toMillis(1)

        var currentStreak = 0
        var checkDay = today
        var dayIndex = 0

        if (activeDays[0] == today) {
            currentStreak = 1
            dayIndex = 1
            checkDay = today
        } else if (activeDays[0] == yesterday) {
            currentStreak = 1
            dayIndex = 1
            checkDay = yesterday
        }

        if (currentStreak > 0) {
            while (dayIndex < activeDays.size) {
                val nextExpectedDay = checkDay - TimeUnit.DAYS.toMillis(1)
                if (activeDays[dayIndex] == nextExpectedDay) {
                    currentStreak++
                    checkDay = nextExpectedDay
                    dayIndex++
                } else {
                    break
                }
            }
        }

        var longestStreak = 0
        var tempCurrent = 1
        for (i in 0 until activeDays.size - 1) {
            if (activeDays[i] - TimeUnit.DAYS.toMillis(1) == activeDays[i + 1]) {
                tempCurrent++
            } else {
                if (tempCurrent > longestStreak) longestStreak = tempCurrent
                tempCurrent = 1
            }
        }
        if (tempCurrent > longestStreak) longestStreak = tempCurrent
        if (longestStreak < currentStreak) longestStreak = currentStreak

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = maxOf(longestStreak, currentStreak),
            activeDays = activeDays.size
        )
    }
}
