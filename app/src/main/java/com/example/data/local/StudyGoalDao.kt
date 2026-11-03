package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyGoalDao {
    @Query("SELECT * FROM study_goals WHERE userId = :userId LIMIT 1")
    fun getStudyGoal(userId: String): Flow<StudyGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: StudyGoalEntity)

    @Query("DELETE FROM study_goals WHERE userId = :userId")
    suspend fun deleteGoal(userId: String)
}
