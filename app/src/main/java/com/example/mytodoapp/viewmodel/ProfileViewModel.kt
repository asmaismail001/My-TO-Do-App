package com.example.mytodoapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.model.*
import com.example.mytodoapp.repository.AuthRepository
import com.example.mytodoapp.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var profileData by mutableStateOf<ProfileResponse?>(null)
        private set

    var editName by mutableStateOf("")
    var editPhone by mutableStateOf("")
    var editProfileImageUri by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun initializeEditFields() {
        profileData?.let {
            editName = it.name
            editPhone = it.phone ?: ""
            editProfileImageUri = it.profileImage
        }
    }

    fun loadProfile() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = profileRepository.getProfile()
            isLoading = false
            result.onSuccess {
                profileData = it
            }.onFailure {
                errorMessage = "Unable to load profile."
            }
        }
    }

    fun updateProfile(onSuccess: () -> Unit) {
        if (editName.isBlank()) {
            errorMessage = "Name cannot be empty."
            return
        }
        isSaving = true
        errorMessage = null
        successMessage = null
        viewModelScope.launch {
            val request = UpdateProfileRequest(
                name = editName.trim(),
                phone = editPhone.trim().ifEmpty { null },
                profileImage = editProfileImageUri
            )
            val result = profileRepository.updateProfile(request)
            isSaving = false
            result.onSuccess {
                profileData = it
                successMessage = "Profile updated successfully!"
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Failed to update profile."
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        errorMessage = null
        viewModelScope.launch {
            val result = authRepository.logout()
            result.onSuccess {
                profileData = null
                onSuccess()
            }.onFailure {
                profileData = null
                onSuccess()
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
