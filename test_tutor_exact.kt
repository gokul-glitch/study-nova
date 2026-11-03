import java.net.URL
import java.net.HttpURLConnection
import org.json.JSONObject
import org.json.JSONArray
import java.nio.charset.Charsets

fun main() {
    val apiKey = System.getenv("GEMINI_API_KEY") ?: ""
    val subjectName = "General"
    val materialText = "Some material"
    val conversationBuilder = java.lang.StringBuilder()
    conversationBuilder.append("You are a friendly, helpful, and expert study tutor for the subject: $subjectName.\n")
    conversationBuilder.append("Student: Hello!\n")
    conversationBuilder.append("Tutor: ")

    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true
    
    val requestBody = JSONObject()
    val contentsArray = JSONArray()
    val contentObj = JSONObject()
    val partsArray = JSONArray()
    val partObj = JSONObject()
    
    partObj.put("text", conversationBuilder.toString())
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
        println("Success: $responseString")
    } else {
        val errorDetails = try {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No details"
        } catch (e: Exception) {
            "Could not read error stream"
        }
        println("API Error: ${connection.responseCode} - $errorDetails")
    }
}
