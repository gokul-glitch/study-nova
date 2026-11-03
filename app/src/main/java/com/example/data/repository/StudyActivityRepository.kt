package com.example.data.repository

import com.example.data.local.StudyActivityDao
import com.example.data.local.StudyActivityEntity
import kotlinx.coroutines.flow.Flow

class StudyActivityRepository(private val dao: StudyActivityDao) {
    suspend fun recordActivity(
        userId: String, 
        activityType: String, 
        subjectId: Long? = null, 
        materialId: Long? = null
    ) {
        val activity = StudyActivityEntity(
            userId = userId,
            activityType = activityType,
            subjectId = subjectId,
            materialId = materialId
        )
        dao.insertActivity(activity)
    }

    fun getRecentActivities(userId: String, limit: Int = 50): Flow<List<StudyActivityEntity>> {
        return dao.getRecentActivitiesForUser(userId, limit)
    }

    fun getAllActivities(userId: String): Flow<List<StudyActivityEntity>> {
        return dao.getAllActivitiesForUser(userId)
    }
}
