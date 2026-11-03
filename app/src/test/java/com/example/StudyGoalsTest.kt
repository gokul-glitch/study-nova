package com.example

import com.example.data.local.StudyGoalEntity
import com.example.viewmodel.StudyGoalProgress
import org.junit.Assert.*
import org.junit.Test

class StudyGoalsTest {

    @Test
    fun defaultStudyGoal_initializesCorrectly() {
        val goal = StudyGoalEntity(userId = "user_123")
        assertEquals("user_123", goal.userId)
        assertEquals("DAILY", goal.period)
        assertEquals(45, goal.dailyStudyTimeMinutes)
        assertEquals(80, goal.dailyQuizAccuracy)
        assertEquals(240, goal.weeklyStudyTimeMinutes)
        assertEquals(80, goal.weeklyQuizAccuracy)
    }

    @Test
    fun studyGoalProgress_calculatesCompletionCorrectly() {
        val progress = StudyGoalProgress(
            period = "DAILY",
            studyTimeMinutesCurrent = 45,
            studyTimeMinutesTarget = 45,
            studyTimeProgress = 1.0f,
            quizAccuracyCurrent = 85,
            quizAccuracyTarget = 80,
            quizAccuracyProgress = 1.06f,
            isStudyTimeMet = true,
            isQuizAccuracyMet = true,
            overallCompletionPercentage = 100
        )

        assertTrue(progress.isStudyTimeMet)
        assertTrue(progress.isQuizAccuracyMet)
        assertEquals(100, progress.overallCompletionPercentage)
    }

    @Test
    fun studyGoalProgress_partialCompletion() {
        val progress = StudyGoalProgress(
            period = "DAILY",
            studyTimeMinutesCurrent = 20,
            studyTimeMinutesTarget = 40,
            studyTimeProgress = 0.5f,
            quizAccuracyCurrent = null,
            quizAccuracyTarget = 80,
            quizAccuracyProgress = 0f,
            isStudyTimeMet = false,
            isQuizAccuracyMet = false,
            overallCompletionPercentage = 25
        )

        assertFalse(progress.isStudyTimeMet)
        assertFalse(progress.isQuizAccuracyMet)
        assertEquals(25, progress.overallCompletionPercentage)
    }
}
