package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var setId: Long = 0,
    val userId: String?,
    val materialId: Long,
    val front: String,
    val back: String,
    val isMastered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
