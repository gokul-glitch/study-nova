package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_activities")
data class StudyActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val activityType: String, // e.g., "UPLOAD_MATERIAL", "GENERATE_SUMMARY", "COMPLETE_QUIZ", "GENERATE_FLASHCARDS", "STUDY_FLASHCARDS", "USE_AI_TUTOR"
    val subjectId: Long? = null,
    val materialId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
