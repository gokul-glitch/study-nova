package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyApplication
import com.example.data.local.StudyMaterialEntity
import com.example.service.DocumentProcessor
import com.example.service.SummarizationService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SummaryUiState {
    object Loading : SummaryUiState()
    data class Success(val summary: String, val materialTitle: String, val subjectName: String?) : SummaryUiState()
    data class Error(val message: String) : SummaryUiState()
}

class SummaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as StudyApplication).repository
    private val studyActivityRepository = (application as StudyApplication).studyActivityRepository
    private val documentProcessor = DocumentProcessor()
    private val summarizationService = SummarizationService()
    
    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.Loading)
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()
    
    private var _currentUserId = "local_user"
    
    fun setUserId(userId: String) {
        _currentUserId = userId
    }
    
    fun loadMaterial(materialId: Long, forceRegenerate: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = SummaryUiState.Loading
            val material = repository.getMaterialByIdAndUser(materialId, _currentUserId).firstOrNull()
            if (material != null) {
                var subjectName: String? = null
                if (material.subjectId != null) {
                    val subjectRepo = getApplication<StudyApplication>().subjectRepository
                    val subjects = subjectRepo.getSubjectsForUser(_currentUserId).firstOrNull()
                    subjectName = subjects?.find { it.id == material.subjectId }?.subjectName
                }

                if (material.summary != null && !forceRegenerate) {
                    _uiState.value = SummaryUiState.Success(
                        summary = material.summary,
                        materialTitle = material.title,
                        subjectName = subjectName
                    )
                } else {
                    generateSummary(material, subjectName)
                }
            } else {
                _uiState.value = SummaryUiState.Error("Material not found.")
            }
        }
    }
    
    private suspend fun generateSummary(material: StudyMaterialEntity, subjectName: String?) {
        _uiState.value = SummaryUiState.Loading
        try {
            val extractedText = documentProcessor.extractText(
                getApplication(),
                material.fileUri,
                material.fileType
            )
            
            // Artificial truncation for very large files if needed
            val safeText = if (extractedText.length > 50000) extractedText.substring(0, 50000) else extractedText
            
            val updatedMat = material.copy(textContent = safeText)
            
            val summary = summarizationService.generateSummary(updatedMat.textContent ?: "No text content available", subjectName)
            
            if (summary.startsWith("Error", ignoreCase = true) || summary.startsWith("API Error", ignoreCase = true) || summary.startsWith("Could not", ignoreCase = true)) {
                _uiState.value = SummaryUiState.Error(summary)
            } else {
                // Cache the summary in DB
                val updatedMaterial = material.copy(textContent = safeText, summary = summary)
                repository.insert(updatedMaterial)
                studyActivityRepository.recordActivity(_currentUserId, "GENERATE_SUMMARY", material.subjectId, material.id)
                
                _uiState.value = SummaryUiState.Success(
                    summary = summary,
                    materialTitle = material.title,
                    subjectName = subjectName
                )
            }
        } catch (e: SecurityException) {
            _uiState.value = SummaryUiState.Error("Action halted: " + e.message)
        } catch (e: Exception) {
            _uiState.value = SummaryUiState.Error("Failed to summarize document: ${e.message}")
        }
    }
}
