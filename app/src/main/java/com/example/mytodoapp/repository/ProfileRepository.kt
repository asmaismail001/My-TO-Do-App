package com.example.mytodoapp.repository

import android.content.Context
import com.example.mytodoapp.model.*
import com.example.mytodoapp.repository.remote.RetrofitClient
import com.example.mytodoapp.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val context: Context) {
    private val apiService = RetrofitClient.getClient(context)
    private val sessionManager = SessionManager(context)

    suspend fun getProfile(): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()
            sessionManager.setUserName(response.name)
            sessionManager.setUserPhone(response.phone)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProfile(request)
            sessionManager.setUserName(response.name)
            sessionManager.setUserPhone(response.phone)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
