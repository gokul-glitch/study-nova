package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM study_materials WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllMaterialsForUser(userId: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE id = :id AND userId = :userId")
    fun getMaterialByIdAndUser(id: Long, userId: String): Flow<StudyMaterialEntity?>

    @Query("SELECT * FROM study_materials WHERE id = :id")
    fun getMaterialById(id: Long): Flow<StudyMaterialEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: StudyMaterialEntity)
}
