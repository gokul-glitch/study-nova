package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val college: String = "",
    val course: String = "",
    val profilePictureUri: String? = null,
    val studyGoal: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
