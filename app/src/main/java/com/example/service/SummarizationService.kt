package com.example.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SummarizationService {
    suspend fun generateSummary(text: String, subjectName: String?): String = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "Error: Gemini API key is missing or not configured."
            }

            // Ensure we don't blow up the prompt size
            val safeText = if (text.length > 40000) text.substring(0, 40000) else text
            
            val contextNote = if (safeText.length < text.length) {
                "(Note: The document is very large. Processing the first part safely to avoid limits.)"
            } else ""

            val subjectContext = if (subjectName != null && subjectName.isNotBlank()) {
                "The subject of this material is: $subjectName."
            } else {
                "The material covers general topics."
            }

            val promptText = """
                Please summarize the following study material.
                $subjectContext
                $contextNote
                
                Generate a student-friendly summary organizing the result using:
                - Topic Title
                - Short Overview
                - Important Concepts
                - Key Definitions
                - Important Points
                - Examples (if useful)
                - Formulas (if applicable)
                - Exam-focused Points
                - Quick Revision Section
                
                Do NOT force every section when it isn't relevant to the material.
                Avoid unnecessary conversational text. 
                Keep it easy for a college student to study.

                Material Context:
                $safeText
            """.trimIndent()

            // Using standard v1beta Gemini API model: gemini-2.5-flash for academic summarization
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 60000
            connection.readTimeout = 60000
            connection.doOutput = true

            val requestBody = JSONObject()

            // System instruction to give summarization assistant specific role
            val systemInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", "You are Study Nova's expert academic summarizer. Generate structured, student-friendly, exam-ready summaries from study notes and textbooks. Categorize concepts clearly with headings, key definitions, formulas, and high-yield exam takeaways.")
            sysPartsArray.put(sysPartObj)
            systemInstructionObj.put("parts", sysPartsArray)
            requestBody.put("systemInstruction", systemInstructionObj)

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", promptText)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBody.put("contents", contentsArray)

            connection.outputStream.use { os ->
                val input = requestBody.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            if (connection.responseCode == 200) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                    }
                }
                return@withContext "Could not generate an answer."
            } else {
                val errorDetails = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No details"
                } catch (e: Exception) {
                    "Could not read error stream: ${e.message}"
                }
                return@withContext "API Error: ${connection.responseCode}. Failed to generate summary. Details: $errorDetails"
            }
        } catch (e: Exception) {
            return@withContext "Error connecting to AI: ${e.message}"
        }
    }
}
