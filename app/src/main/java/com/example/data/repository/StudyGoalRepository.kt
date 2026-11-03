package com.example.data.repository

import com.example.data.local.StudyGoalDao
import com.example.data.local.StudyGoalEntity
import kotlinx.coroutines.flow.Flow

class StudyGoalRepository(private val studyGoalDao: StudyGoalDao) {
    fun getStudyGoal(userId: String): Flow<StudyGoalEntity?> =
        studyGoalDao.getStudyGoal(userId)

    suspend fun saveStudyGoal(goal: StudyGoalEntity) {
        studyGoalDao.insertOrUpdateGoal(goal)
    }
}
