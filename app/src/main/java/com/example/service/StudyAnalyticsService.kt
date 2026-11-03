package com.example.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AIStudyAnalysis(
    val strengths: List<String>,
    val areasToImprove: List<String>,
    val recommendedAction: String,
    val generatedAt: Long = System.currentTimeMillis()
)

class StudyAnalyticsService {

    suspend fun generateAnalysis(
        overallAccuracy: Int,
        totalQuizzes: Int,
        totalQuestions: Int,
        streakDays: Int,
        subjectsInfo: String,
        trendInfo: String
    ): AIStudyAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("Gemini API key is not configured.")
        }

        val promptText = """
            You are Study Nova's Academic Performance Coach.
            Analyze the following verified study metrics for a student:
            
            - Overall Quiz Accuracy: $overallAccuracy%
            - Total Quizzes Completed: $totalQuizzes
            - Total Questions Answered: $totalQuestions
            - Study Streak: $streakDays days
            
            Subject Performance:
            $subjectsInfo
            
            Recent Quiz Trend:
            $trendInfo
            
            Provide a constructive, highly personalized study performance analysis.
            Format output STRICTLY as valid JSON with NO markdown formatting, NO backticks.
            
            JSON Schema:
            {
              "strengths": [
                "1-2 concise bullet points highlighting topics/habits where the student excels"
              ],
              "areasToImprove": [
                "1-2 actionable points identifying weaker topics, lower accuracy areas, or consistency gaps"
              ],
              "recommendedAction": "One specific, practical next study action (e.g., generate a quiz on subject X, review flashcards, or ask AI Tutor)."
            }
            
            Rules:
            - Analyze ONLY the verified data provided above.
            - Do NOT invent fictional subjects or fake scores.
            - Keep tone encouraging, academic, and sharp.
        """.trimIndent()

        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 60000
            readTimeout = 60000
            doOutput = true
        }

        val requestBody = JSONObject()

        val systemInstructionObj = JSONObject()
        val sysPartsArray = JSONArray()
        val sysPartObj = JSONObject()
        sysPartObj.put("text", "You are Study Nova's Academic Performance Coach. You analyze actual student metrics to generate structured, realistic study insights in valid JSON format.")
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

        if (connection.responseCode != 200) {
            val errorDetails = try {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No details"
            } catch (e: Exception) {
                "Could not read error stream"
            }
            throw Exception("API Error ${connection.responseCode}: $errorDetails")
        }

        val responseString = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonResponse = JSONObject(responseString)
        val candidates = jsonResponse.optJSONArray("candidates")
            ?: throw Exception("No response received from AI.")

        if (candidates.length() == 0) {
            throw Exception("Empty response from AI.")
        }

        var aiText = candidates.getJSONObject(0)
            .optJSONObject("content")
            ?.optJSONArray("parts")
            ?.getJSONObject(0)
            ?.optString("text", "") ?: ""

        aiText = aiText.replace("```json", "").replace("```", "").trim()

        val parsedObj = JSONObject(aiText)
        val strengthsList = mutableListOf<String>()
        val strengthsArray = parsedObj.optJSONArray("strengths")
        if (strengthsArray != null) {
            for (i in 0 until strengthsArray.length()) {
                val item = strengthsArray.optString(i, "").trim()
                if (item.isNotBlank()) strengthsList.add(item)
            }
        }

        val improvementsList = mutableListOf<String>()
        val improvementsArray = parsedObj.optJSONArray("areasToImprove")
        if (improvementsArray != null) {
            for (i in 0 until improvementsArray.length()) {
                val item = improvementsArray.optString(i, "").trim()
                if (item.isNotBlank()) improvementsList.add(item)
            }
        }

        val action = parsedObj.optString(
            "recommendedAction",
            "Review your recent notes and take a quick practice quiz to consolidate your knowledge."
        )

        AIStudyAnalysis(
            strengths = if (strengthsList.isNotEmpty()) strengthsList else listOf("Consistent study habit with dedicated learning focus."),
            areasToImprove = if (improvementsList.isNotEmpty()) improvementsList else listOf("Continue expanding quiz practice across more subjects."),
            recommendedAction = action
        )
    }
}
