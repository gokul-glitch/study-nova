package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_goals")
data class StudyGoalEntity(
    @PrimaryKey val userId: String,
    val period: String = "DAILY", // "DAILY" or "WEEKLY"
    val dailyStudyTimeMinutes: Int = 45,
    val dailyQuizAccuracy: Int = 80,
    val weeklyStudyTimeMinutes: Int = 240,
    val weeklyQuizAccuracy: Int = 80,
    val updatedAt: Long = System.currentTimeMillis()
)
