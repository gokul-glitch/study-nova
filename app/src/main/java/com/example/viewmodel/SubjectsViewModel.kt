package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyApplication
import com.example.data.local.SubjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectsViewModel(application: Application) : AndroidViewModel(application) {
    private val subjectRepository = (application as StudyApplication).subjectRepository
    
    private val _currentUserId = MutableStateFlow<String>("guest_user")

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<SubjectEntity>> = _currentUserId
        .flatMapLatest { userId -> subjectRepository.getSubjectsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String, description: String? = null) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isNotBlank() && name.isNotBlank()) {
                val newSubject = SubjectEntity(
                    userId = userId,
                    subjectName = name,
                    description = description?.takeIf { it.isNotBlank() },
                    createdAt = System.currentTimeMillis()
                )
                subjectRepository.insertSubject(newSubject)
            }
        }
    }

    fun editSubject(subject: SubjectEntity, newName: String, newDescription: String?) {
        viewModelScope.launch {
            if (newName.isNotBlank()) {
                val updatedSubject = subject.copy(
                    subjectName = newName,
                    description = newDescription?.takeIf { it.isNotBlank() }
                )
                subjectRepository.insertSubject(updatedSubject)
            }
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            subjectRepository.deleteSubject(subject)
        }
    }
}
