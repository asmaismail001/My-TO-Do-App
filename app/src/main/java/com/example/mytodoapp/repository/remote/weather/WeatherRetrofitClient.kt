package com.example.mytodoapp.repository.remote.weather

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WeatherRetrofitClient {

    private var apiService: WeatherApiService? = null

    fun getService(): WeatherApiService {
        if (apiService == null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(WeatherApiConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(WeatherApiService::class.java)
        }
        return apiService!!
    }
}
