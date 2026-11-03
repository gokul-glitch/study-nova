package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcardSet(flashcardSet: FlashcardSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Query("SELECT * FROM flashcard_sets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getFlashcardSetsForUser(userId: String): Flow<List<FlashcardSetEntity>>

    @Query("SELECT * FROM flashcard_sets WHERE id = :setId")
    suspend fun getFlashcardSetById(setId: Long): FlashcardSetEntity?

    @Query("SELECT * FROM flashcards WHERE setId = :setId ORDER BY id ASC")
    suspend fun getFlashcardsForSet(setId: Long): List<FlashcardEntity>

    @Query("DELETE FROM flashcard_sets WHERE id = :setId")
    suspend fun deleteFlashcardSet(setId: Long)

    @Query("DELETE FROM flashcards WHERE setId = :setId")
    suspend fun deleteFlashcardsForSet(setId: Long)

    @Query("UPDATE flashcards SET isMastered = :isMastered WHERE id = :cardId")
    suspend fun updateMastery(cardId: Long, isMastered: Boolean)
}
