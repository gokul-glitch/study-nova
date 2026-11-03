package com.example.service

import com.example.BuildConfig
import com.example.data.local.QuizQuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class GeneratedQuizResult(
    val title: String,
    val subject: String,
    val difficulty: String,
    val questions: List<QuizQuestionEntity>
)

class QuizGeneratorService {

    suspend fun generateQuiz(
        subjectName: String,
        materialContent: String,
        numQuestions: Int,
        difficulty: String,
        questionType: String // "Multiple Choice", "True / False", "Mixed"
    ): GeneratedQuizResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("Gemini API key is missing or not configured in AI Studio Secrets.")
        }

        val safeText = if (materialContent.length > 40000) {
            materialContent.substring(0, 40000)
        } else materialContent

        val typeInstruction = when (questionType) {
            "True / False" -> "All questions must be True / False questions. Each must have options: [\"True\", \"False\"] with an unambiguous statement and truth value."
            "Mixed" -> "Generate an engaging mix of Multiple Choice questions (4 options) and True / False questions (options [\"True\", \"False\"])."
            else -> "All questions must be Multiple Choice Questions (MCQ) with exactly 4 options, exactly one correct answer, and plausible distractors."
        }

        val promptText = """
            You are Study Nova's Exam & Quiz Designer for the subject "$subjectName".
            Generate exactly $numQuestions questions with $difficulty difficulty based ONLY on the following study material.
            
            Format requirements:
            $typeInstruction
            - Questions must be derived strictly and exclusively from the provided text.
            - Never invent unsupported information or hallucinatory facts.
            - Avoid duplicate or ambiguous questions.
            - Do not reveal the answer inside the question prompt text.
            - Each question must include a clear, helpful explanation.
            - For MCQ: provide exactly 4 distinct options in the "options" array.
            - For True / False: provide options ["True", "False"].
            - "correctAnswer" MUST exactly match one of the items in the "options" array.
            - Return ONLY a valid JSON object with NO markdown formatting, NO backticks.
            
            JSON Schema:
            {
              "title": "$subjectName Quiz",
              "subject": "$subjectName",
              "difficulty": "$difficulty",
              "questions": [
                {
                  "question": "Question text here?",
                  "type": "${if (questionType == "True / False") "TF" else "MCQ"}",
                  "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
                  "correctAnswer": "Option 1",
                  "explanation": "Clear explanation of why this answer is correct."
                }
              ]
            }
            
            Study Material:
            $safeText
        """.trimIndent()

        // Using gemini-2.5-flash for fast and reliable structured JSON generation
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
        sysPartObj.put("text", "You are Study Nova's Advanced Academic Quiz Generator. You generate high-quality, rigorous exam quizzes from source materials in valid JSON format.")
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
            ?: throw Exception("No candidates returned from AI model.")

        if (candidates.length() == 0) {
            throw Exception("Empty response from AI model.")
        }

        var aiText = candidates.getJSONObject(0)
            .optJSONObject("content")
            ?.optJSONArray("parts")
            ?.getJSONObject(0)
            ?.optString("text", "") ?: ""

        aiText = aiText.replace("```json", "").replace("```", "").trim()

        parseGeneratedQuizJson(aiText, subjectName, difficulty)
    }

    private fun parseGeneratedQuizJson(
        jsonString: String,
        fallbackSubject: String,
        fallbackDifficulty: String
    ): GeneratedQuizResult {
        var quizTitle = "$fallbackSubject Quiz"
        var subject = fallbackSubject
        var difficulty = fallbackDifficulty
        val parsedQuestions = mutableListOf<QuizQuestionEntity>()

        var questionsArray: JSONArray? = null

        val trimmed = jsonString.trim()
        if (trimmed.startsWith("{")) {
            val rootObj = JSONObject(trimmed)
            quizTitle = rootObj.optString("title", "$fallbackSubject Quiz")
            subject = rootObj.optString("subject", fallbackSubject)
            difficulty = rootObj.optString("difficulty", fallbackDifficulty)

            if (rootObj.has("questions")) {
                questionsArray = rootObj.optJSONArray("questions")
            } else {
                // Check if any key contains an array of questions
                val keys = rootObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val arr = rootObj.optJSONArray(key)
                    if (arr != null && arr.length() > 0) {
                        questionsArray = arr
                        break
                    }
                }
            }
        } else if (trimmed.startsWith("[")) {
            questionsArray = JSONArray(trimmed)
        }

        if (questionsArray == null || questionsArray.length() == 0) {
            throw Exception("Could not find any quiz questions in the AI response.")
        }

        for (i in 0 until questionsArray.length()) {
            val qObj = questionsArray.optJSONObject(i) ?: continue
            val questionText = qObj.optString("question").trim()
            if (questionText.isBlank()) continue

            val type = qObj.optString("type", "MCQ")
            val explanation = qObj.optString("explanation", "Based on the provided study notes.")
            var rawCorrectAnswer = qObj.optString("correctAnswer", "").trim()

            // Parse options
            val optionsList = mutableListOf<String>()
            val optArray = qObj.optJSONArray("options")
            if (optArray != null) {
                for (j in 0 until optArray.length()) {
                    val opt = optArray.optString(j, "").trim()
                    if (opt.isNotBlank()) {
                        optionsList.add(opt)
                    }
                }
            } else {
                // Check for A, B, C, D keys
                listOf("A", "B", "C", "D").forEach { key ->
                    val opt = qObj.optString(key, "").trim()
                    if (opt.isNotBlank()) optionsList.add(opt)
                }
            }

            // Fallback for True/False if empty options
            if (type.equals("TF", ignoreCase = true) || type.contains("True", ignoreCase = true)) {
                if (optionsList.isEmpty()) {
                    optionsList.add("True")
                    optionsList.add("False")
                }
            }

            // Validate that options are available
            if (optionsList.size < 2) continue

            // Normalize correct answer
            var resolvedCorrectAnswer = rawCorrectAnswer
            val matchingDirect = optionsList.firstOrNull { it.equals(rawCorrectAnswer, ignoreCase = true) }
            if (matchingDirect != null) {
                resolvedCorrectAnswer = matchingDirect
            } else {
                // Maybe letter like "A", "B", "C", "D"
                val letterIndex = when (rawCorrectAnswer.uppercase()) {
                    "A" -> 0
                    "B" -> 1
                    "C" -> 2
                    "D" -> 3
                    else -> -1
                }
                if (letterIndex in 0 until optionsList.size) {
                    resolvedCorrectAnswer = optionsList[letterIndex]
                } else {
                    // Default to first option if unmatched
                    resolvedCorrectAnswer = optionsList[0]
                }
            }

            val optA = optionsList.getOrElse(0) { "" }
            val optB = optionsList.getOrElse(1) { "" }
            val optC = optionsList.getOrElse(2) { "" }
            val optD = optionsList.getOrElse(3) { "" }

            parsedQuestions.add(
                QuizQuestionEntity(
                    quizId = 0,
                    question = questionText,
                    questionType = if (type.contains("TF", ignoreCase = true) || optionsList.size == 2 && optionsList.contains("True")) "TF" else "MCQ",
                    optionA = optA,
                    optionB = optB,
                    optionC = optC,
                    optionD = optD,
                    correctAnswer = resolvedCorrectAnswer,
                    explanation = explanation,
                    studentAnswer = null,
                    isCorrect = false
                )
            )
        }

        if (parsedQuestions.isEmpty()) {
            throw Exception("Failed to parse valid quiz questions from the study material.")
        }

        return GeneratedQuizResult(
            title = quizTitle,
            subject = subject,
            difficulty = difficulty,
            questions = parsedQuestions
        )
    }
}
