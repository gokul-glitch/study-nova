package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Update
    suspend fun updateQuestions(questions: List<QuizQuestionEntity>)

    @Query("SELECT * FROM quizzes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getQuizzesForUser(userId: String): Flow<List<QuizEntity>>
    
    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: Long): QuizEntity?

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    suspend fun getQuestionsForQuiz(quizId: Long): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    fun getQuestionsForQuizFlow(quizId: Long): Flow<List<QuizQuestionEntity>>

    @Query("UPDATE quizzes SET score = :score, percentage = :percentage, correctCount = :correctCount, incorrectCount = :incorrectCount WHERE id = :quizId")
    suspend fun updateQuizResult(quizId: Long, score: Int, percentage: Float, correctCount: Int, incorrectCount: Int)

    @Query("UPDATE quizzes SET score = :score WHERE id = :quizId")
    suspend fun updateQuizScore(quizId: Long, score: Int)

    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuiz(quizId: Long)

    @Query("DELETE FROM quiz_questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Long)
}
