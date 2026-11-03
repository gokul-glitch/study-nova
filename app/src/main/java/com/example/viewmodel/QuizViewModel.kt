package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyApplication
import com.example.data.local.QuizEntity
import com.example.data.local.QuizQuestionEntity
import com.example.data.local.StudyMaterialEntity
import com.example.data.local.SubjectEntity
import com.example.service.QuizGeneratorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val subjectRepository = (application as StudyApplication).subjectRepository
    private val materialRepository = (application as StudyApplication).repository
    private val quizRepository = (application as StudyApplication).quizRepository
    private val studyActivityRepository = (application as StudyApplication).studyActivityRepository
    private val quizService = QuizGeneratorService()

    private val _currentUserId = MutableStateFlow("guest_user")
    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<SubjectEntity>> = _currentUserId
        .flatMapLatest { userId -> subjectRepository.getSubjectsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val materials: StateFlow<List<StudyMaterialEntity>> = _currentUserId
        .flatMapLatest { userId -> materialRepository.getAllMaterialsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val quizHistory: StateFlow<List<QuizEntity>> = _currentUserId
        .flatMapLatest { userId -> quizRepository.getQuizzesForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubject = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubject = _selectedSubject.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<StudyMaterialEntity?>(null)
    val selectedMaterial = _selectedMaterial.asStateFlow()

    private val _isAllMaterialsSelected = MutableStateFlow(false)
    val isAllMaterialsSelected = _isAllMaterialsSelected.asStateFlow()

    private val _numQuestions = MutableStateFlow(10)
    val numQuestions = _numQuestions.asStateFlow()

    private val _difficulty = MutableStateFlow("Medium")
    val difficulty = _difficulty.asStateFlow()

    private val _questionType = MutableStateFlow("Multiple Choice")
    val questionType = _questionType.asStateFlow()

    private val _questions = MutableStateFlow<List<QuizQuestionEntity>>(emptyList())
    val questions = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val selectedAnswers = _selectedAnswers.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private val _currentQuiz = MutableStateFlow<QuizEntity?>(null)
    val currentQuiz = _currentQuiz.asStateFlow()

    fun selectSubject(subject: SubjectEntity?) {
        _selectedSubject.value = subject
        _selectedMaterial.value = null
        _isAllMaterialsSelected.value = false
        _errorMessage.value = null
    }

    fun selectSubjectById(subjectId: Long) {
        val found = subjects.value.find { it.id == subjectId }
        if (found != null) {
            selectSubject(found)
        }
    }

    fun selectMaterial(material: StudyMaterialEntity?) {
        _selectedMaterial.value = material
        _isAllMaterialsSelected.value = false
        _errorMessage.value = null
    }

    fun selectAllMaterialsForSubject(useAll: Boolean) {
        _isAllMaterialsSelected.value = useAll
        if (useAll) {
            _selectedMaterial.value = null
        }
        _errorMessage.value = null
    }

    fun setNumQuestions(num: Int) {
        _numQuestions.value = num
    }

    fun setDifficulty(diff: String) {
        _difficulty.value = diff
    }

    fun setQuestionType(type: String) {
        _questionType.value = type
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun generateQuiz(onSuccess: () -> Unit = {}) {
        val subject = _selectedSubject.value
        if (subject == null) {
            _errorMessage.value = "Please select a subject first."
            return
        }

        val allSubjectMaterials = materials.value.filter { it.subjectId == subject.id }
        if (allSubjectMaterials.isEmpty()) {
            _errorMessage.value = "No study material available for this subject."
            return
        }

        val combinedContent = StringBuilder()
        var materialTitleUsed = "All Materials"

        if (_isAllMaterialsSelected.value) {
            allSubjectMaterials.forEach { mat ->
                mat.textContent?.let { text ->
                    if (text.isNotBlank()) {
                        combinedContent.append("--- Document: ${mat.title} ---\n")
                        combinedContent.append(text).append("\n\n")
                    }
                }
            }
            materialTitleUsed = "All ${subject.subjectName} Notes"
        } else {
            val material = _selectedMaterial.value
            if (material == null) {
                _errorMessage.value = "Please select a study material or choose 'All Materials'."
                return
            }
            materialTitleUsed = material.title
            combinedContent.append(material.textContent ?: "")
        }

        if (combinedContent.isBlank()) {
            _errorMessage.value = "Selected material has no readable text content."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isFinished.value = false
            _score.value = 0
            _currentQuestionIndex.value = 0
            _selectedAnswers.value = emptyMap()
            _questions.value = emptyList()

            try {
                val generated = quizService.generateQuiz(
                    subjectName = subject.subjectName,
                    materialContent = combinedContent.toString(),
                    numQuestions = _numQuestions.value,
                    difficulty = _difficulty.value,
                    questionType = _questionType.value
                )

                if (generated.questions.isNotEmpty()) {
                    val newQuizEntity = QuizEntity(
                        userId = _currentUserId.value,
                        subjectId = subject.id,
                        subjectName = subject.subjectName,
                        materialId = _selectedMaterial.value?.id,
                        materialTitle = materialTitleUsed,
                        quizTitle = generated.title,
                        difficulty = generated.difficulty,
                        questionType = _questionType.value,
                        totalQuestions = generated.questions.size,
                        score = 0,
                        percentage = 0f,
                        correctCount = 0,
                        incorrectCount = 0
                    )

                    val savedQuizId = quizRepository.createQuiz(newQuizEntity, generated.questions)
                    val fullQuiz = newQuizEntity.copy(id = savedQuizId)
                    _currentQuiz.value = fullQuiz
                    _questions.value = generated.questions.map { it.copy(quizId = savedQuizId) }
                    _isLoading.value = false
                    onSuccess()
                } else {
                    _errorMessage.value = "Quiz generation failed. Please try again."
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Quiz generation failed. Please try again."
                _isLoading.value = false
            }
        }
    }

    fun answerQuestion(answerText: String) {
        val currentQIndex = _currentQuestionIndex.value
        val questionsList = _questions.value
        if (currentQIndex >= questionsList.size) return
        if (_selectedAnswers.value.containsKey(currentQIndex)) return

        val updatedMap = _selectedAnswers.value.toMutableMap()
        updatedMap[currentQIndex] = answerText
        _selectedAnswers.value = updatedMap

        if (questionsList[currentQIndex].correctAnswer.equals(answerText, ignoreCase = true)) {
            _score.value += 1
        }
    }

    fun nextQuestion(onFinish: () -> Unit = {}) {
        val currentQIndex = _currentQuestionIndex.value
        val questionsList = _questions.value
        if (currentQIndex < questionsList.size - 1) {
            _currentQuestionIndex.value += 1
        } else {
            finishQuiz(onFinish)
        }
    }

    fun finishQuiz(onCompleted: () -> Unit = {}) {
        _isFinished.value = true
        val quiz = _currentQuiz.value
        val questionsList = _questions.value
        val answersMap = _selectedAnswers.value

        var correctCount = 0
        val updatedQuestions = questionsList.mapIndexed { index, question ->
            val userAns = answersMap[index]
            val isCorr = question.correctAnswer.equals(userAns, ignoreCase = true)
            if (isCorr) correctCount++
            question.copy(studentAnswer = userAns, isCorrect = isCorr)
        }

        val total = questionsList.size
        val incorrectCount = maxOf(0, total - correctCount)
        val percentage = if (total > 0) (correctCount.toFloat() / total) * 100f else 0f

        _score.value = correctCount
        _questions.value = updatedQuestions

        viewModelScope.launch {
            if (quiz != null) {
                val updatedQuiz = quiz.copy(
                    score = correctCount,
                    percentage = percentage,
                    correctCount = correctCount,
                    incorrectCount = incorrectCount
                )
                _currentQuiz.value = updatedQuiz
                quizRepository.saveCompletedQuiz(updatedQuiz, updatedQuestions)
                studyActivityRepository.recordActivity(
                    userId = _currentUserId.value,
                    activityType = "COMPLETE_QUIZ",
                    subjectId = updatedQuiz.subjectId,
                    materialId = updatedQuiz.materialId
                )
            }
            onCompleted()
        }
    }

    fun retryCurrentQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _score.value = 0
        _isFinished.value = false
        _errorMessage.value = null
        // Reset student answers in current questions list
        _questions.value = _questions.value.map { it.copy(studentAnswer = null, isCorrect = false) }
    }

    fun resetForNewQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _score.value = 0
        _isFinished.value = false
        _questions.value = emptyList()
        _currentQuiz.value = null
        _errorMessage.value = null
    }

    fun loadCompletedQuiz(quiz: QuizEntity, onLoaded: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val loadedQuestions = quizRepository.getQuestionsForQuiz(quiz.id)
            _currentQuiz.value = quiz
            _questions.value = loadedQuestions
            _score.value = quiz.correctCount
            val map = mutableMapOf<Int, String>()
            loadedQuestions.forEachIndexed { index, q ->
                q.studentAnswer?.let { map[index] = it }
            }
            _selectedAnswers.value = map
            _isFinished.value = true
            _currentQuestionIndex.value = 0
            _isLoading.value = false
            onLoaded()
        }
    }
}
