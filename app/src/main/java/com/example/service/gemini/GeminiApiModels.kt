package com.example.service.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Gemini API Request & Response Data Transfer Objects for Retrofit
 */

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents")
    val contents: List<GeminiContent>,
    @Json(name = "systemInstruction")
    val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig")
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts")
    val parts: List<GeminiPart>,
    @Json(name = "role")
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text")
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature")
    val temperature: Float? = null,
    @Json(name = "topP")
    val topP: Float? = null,
    @Json(name = "topK")
    val topK: Int? = null,
    @Json(name = "maxOutputTokens")
    val maxOutputTokens: Int? = null,
    @Json(name = "responseMimeType")
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates")
    val candidates: List<GeminiCandidate>? = null,
    @Json(name = "promptFeedback")
    val promptFeedback: GeminiPromptFeedback? = null,
    @Json(name = "usageMetadata")
    val usageMetadata: GeminiUsageMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content")
    val content: GeminiContent? = null,
    @Json(name = "finishReason")
    val finishReason: String? = null,
    @Json(name = "index")
    val index: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPromptFeedback(
    @Json(name = "blockReason")
    val blockReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiUsageMetadata(
    @Json(name = "promptTokenCount")
    val promptTokenCount: Int? = null,
    @Json(name = "candidatesTokenCount")
    val candidatesTokenCount: Int? = null,
    @Json(name = "totalTokenCount")
    val totalTokenCount: Int? = null
)
