package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_sets")
data class FlashcardSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val subjectId: Long,
    val materialId: Long,
    val totalCards: Int,
    val createdAt: Long = System.currentTimeMillis()
)
