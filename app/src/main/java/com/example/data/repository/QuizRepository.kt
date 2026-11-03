package com.example.data.repository

import com.example.data.local.QuizDao
import com.example.data.local.QuizEntity
import com.example.data.local.QuizQuestionEntity
import kotlinx.coroutines.flow.Flow

class QuizRepository(private val dao: QuizDao) {
    suspend fun createQuiz(quiz: QuizEntity, questions: List<QuizQuestionEntity>): Long {
        val quizId = dao.insertQuiz(quiz)
        val questionsWithQuizId = questions.map { it.copy(quizId = quizId) }
        dao.insertQuestions(questionsWithQuizId)
        return quizId
    }

    suspend fun updateQuizResult(quizId: Long, score: Int, percentage: Float, correctCount: Int, incorrectCount: Int) {
        dao.updateQuizResult(quizId, score, percentage, correctCount, incorrectCount)
    }

    suspend fun saveCompletedQuiz(quiz: QuizEntity, questions: List<QuizQuestionEntity>) {
        dao.updateQuiz(quiz)
        dao.updateQuestions(questions)
    }

    fun getQuizzesForUser(userId: String): Flow<List<QuizEntity>> {
        return dao.getQuizzesForUser(userId)
    }

    suspend fun getQuizById(quizId: Long): QuizEntity? {
        return dao.getQuizById(quizId)
    }

    suspend fun getQuestionsForQuiz(quizId: Long): List<QuizQuestionEntity> {
        return dao.getQuestionsForQuiz(quizId)
    }

    suspend fun deleteQuiz(quizId: Long) {
        dao.deleteQuestionsForQuiz(quizId)
        dao.deleteQuiz(quizId)
    }
}
