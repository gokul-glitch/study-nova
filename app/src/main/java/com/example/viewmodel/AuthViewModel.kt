package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudyApplication
import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val uid: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userProfileRepo = (application as StudyApplication).profileRepository
    private val userPrefsRepo = (application as StudyApplication).userPreferencesRepository

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()

    val isFirebaseConfigured = false // Mocked since Firebase is removed

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            val userId: String? = userPrefsRepo.loggedInUserId.firstOrNull()
            if (userId != null) {
                _currentUserId.value = userId
                userProfileRepo.getUserProfile(userId).collect { profile ->
                    _userProfile.value = profile
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = userProfileRepo.getUserByEmail(email)
                if (user != null && user.passwordHash == password) { // Note: plaintext for simplicity in this local implementation
                    userPrefsRepo.saveLoggedInUserId(user.userId)
                    _currentUserId.value = user.userId
                    _uiState.value = AuthUiState.Success(user.userId)
                    userProfileRepo.getUserProfile(user.userId).collect { profile ->
                        _userProfile.value = profile
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String, college: String, course: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val existingUser = userProfileRepo.getUserByEmail(email)
                if (existingUser != null) {
                    _uiState.value = AuthUiState.Error("Account with this email already exists")
                    return@launch
                }
                val newUserId = UUID.randomUUID().toString()
                val profile = UserProfileEntity(
                    userId = newUserId,
                    name = name,
                    email = email,
                    passwordHash = password, // Note: plaintext for simplicity
                    college = college,
                    course = course
                )
                userProfileRepo.saveProfile(profile)
                
                userPrefsRepo.saveLoggedInUserId(newUserId)
                _currentUserId.value = newUserId
                _uiState.value = AuthUiState.Success(newUserId)
                _userProfile.value = profile
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun updateProfile(name: String, college: String, course: String, studyGoal: String, profilePictureUri: String?) {
        viewModelScope.launch {
            val currentProfile = _userProfile.value
            if (currentProfile != null) {
                val updatedProfile = currentProfile.copy(
                    name = name,
                    college = college,
                    course = course,
                    studyGoal = studyGoal,
                    profilePictureUri = profilePictureUri
                )
                userProfileRepo.saveProfile(updatedProfile)
                _userProfile.value = updatedProfile
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            val guestId = "guest_user"
            userPrefsRepo.saveLoggedInUserId(guestId)
            _currentUserId.value = guestId
            _uiState.value = AuthUiState.Success(guestId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPrefsRepo.clearLoggedInUserId()
            _currentUserId.value = null
            _userProfile.value = null
            _uiState.value = AuthUiState.Idle
        }
    }
    
    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
