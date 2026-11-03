package com.example.data.local

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var quizId: Long = 0,
    val question: String,
    val questionType: String = "MCQ",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String,
    val explanation: String = "",
    val studentAnswer: String? = null,
    val isCorrect: Boolean = false
) {
    @get:Ignore
    val options: List<String>
        get() = if (optionC.isBlank() && optionD.isBlank()) {
            listOf(optionA, optionB).filter { it.isNotBlank() }
        } else {
            listOf(optionA, optionB, optionC, optionD).filter { it.isNotBlank() }
        }
}
