package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.StudyApplication
import com.example.data.local.FlashcardEntity
import com.example.data.local.FlashcardSetEntity
import com.example.data.local.StudyMaterialEntity
import com.example.data.local.SubjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FlashcardsViewModel(application: Application) : AndroidViewModel(application) {
    private val subjectRepository = (application as StudyApplication).subjectRepository
    private val materialRepository = (application as StudyApplication).repository
    private val flashcardRepository = (application as StudyApplication).flashcardRepository
    private val studyActivityRepository = (application as StudyApplication).studyActivityRepository

    private val _currentUserId = MutableStateFlow<String>("guest_user")
    fun setUserId(userId: String) { _currentUserId.value = userId }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<SubjectEntity>> = _currentUserId
        .flatMapLatest { userId -> subjectRepository.getSubjectsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val materials: StateFlow<List<StudyMaterialEntity>> = _currentUserId
        .flatMapLatest { userId -> materialRepository.getAllMaterialsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val flashcardSets: StateFlow<List<FlashcardSetEntity>> = _currentUserId
        .flatMapLatest { userId -> flashcardRepository.getFlashcardSetsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubject = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubject = _selectedSubject.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<StudyMaterialEntity?>(null)
    val selectedMaterial = _selectedMaterial.asStateFlow()

    private val _numCards = MutableStateFlow(10)
    val numCards = _numCards.asStateFlow()

    private val _difficulty = MutableStateFlow("Medium")
    val difficulty = _difficulty.asStateFlow()

    private val _flashcards = MutableStateFlow<List<FlashcardEntity>>(emptyList())
    val flashcards = _flashcards.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()
    
    private val _isFlipped = MutableStateFlow(false)
    val isFlipped = _isFlipped.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private val _masteredIndices = MutableStateFlow<Set<Int>>(emptySet())
    val masteredIndices = _masteredIndices.asStateFlow()

    private val _learningIndices = MutableStateFlow<Set<Int>>(emptySet())
    val learningIndices = _learningIndices.asStateFlow()

    private val _deckTitle = MutableStateFlow("Study Deck")
    val deckTitle = _deckTitle.asStateFlow()

    private val _sessionStartTime = MutableStateFlow(System.currentTimeMillis())
    val sessionStartTime = _sessionStartTime.asStateFlow()

    private val _sessionDurationSeconds = MutableStateFlow(0L)
    val sessionDurationSeconds = _sessionDurationSeconds.asStateFlow()

    fun selectSubject(subject: SubjectEntity?) { _selectedSubject.value = subject }
    fun selectMaterial(material: StudyMaterialEntity?) { _selectedMaterial.value = material }
    fun setNumCards(num: Int) { _numCards.value = num }
    fun setDifficulty(diff: String) { _difficulty.value = diff }
    fun clearError() { _errorMessage.value = null }

    fun generateFlashcards() {
        val material = _selectedMaterial.value
        val subject = _selectedSubject.value
        if (material == null || subject == null) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _currentIndex.value = 0
            _isFlipped.value = false
            _isFinished.value = false
            _masteredIndices.value = emptySet()
            _learningIndices.value = emptySet()
            _sessionStartTime.value = System.currentTimeMillis()
            _sessionDurationSeconds.value = 0L
            _deckTitle.value = "${subject.subjectName} • ${material.title}"
            _flashcards.value = emptyList()
            
            try {
                val generatedFlashcards = fetchFlashcardsFromAI(material, _numCards.value, _difficulty.value, subject.subjectName)
                if (generatedFlashcards.isNotEmpty()) {
                    val setEntity = FlashcardSetEntity(
                        userId = _currentUserId.value,
                        subjectId = subject.id,
                        materialId = material.id,
                        totalCards = generatedFlashcards.size
                    )
                    flashcardRepository.saveFlashcardSet(setEntity, generatedFlashcards)
                    studyActivityRepository.recordActivity(_currentUserId.value, "GENERATE_FLASHCARDS", subject.id, material.id)
                    _flashcards.value = generatedFlashcards
                } else {
                    _errorMessage.value = "Failed to generate flashcards. Please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unknown error occurred"
            }
            _isLoading.value = false
        }
    }

    fun loadFlashcardSet(setId: Long, title: String? = null) {
        viewModelScope.launch {
            val cards = flashcardRepository.getFlashcardsForSet(setId)
            if (cards.isNotEmpty()) {
                _flashcards.value = cards
                _currentIndex.value = 0
                _isFlipped.value = false
                _isFinished.value = false
                _masteredIndices.value = emptySet()
                _learningIndices.value = emptySet()
                _sessionStartTime.value = System.currentTimeMillis()
                _sessionDurationSeconds.value = 0L
                _deckTitle.value = title ?: "Flashcard Deck"
            }
        }
    }

    fun startQuickPractice(topic: String = "Cognitive Science & Study Skills") {
        val starterCards = listOf(
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Active Recall",
                back = "The practice of retrieving information from memory during the learning process without looking at notes, strengthening neural pathways and retention."
            ),
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Spaced Repetition",
                back = "Reviewing information at increasing intervals over time to counter the forgetting curve and transfer knowledge into long-term memory."
            ),
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Feynman Technique",
                back = "A 4-step framework: choose a concept, teach it in plain language to a beginner, pinpoint gaps in explanation, and simplify/clarify your mental model."
            ),
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Interleaving",
                back = "Mixing different subjects or problem types during a single session, training the brain to categorize problems and choose appropriate solutions."
            ),
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Dual Coding",
                back = "Combining verbal explanations with visual representations (diagrams, mind maps) to process data across two separate cognitive channels."
            ),
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Cognitive Load",
                back = "The total amount of mental effort used in working memory. Optimal learning minimizes extraneous load while maximizing germane processing."
            ),
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Metacognition",
                back = "Thinking about your own thinking; self-monitoring your comprehension, recognizing when you do not understand, and adjusting strategies."
            ),
            FlashcardEntity(
                userId = _currentUserId.value,
                materialId = 0L,
                front = "Pomodoro Technique",
                back = "A time-management method breaking study into focused 25-minute intervals separated by 5-minute restorative breaks to sustain mental stamina."
            )
        )
        _flashcards.value = starterCards
        _currentIndex.value = 0
        _isFlipped.value = false
        _isFinished.value = false
        _masteredIndices.value = emptySet()
        _learningIndices.value = emptySet()
        _sessionStartTime.value = System.currentTimeMillis()
        _sessionDurationSeconds.value = 0L
        _deckTitle.value = topic
    }

    fun deleteFlashcardSet(setId: Long) {
        viewModelScope.launch {
            flashcardRepository.deleteFlashcardSet(setId)
            if (_flashcards.value.isNotEmpty() && _flashcards.value[0].setId == setId) {
                resetSession()
            }
        }
    }

    private var hasLoggedStudyInCurrentSession = false
    private var cardsInteractedInSession = 0

    fun swipeRight() {
        markCard(isMastered = true)
    }

    fun swipeLeft() {
        markCard(isMastered = false)
    }

    fun markCard(isMastered: Boolean) {
        val currIdx = _currentIndex.value
        val list = _flashcards.value
        if (currIdx in list.indices) {
            val card = list[currIdx]
            if (isMastered) {
                _masteredIndices.value = _masteredIndices.value + currIdx
                _learningIndices.value = _learningIndices.value - currIdx
                if (card.id > 0) {
                    viewModelScope.launch {
                        flashcardRepository.updateMastery(card.id, true)
                    }
                }
            } else {
                _learningIndices.value = _learningIndices.value + currIdx
                _masteredIndices.value = _masteredIndices.value - currIdx
                if (card.id > 0) {
                    viewModelScope.launch {
                        flashcardRepository.updateMastery(card.id, false)
                    }
                }
            }
            advanceOrFinish()
        }
    }

    private fun advanceOrFinish() {
        cardsInteractedInSession++
        val total = _flashcards.value.size
        if (_currentIndex.value < total - 1) {
            _currentIndex.value += 1
            _isFlipped.value = false
        } else {
            finishSession()
        }
    }

    private fun finishSession() {
        _isFinished.value = true
        _sessionDurationSeconds.value = ((System.currentTimeMillis() - _sessionStartTime.value) / 1000).coerceAtLeast(1)
        if (!hasLoggedStudyInCurrentSession) {
            hasLoggedStudyInCurrentSession = true
            viewModelScope.launch {
                studyActivityRepository.recordActivity(
                    _currentUserId.value,
                    "STUDY_FLASHCARDS",
                    _selectedSubject.value?.id,
                    _selectedMaterial.value?.id
                )
            }
        }
    }

    fun reviewMissedCards() {
        val missed = _learningIndices.value.mapNotNull { idx ->
            _flashcards.value.getOrNull(idx)
        }
        if (missed.isNotEmpty()) {
            _flashcards.value = missed
            _currentIndex.value = 0
            _isFlipped.value = false
            _isFinished.value = false
            _masteredIndices.value = emptySet()
            _learningIndices.value = emptySet()
            _sessionStartTime.value = System.currentTimeMillis()
            _sessionDurationSeconds.value = 0L
            _deckTitle.value = "Reviewing Missed Terms"
            hasLoggedStudyInCurrentSession = false
            cardsInteractedInSession = 0
        }
    }

    fun nextCard() {
        advanceOrFinish()
    }
    
    fun prevCard() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            _isFlipped.value = false
            val prevIdx = _currentIndex.value
            _masteredIndices.value = _masteredIndices.value - prevIdx
            _learningIndices.value = _learningIndices.value - prevIdx
        }
    }

    fun flipCard() { 
        _isFlipped.value = !_isFlipped.value 
        cardsInteractedInSession++
        if (cardsInteractedInSession >= 2 && !hasLoggedStudyInCurrentSession) {
            hasLoggedStudyInCurrentSession = true
            viewModelScope.launch {
                studyActivityRepository.recordActivity(_currentUserId.value, "STUDY_FLASHCARDS", _selectedSubject.value?.id, _selectedMaterial.value?.id)
            }
        }
    }
    
    fun studyAgain() {
        _currentIndex.value = 0
        _isFlipped.value = false
        _isFinished.value = false
        _masteredIndices.value = emptySet()
        _learningIndices.value = emptySet()
        _sessionStartTime.value = System.currentTimeMillis()
        _sessionDurationSeconds.value = 0L
        hasLoggedStudyInCurrentSession = false
        cardsInteractedInSession = 0
    }

    fun resetSession() {
        _flashcards.value = emptyList()
        _currentIndex.value = 0
        _isFlipped.value = false
        _isFinished.value = false
        _masteredIndices.value = emptySet()
        _learningIndices.value = emptySet()
        _errorMessage.value = null
        _sessionDurationSeconds.value = 0L
        hasLoggedStudyInCurrentSession = false
        cardsInteractedInSession = 0
    }

    private suspend fun fetchFlashcardsFromAI(
        material: StudyMaterialEntity, 
        numCards: Int, 
        difficulty: String,
        subjectName: String
    ): List<FlashcardEntity> = withContext(Dispatchers.IO) {
        val cards = mutableListOf<FlashcardEntity>()
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                throw Exception("Error: Gemini API key is missing or not configured.")
            }
            
            val textContent = material.textContent ?: "General context."
            val safeText = if (textContent.length > 40000) textContent.substring(0, 40000) else textContent
            
            val promptText = """
                You are a $subjectName study assistant.
                Generate exactly $numCards study flashcards with $difficulty difficulty based ONLY on the following text.
                Format the output strictly as a JSON array of objects, with NO markdown formatting, NO backticks.
                Do not invent information unrelated to the uploaded material.
                Each object must have exactly two keys: "front" (the question, concept, or term) and "back" (the correct answer and a brief clear explanation).
                
                Text:
                $safeText
            """.trimIndent()

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            connection.doOutput = true
            
            val requestBody = JSONObject()

            // System instruction for rapid flashcard creation
            val systemInstructionObj = JSONObject()
            val sysPartsArray = org.json.JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", "You are Study Nova's Rapid Flashcard Engine designed for instant speed. You extract essential study concepts and output strictly formatted JSON flashcards without markdown formatting or conversational filler.")
            sysPartsArray.put(sysPartObj)
            systemInstructionObj.put("parts", sysPartsArray)
            requestBody.put("systemInstruction", systemInstructionObj)

            val contentsArray = org.json.JSONArray()
            val contentObj = JSONObject()
            val partsArray = org.json.JSONArray()
            val partObj = JSONObject()
            partObj.put("text", promptText)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBody.put("contents", contentsArray)
            
            connection.outputStream.use { os ->
                val input = requestBody.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }
            
            if (connection.responseCode == 200) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    var aiText = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")?.getJSONObject(0)?.optString("text", "") ?: ""
                    aiText = aiText.replace("```json", "").replace("```", "").trim()
                    
                    if (aiText.startsWith("{")) {
                        val obj = JSONObject(aiText)
                        val keys = obj.keys()
                        while(keys.hasNext()) {
                            val key = keys.next()
                            if (obj.optJSONArray(key) != null) {
                                aiText = obj.getJSONArray(key).toString()
                                break
                            }
                        }
                    }

                    val jsonArray = JSONArray(aiText)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        cards.add(
                            FlashcardEntity(
                                userId = _currentUserId.value,
                                materialId = material.id,
                                front = obj.optString("front"),
                                back = obj.optString("back")
                            )
                        )
                    }
                } else {
                    throw Exception("Could not generate flashcards content.")
                }
            } else {
                val errorDetails = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No details"
                } catch (e: Exception) {
                    "Could not read error stream"
                }
                throw Exception("API Error ${connection.responseCode}: $errorDetails")
            }
        } catch (e: Exception) { 
            e.printStackTrace()
            throw e
        }
        cards
    }
}
