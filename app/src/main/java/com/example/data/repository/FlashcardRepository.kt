package com.example.data.repository

import com.example.data.local.FlashcardDao
import com.example.data.local.FlashcardEntity
import com.example.data.local.FlashcardSetEntity
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(private val dao: FlashcardDao) {
    suspend fun saveFlashcardSet(set: FlashcardSetEntity, flashcards: List<FlashcardEntity>): Long {
        val setId = dao.insertFlashcardSet(set)
        val flashcardsWithSetId = flashcards.map { it.copy(setId = setId) }
        dao.insertFlashcards(flashcardsWithSetId)
        return setId
    }

    fun getFlashcardSetsForUser(userId: String): Flow<List<FlashcardSetEntity>> {
        return dao.getFlashcardSetsForUser(userId)
    }

    suspend fun getFlashcardsForSet(setId: Long): List<FlashcardEntity> {
        return dao.getFlashcardsForSet(setId)
    }

    suspend fun deleteFlashcardSet(setId: Long) {
        dao.deleteFlashcardsForSet(setId)
        dao.deleteFlashcardSet(setId)
    }

    suspend fun updateMastery(cardId: Long, isMastered: Boolean) {
        if (cardId > 0) {
            dao.updateMastery(cardId, isMastered)
        }
    }
}
