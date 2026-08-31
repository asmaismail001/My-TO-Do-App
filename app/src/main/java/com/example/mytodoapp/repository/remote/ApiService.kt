package com.example.mytodoapp.repository.remote

import com.example.mytodoapp.model.*
import retrofit2.http.*

interface ApiService {

    @POST("signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse

    @POST("logout")
    suspend fun logout(): Void?
}
