package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.StudyMaterialRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.UserProfileRepository
import com.example.data.repository.SubjectRepository
import com.example.data.repository.QuizRepository
import com.example.data.repository.FlashcardRepository
import com.example.data.repository.StudyActivityRepository
import com.example.data.repository.StudyGoalRepository
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class StudyApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { StudyMaterialRepository(database.studyMaterialDao()) }
    val profileRepository by lazy { UserProfileRepository(database.userProfileDao()) }
    val subjectRepository by lazy { SubjectRepository(database.subjectDao()) }
    val quizRepository by lazy { QuizRepository(database.quizDao()) }
    val flashcardRepository by lazy { FlashcardRepository(database.flashcardDao()) }
    val studyActivityRepository by lazy { StudyActivityRepository(database.studyActivityDao()) }
    val studyGoalRepository by lazy { StudyGoalRepository(database.studyGoalDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        try {
            PDFBoxResourceLoader.init(applicationContext)
        } catch (e: Exception) {
            android.util.Log.w("StudyApplication", "PDFBoxResourceLoader init warning: ${e.message}")
        }
    }
}
