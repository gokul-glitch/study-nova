package com.example.data.repository

import com.example.data.local.SubjectDao
import com.example.data.local.SubjectEntity
import kotlinx.coroutines.flow.Flow

class SubjectRepository(private val dao: SubjectDao) {
    fun getSubjectsForUser(userId: String): Flow<List<SubjectEntity>> = dao.getSubjectsForUser(userId)
    suspend fun insertSubject(subject: SubjectEntity) = dao.insertSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)
}
