package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.StudyApplication
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

data class GroundingSource(
    val title: String,
    val url: String
)

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val modelUsed: String? = null,
    val roleName: String? = null,
    val webSearchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList(),
    val isGrounded: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class TutorResponse(
    val text: String,
    val modelUsed: String,
    val roleName: String,
    val webSearchQueries: List<String>,
    val sources: List<GroundingSource>,
    val isGrounded: Boolean
)

enum class TutorRole(
    val id: String,
    val displayName: String,
    val modelName: String,
    val badgeLabel: String,
    val description: String,
    val defaultSearchGrounding: Boolean,
    val systemInstruction: String
) {
    ACADEMIC_RESEARCH(
        id = "research",
        displayName = "Research & Study Buddy",
        modelName = "gemini-2.5-flash",
        badgeLabel = "Grounded",
        description = "General study assistant with Google Search Grounding for verified web facts.",
        defaultSearchGrounding = true,
        systemInstruction = "You are Study Nova's Academic Research & Study Buddy. You provide accurate, grounded academic explanations, cite real-world web sources using Google Search when needed, and assist students with comprehensive study insights. Ensure all explanations are factual, up-to-date, pedagogical, and clear. Ground your answers with credible facts."
    ),
    STEM_EXAM_COACH(
        id = "stem_coach",
        displayName = "STEM & Exam Coach",
        modelName = "gemini-2.5-flash",
        badgeLabel = "Deep Reasoning",
        description = "Complex tasks: advanced reasoning, mathematical derivations, STEM problem solving & exam prep.",
        defaultSearchGrounding = false,
        systemInstruction = "You are Study Nova's Senior STEM & Exam Coach specializing in deep reasoning, step-by-step mathematical problem solving, rigorous concept breakdown, and exam mastery. Walk through complex derivations clearly, challenge students with thought-provoking questions, and verify logical correctness at every step."
    ),
    RAPID_REVISION(
        id = "rapid_revision",
        displayName = "Rapid Flash Tutor",
        modelName = "gemini-2.5-flash",
        badgeLabel = "Fast",
        description = "Fast tasks: instant answers, high-yield bulleted summaries, mnemonics & quick revision.",
        defaultSearchGrounding = false,
        systemInstruction = "You are Study Nova's Rapid Revision Tutor. You deliver ultra-fast, concise, high-yield bulleted summaries, mnemonics, and instant concept definitions so students can review critical exam material in seconds without fluff."
    ),
    SOCRATIC_MENTOR(
        id = "socratic",
        displayName = "Socratic Academic Mentor",
        modelName = "gemini-2.5-flash",
        badgeLabel = "Guided",
        description = "Pedagogical role: guides you with targeted questions and hints to master deep concepts.",
        defaultSearchGrounding = false,
        systemInstruction = "You are Study Nova's Socratic Academic Mentor. Rather than directly providing full answers, guide the student with thoughtful questions, scaffolding hints, and analogies so they build self-sufficient reasoning and deep conceptual intuition."
    )
}

class AITutorViewModel(application: Application) : AndroidViewModel(application) {
    private val subjectRepository = (application as StudyApplication).subjectRepository
    private val materialRepository = (application as StudyApplication).repository
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

    private val _selectedSubject = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubject = _selectedSubject.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<StudyMaterialEntity?>(null)
    val selectedMaterial = _selectedMaterial.asStateFlow()

    private val _selectedRole = MutableStateFlow(TutorRole.ACADEMIC_RESEARCH)
    val selectedRole = _selectedRole.asStateFlow()

    private val _isSearchGroundingEnabled = MutableStateFlow(true)
    val isSearchGroundingEnabled = _isSearchGroundingEnabled.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun selectSubject(subject: SubjectEntity?) { _selectedSubject.value = subject }
    fun selectMaterial(material: StudyMaterialEntity?) { _selectedMaterial.value = material }
    
    fun selectRole(role: TutorRole) {
        _selectedRole.value = role
        // Align default search grounding when switching to research role
        if (role == TutorRole.ACADEMIC_RESEARCH) {
            _isSearchGroundingEnabled.value = true
        }
    }

    fun toggleSearchGrounding() {
        _isSearchGroundingEnabled.value = !_isSearchGroundingEnabled.value
    }

    fun setSearchGrounding(enabled: Boolean) {
        _isSearchGroundingEnabled.value = enabled
    }

