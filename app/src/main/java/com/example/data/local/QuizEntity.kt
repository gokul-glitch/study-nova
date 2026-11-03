package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val subjectId: Long? = null,
    val subjectName: String = "",
    val materialId: Long? = null,
    val materialTitle: String = "",
    val quizTitle: String = "AI Quiz",
    val difficulty: String = "Medium",
    val questionType: String = "Multiple Choice",
    val totalQuestions: Int = 0,
    val score: Int = 0,
    val percentage: Float = 0f,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
