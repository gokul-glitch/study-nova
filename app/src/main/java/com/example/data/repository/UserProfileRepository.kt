package com.example.data.repository

import com.example.data.local.UserProfileDao
import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val dao: UserProfileDao) {
    fun getUserProfile(userId: String): Flow<UserProfileEntity?> = dao.getUserProfile(userId)
    
    suspend fun getUserByEmail(email: String): UserProfileEntity? = dao.getUserByEmail(email)

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.insertProfile(profile)
    }
}