    fun clearChat() { _messages.value = emptyList() }

    fun retryLastMessage() {
        val currentMsgs = _messages.value.toMutableList()
        // Remove the last error message
        if (currentMsgs.isNotEmpty() && !currentMsgs.last().isUser && 
            (currentMsgs.last().text.startsWith("Error:") || currentMsgs.last().text.startsWith("API Error:"))) {
            currentMsgs.removeLast()
        }
        _messages.value = currentMsgs
        
        // Find the last user message to resend
        val lastUserMsg = currentMsgs.lastOrNull { it.isUser }?.text
        if (lastUserMsg != null) {
            viewModelScope.launch {
                _isLoading.value = true
                val response = generateAnswer(lastUserMsg)
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(
                    ChatMessage(
                        isUser = false,
                        text = response.text,
                        modelUsed = response.modelUsed,
                        roleName = response.roleName,
                        webSearchQueries = response.webSearchQueries,
                        sources = response.sources,
                        isGrounded = response.isGrounded
                    )
                )
                _messages.value = updatedMessages
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(question: String) {
        if (question.isBlank()) return
        
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(
            ChatMessage(
                isUser = true,
                text = question,
                roleName = _selectedRole.value.displayName
            )
        )
        _messages.value = currentMessages

        viewModelScope.launch {
            _isLoading.value = true
            val response = generateAnswer(question)
            
            val updatedMessages = _messages.value.toMutableList()
            updatedMessages.add(
                ChatMessage(
                    isUser = false,
                    text = response.text,
                    modelUsed = response.modelUsed,
                    roleName = response.roleName,
                    webSearchQueries = response.webSearchQueries,
                    sources = response.sources,
                    isGrounded = response.isGrounded
                )
            )
            _messages.value = updatedMessages
            
            if (!response.text.startsWith("Error:") && !response.text.startsWith("API Error:")) {
                studyActivityRepository.recordActivity(
                    _currentUserId.value,
                    "USE_AI_TUTOR",
                    _selectedSubject.value?.id,
                    _selectedMaterial.value?.id
                )
            }
            
            _isLoading.value = false
        }
    }

    private suspend fun generateAnswer(question: String): TutorResponse = withContext(Dispatchers.IO) {
        val currentRole = _selectedRole.value
        val useSearchGrounding = _isSearchGroundingEnabled.value
        
        // Model Selection rule:
        // - Search Grounding: gemini-3.5-flash with googleSearch tool
        // - Complex STEM/Reasoning: gemini-3.1-pro-preview
        // - Fast tasks: gemini-3.1-flash-lite-preview
        // - General tasks: gemini-3.5-flash
        val modelToUse = "gemini-2.5-flash"

        try {
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext TutorResponse(
                    text = "Error: Gemini API key is missing or not configured. Please add your key in the Secrets panel.",
                    modelUsed = modelToUse,
                    roleName = currentRole.displayName,
                    webSearchQueries = emptyList(),
                    sources = emptyList(),
                    isGrounded = false
                )
            }

            val subjectName = _selectedSubject.value?.subjectName ?: "General"
            val materialText = _selectedMaterial.value?.textContent ?: ""

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelToUse:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            connection.doOutput = true

            val requestBody = JSONObject()

            // 1. System Instruction defining the chatbot role
            val systemInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            val fullSystemPrompt = buildString {
                append(currentRole.systemInstruction)
                append("\n\nAcademic Subject: ").append(subjectName)
                if (useSearchGrounding) {
                    append("\nGoogle Search Grounding is ENABLED. Search the web for up-to-date and accurate information whenever relevant, and cite facts precisely.")
                }
            }
            sysPartObj.put("text", fullSystemPrompt)
            sysPartsArray.put(sysPartObj)
            systemInstructionObj.put("parts", sysPartsArray)
            requestBody.put("systemInstruction", systemInstructionObj)

            // 2. Tools: Add googleSearch tool when Search Grounding is enabled
            if (useSearchGrounding) {
                val toolsArray = JSONArray()
                val searchToolObj = JSONObject()
                searchToolObj.put("googleSearch", JSONObject())
                toolsArray.put(searchToolObj)
                requestBody.put("tools", toolsArray)
            }

            // 3. Conversation Contents
            val contentsArray = JSONArray()
            val validHistory = _messages.value.filter { msg ->
                msg.isUser || (!msg.text.startsWith("Error:") && !msg.text.startsWith("API Error:"))
            }

            // Format previous conversation turns alternating user and model
            val mergedTurns = mutableListOf<Pair<String, StringBuilder>>()
            for (msg in validHistory) {
                val role = if (msg.isUser) "user" else "model"
                if (mergedTurns.isNotEmpty() && mergedTurns.last().first == role) {
                    mergedTurns.last().second.append("\n").append(msg.text)
                } else {
                    mergedTurns.add(role to StringBuilder(msg.text))
                }
            }
            if (mergedTurns.isNotEmpty() && mergedTurns.first().first != "user") {
                mergedTurns.removeAt(0)
            }

            // Prepare prompt context
            val materialContext = if (materialText.isNotBlank()) {
                val safeMaterial = if (materialText.length > 30000) materialText.substring(0, 30000) else materialText
                "[Study Material Context: ${_selectedMaterial.value?.title ?: "Document"}]:\n$safeMaterial\n\n"
            } else ""

            val currentUserPrompt = if (mergedTurns.isEmpty() && materialContext.isNotBlank()) {
                "$materialContext$question"
            } else {
                question
            }

            if (mergedTurns.isNotEmpty() && mergedTurns.last().first == "user") {
                mergedTurns.last().second.append("\n").append(currentUserPrompt)
            } else {
                mergedTurns.add("user" to StringBuilder(currentUserPrompt))
            }

            for ((role, textBuilder) in mergedTurns) {
                val turnObj = JSONObject()
                turnObj.put("role", role)
                val parts = JSONArray()
                val part = JSONObject()
                part.put("text", textBuilder.toString())
                parts.put(part)
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

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
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val responseText = if (parts != null && parts.length() > 0) {
                        parts.getJSONObject(0).optString("text", "No text generated.")
                    } else {
                        "No text generated."
                    }

                    // Extract Search Grounding metadata
                    val searchQueries = mutableListOf<String>()
                    val sources = mutableListOf<GroundingSource>()
                    val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
                    
                    if (groundingMetadata != null) {
                        val queriesArray = groundingMetadata.optJSONArray("webSearchQueries")
                        if (queriesArray != null) {
                            for (i in 0 until queriesArray.length()) {
                                val query = queriesArray.optString(i)
                                if (query.isNotBlank()) searchQueries.add(query)
                            }
                        }

                        val chunksArray = groundingMetadata.optJSONArray("groundingChunks")
                        if (chunksArray != null) {
                            for (i in 0 until chunksArray.length()) {
                                val chunkObj = chunksArray.optJSONObject(i)
                                val webObj = chunkObj?.optJSONObject("web")
                                if (webObj != null) {
                                    val title = webObj.optString("title", "Web Source")
                                    val uri = webObj.optString("uri", "")
                                    if (uri.isNotBlank()) {
                                        sources.add(GroundingSource(title = title, url = uri))
                                    }
                                }
                            }
                        }
                    }

                    val isGrounded = useSearchGrounding || searchQueries.isNotEmpty() || sources.isNotEmpty()

                    return@withContext TutorResponse(
                        text = responseText,
                        modelUsed = modelToUse,
                        roleName = currentRole.displayName,
                        webSearchQueries = searchQueries,
                        sources = sources,
                        isGrounded = isGrounded
                    )
                }
                return@withContext TutorResponse(
                    text = "Error: Could not generate an answer.",
                    modelUsed = modelToUse,
                    roleName = currentRole.displayName,
                    webSearchQueries = emptyList(),
                    sources = emptyList(),
                    isGrounded = false
                )
            } else {
                val errorDetails = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No details"
                } catch (e: Exception) {
                    "Could not read error stream"
                }
                return@withContext TutorResponse(
                    text = "API Error: ${connection.responseCode} - $errorDetails",
                    modelUsed = modelToUse,
                    roleName = currentRole.displayName,
                    webSearchQueries = emptyList(),
                    sources = emptyList(),
                    isGrounded = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TutorResponse(
                text = "Error connecting to AI: ${e.message}",
                modelUsed = modelToUse,
                roleName = currentRole.displayName,
                webSearchQueries = emptyList(),
                sources = emptyList(),
                isGrounded = false
            )
        }
    }
}
