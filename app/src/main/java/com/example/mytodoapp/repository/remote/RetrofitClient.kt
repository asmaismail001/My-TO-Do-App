package com.example.mytodoapp.repository.remote

import android.content.Context
import com.example.mytodoapp.model.*
import com.example.mytodoapp.util.SessionManager
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.UUID

object RetrofitClient {
    private const val BASE_URL = "https://api.mytodoapp.com/api/"

    private var retrofit: Retrofit? = null

    fun getClient(context: Context): ApiService {
        if (retrofit == null) {
            val sessionManager = SessionManager(context)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val token = sessionManager.getAuthToken()
                    
                    val requestBuilder = originalRequest.newBuilder()
                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                    chain.proceed(requestBuilder.build())
                }
                .addInterceptor(MockInterceptor(context))
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}

class MockInterceptor(private val context: Context) : Interceptor {
    private val gson = Gson()
    private val mockPrefs = context.getSharedPreferences("mock_backend_prefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        
        // Simulating artificial delay for realistic api calls
        try {
            Thread.sleep(1200)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val responseBuilder = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)

        return when {
            path.endsWith("signup") && request.method == "POST" -> {
                val bodyString = requestBodyToString(request.body)
                val signupReq = gson.fromJson(bodyString, SignupRequest::class.java)

                if (signupReq.name.isBlank()) {
                    return errorResponse(request, 400, "Name cannot be empty")
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(signupReq.email).matches()) {
                    return errorResponse(request, 400, "Invalid email format")
                }
                if (signupReq.password.length < 6) {
                    return errorResponse(request, 400, "Password must be at least 6 characters")
                }

                val existingUser = getMockUserByEmail(signupReq.email)
                if (existingUser != null) {
                    return errorResponse(request, 400, "Email is already registered")
                }

                val userId = UUID.randomUUID().toString()
                val token = "mock_jwt_token_${userId}"
                val profile = ProfileResponse(
                    id = userId,
                    name = signupReq.name,
                    email = signupReq.email,
                    phone = null,
                    profileImage = null,
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date())
                )

                saveMockUser(profile, signupReq.password)

                val authResponse = AuthResponse(
                    token = token,
                    userId = userId,
                    name = signupReq.name,
                    email = signupReq.email
                )

                responseBuilder
                    .code(200)
                    .message("OK")
                    .body(gson.toJson(authResponse).toResponseBody("application/json".toMediaType()))
                    .build()
            }

            path.endsWith("login") && request.method == "POST" -> {
                val bodyString = requestBodyToString(request.body)
                val loginReq = gson.fromJson(bodyString, LoginRequest::class.java)

                if (loginReq.email.isBlank()) {
                    return errorResponse(request, 400, "Email cannot be empty")
                }
                if (loginReq.password.isBlank()) {
                    return errorResponse(request, 400, "Password cannot be empty")
                }

                val user = getMockUserByEmail(loginReq.email)
                val savedPassword = getMockPassword(loginReq.email)

                if (user == null || savedPassword != loginReq.password) {
                    return errorResponse(request, 401, "Email or password is incorrect.")
                }

                val token = "mock_jwt_token_${user.id}"
                val authResponse = AuthResponse(
                    token = token,
                    userId = user.id,
                    name = user.name,
                    email = user.email
                )

                responseBuilder
                    .code(200)
                    .message("OK")
                    .body(gson.toJson(authResponse).toResponseBody("application/json".toMediaType()))
                    .build()
            }

            path.endsWith("profile") && request.method == "GET" -> {
                val authHeader = request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return errorResponse(request, 401, "Unauthorized")
                }
                val token = authHeader.removePrefix("Bearer ")
                val userId = token.removePrefix("mock_jwt_token_")
                
                val user = getMockUserById(userId)
                if (user == null) {
                    return errorResponse(request, 404, "User not found")
                }

                responseBuilder
                    .code(200)
                    .message("OK")
                    .body(gson.toJson(user).toResponseBody("application/json".toMediaType()))
                    .build()
            }

            path.endsWith("profile") && request.method == "PUT" -> {
                val authHeader = request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return errorResponse(request, 401, "Unauthorized")
                }
                val token = authHeader.removePrefix("Bearer ")
                val userId = token.removePrefix("mock_jwt_token_")

                val bodyString = requestBodyToString(request.body)
                val updateReq = gson.fromJson(bodyString, UpdateProfileRequest::class.java)

                val user = getMockUserById(userId) ?: return errorResponse(request, 404, "User not found")

                val updatedUser = user.copy(
                    name = updateReq.name,
                    phone = updateReq.phone,
                    profileImage = updateReq.profileImage
                )

                saveMockUser(updatedUser, getMockPassword(user.email) ?: "")

                responseBuilder
                    .code(200)
                    .message("OK")
                    .body(gson.toJson(updatedUser).toResponseBody("application/json".toMediaType()))
                    .build()
            }

            path.endsWith("logout") && request.method == "POST" -> {
                responseBuilder
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }

            else -> {
                errorResponse(request, 404, "Endpoint not found")
            }
        }
    }

    private fun requestBodyToString(requestBody: RequestBody?): String {
        if (requestBody == null) return ""
        val buffer = okio.Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }

    private fun errorResponse(request: Request, code: Int, message: String): Response {
        val json = "{\"error\": \"$message\"}"
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun getMockUserByEmail(email: String): ProfileResponse? {
        val userJson = mockPrefs.getString("user_email_$email", null) ?: return null
        return gson.fromJson(userJson, ProfileResponse::class.java)
    }

    private fun getMockUserById(id: String): ProfileResponse? {
        val userJson = mockPrefs.getString("user_id_$id", null) ?: return null
        return gson.fromJson(userJson, ProfileResponse::class.java)
    }

    private fun getMockPassword(email: String): String? {
        return mockPrefs.getString("user_pwd_$email", null)
    }

    private fun saveMockUser(user: ProfileResponse, password: String) {
        mockPrefs.edit().apply {
            putString("user_email_${user.email}", gson.toJson(user))
            putString("user_id_${user.id}", gson.toJson(user))
            putString("user_pwd_${user.email}", password)
            apply()
        }
    }
}
