package com.example.data.model

data class StudyMaterial(
    val id: String,
    val title: String,
    val type: String,
    val timestamp: Long,
    val isStarred: Boolean = false,
    val description: String = ""
)

data class Note(
    val id: String,
    val materialId: String,
    val content: String
)

data class Quiz(
    val id: String,
    val title: String,
    val questionCount: Int
)

data class Flashcard(
    val id: String,
    val front: String,
    val back: String
)

data class StudyGoal(
    val title: String,
    val progressPercent: Int,
    val itemsLeft: Int
)

data class UserProfile(
    val name: String,
    val email: String,
    val goal: StudyGoal
)
