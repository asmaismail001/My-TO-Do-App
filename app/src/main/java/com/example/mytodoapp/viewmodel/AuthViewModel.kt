package com.example.mytodoapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.model.LoginRequest
import com.example.mytodoapp.model.SignupRequest
import com.example.mytodoapp.repository.AuthRepository
import com.example.mytodoapp.repository.TodoRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val todoRepository: TodoRepository
) : ViewModel() {

    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    var signupName by mutableStateOf("")
    var signupEmail by mutableStateOf("")
    var signupPassword by mutableStateOf("")
    var signupConfirmPassword by mutableStateOf("")
    var signupProfileImageUri by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var isLoggedIn by mutableStateOf(authRepository.isLoggedIn())
        private set

    fun validateLogin(): Boolean {
        errorMessage = null
        if (loginEmail.isBlank()) {
            errorMessage = "Email cannot be empty."
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
            errorMessage = "Please enter a valid email address."
            return false
        }
        if (loginPassword.isBlank()) {
            errorMessage = "Password cannot be empty."
            return false
        }
        return true
    }

    fun validateSignup(): Boolean {
        errorMessage = null
        if (signupName.isBlank()) {
            errorMessage = "Name cannot be empty."
            return false
        }
        if (signupEmail.isBlank()) {
            errorMessage = "Email cannot be empty."
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(signupEmail).matches()) {
            errorMessage = "Please enter a valid email address."
            return false
        }
        if (signupPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return false
        }
        if (signupConfirmPassword != signupPassword) {
            errorMessage = "Passwords do not match."
            return false
        }
        return true
    }

    fun login(onSuccess: () -> Unit) {
        if (!validateLogin()) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = authRepository.login(LoginRequest(loginEmail.trim(), loginPassword))
            isLoading = false
            result.onSuccess {
                todoRepository.claimOrphanTasks(it.userId)
                isLoggedIn = true
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Email or password is incorrect."
            }
        }
    }

    fun signup(onSuccess: () -> Unit) {
        if (!validateSignup()) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = authRepository.signup(SignupRequest(signupName.trim(), signupEmail.trim(), signupPassword))
            isLoading = false
            result.onSuccess {
                todoRepository.claimOrphanTasks(it.userId)
                isLoggedIn = true
                successMessage = "Account created successfully!"
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Registration failed. Please try again."
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun resetState() {
        loginEmail = ""
        loginPassword = ""
        signupName = ""
        signupEmail = ""
        signupPassword = ""
        signupConfirmPassword = ""
        signupProfileImageUri = null
        errorMessage = null
        successMessage = null
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val todoRepository: TodoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, todoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
