package com.example.data.repository

import com.example.data.local.StudyMaterialDao
import com.example.data.local.StudyMaterialEntity
import kotlinx.coroutines.flow.Flow

class StudyMaterialRepository(private val dao: StudyMaterialDao) {
    
    fun getAllMaterialsForUser(userId: String): Flow<List<StudyMaterialEntity>> = dao.getAllMaterialsForUser(userId)

    fun getMaterialByIdAndUser(id: Long, userId: String): Flow<StudyMaterialEntity?> = dao.getMaterialByIdAndUser(id, userId)

    fun getMaterialById(id: Long): Flow<StudyMaterialEntity?> = dao.getMaterialById(id)

    suspend fun insert(material: StudyMaterialEntity) {
        dao.insertMaterial(material)
    }

    suspend fun delete(material: StudyMaterialEntity) {
        dao.deleteMaterial(material)
    }
}
