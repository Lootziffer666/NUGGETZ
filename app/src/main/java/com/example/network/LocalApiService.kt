package com.example.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface LocalApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Body request: ChatRequest
    ): ChatResponse
}
