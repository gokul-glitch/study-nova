package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyApplication
import com.example.data.local.StudyMaterialEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyMaterialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as StudyApplication).repository
    private val studyActivityRepository = (application as StudyApplication).studyActivityRepository
    
    private val _currentUserId = MutableStateFlow<String>("local_user")
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filter: 0 = All, 1 = Notes (text/doc), 2 = PDFs
    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId = _selectedSubjectId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val materials: StateFlow<List<StudyMaterialEntity>> = _currentUserId
        .flatMapLatest { userId ->
            combine(
                repository.getAllMaterialsForUser(userId),
                _searchQuery,
                _selectedFilter,
                _selectedSubjectId
            ) { list, query, filter, subjectId ->
                list.filter { material ->
                    val matchesQuery = material.title.contains(query, ignoreCase = true) || material.fileName.contains(query, ignoreCase = true)
                    val matchesFilter = when (filter) {
                        1 -> !material.fileType.contains("pdf", ignoreCase = true)
                        2 -> material.fileType.contains("pdf", ignoreCase = true)
                        else -> true
                    }
                    val matchesSubject = if (subjectId != null) material.subjectId == subjectId else true
                    matchesQuery && matchesFilter && matchesSubject
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allMaterialsUnfiltered: StateFlow<List<StudyMaterialEntity>> = _currentUserId
        .flatMapLatest { userId -> repository.getAllMaterialsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filterIndex: Int) {
        _selectedFilter.value = filterIndex
    }

    fun setSubjectFilter(subjectId: Long?) {
        _selectedSubjectId.value = subjectId
    }

    fun importDocument(uri: Uri, subjectId: Long? = null) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                var fileName = "Unknown File"
                var fileSize = 0L
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                val title = if (fileName.contains(".")) fileName.substringBeforeLast(".") else fileName

                val entity = StudyMaterialEntity(
                    userId = _currentUserId.value,
                    subjectId = subjectId,
                    title = title,
                    fileName = fileName,
                    fileUri = uri.toString(),
                    fileType = mimeType,
                    createdAt = System.currentTimeMillis(),
                    fileSize = fileSize
                )
                repository.insert(entity)
                studyActivityRepository.recordActivity(_currentUserId.value, "UPLOAD_MATERIAL", subjectId, null)

                Toast.makeText(context, "Imported $fileName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication<Application>().applicationContext, "Failed to import file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteMaterial(material: StudyMaterialEntity) {
        viewModelScope.launch {
            try {
                repository.delete(material)
            } catch (e: Exception) {
                Toast.makeText(getApplication<Application>().applicationContext, "Failed to delete file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
