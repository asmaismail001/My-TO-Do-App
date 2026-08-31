package com.example.mytodoapp.model

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val name: String,
    val email: String
)

data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val profileImage: String? = null,
    val createdAt: String
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String? = null,
    val profileImage: String? = null
)
