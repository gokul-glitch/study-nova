package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "local_user",
    val subjectId: Long? = null,
    val title: String,
    val fileName: String,
    val fileUri: String,
    val fileType: String,
    val createdAt: Long,
    val fileSize: Long,
    val summary: String? = null,
    val textContent: String? = null
)
