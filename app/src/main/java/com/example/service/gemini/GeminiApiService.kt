package com.example.service.gemini

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit Interface for communicating with Google Gemini API endpoints.
 */
interface GeminiApiService {

    /**
     * Standard generateContent endpoint
     * e.g. POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
     */
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
