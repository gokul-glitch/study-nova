package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val subjectName: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
