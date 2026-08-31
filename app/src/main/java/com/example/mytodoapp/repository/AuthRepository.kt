package com.example.mytodoapp.repository

import android.content.Context
import com.example.mytodoapp.model.*
import com.example.mytodoapp.repository.remote.RetrofitClient
import com.example.mytodoapp.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    private val apiService = RetrofitClient.getClient(context)
    private val sessionManager = SessionManager(context)

    suspend fun signup(request: SignupRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.signup(request)
            sessionManager.saveAuthToken(response.token)
            sessionManager.saveUserSession(response.userId, response.name, response.email)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(request)
            sessionManager.saveAuthToken(response.token)
            sessionManager.saveUserSession(response.userId, response.name, response.email)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
            sessionManager.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            sessionManager.logout()
            Result.success(Unit)
        }
    }

    fun getSessionToken(): String? = sessionManager.getAuthToken()
    fun getUserId(): String? = sessionManager.getUserId()
    fun getUserName(): String? = sessionManager.getUserName()
    fun getUserEmail(): String? = sessionManager.getUserEmail()
    
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
