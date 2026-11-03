package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyActivityDao {
    @Insert
    suspend fun insertActivity(activity: StudyActivityEntity)
    
    @Query("SELECT * FROM study_activities WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivitiesForUser(userId: String, limit: Int = 50): Flow<List<StudyActivityEntity>>
    
    @Query("SELECT * FROM study_activities WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllActivitiesForUser(userId: String): Flow<List<StudyActivityEntity>>
}
