import java.net.URL
import java.net.HttpURLConnection
import org.json.JSONObject

fun main() {
    val apiKey = System.getenv("GEMINI_API_KEY") ?: "NO_KEY"
    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true

    val requestBody = JSONObject()
    val contentsArray = org.json.JSONArray()
    
    // Previous messages
    val msgs = listOf("Hello", "How are you?")
    
    val contentObj = JSONObject()
    val partsArray = org.json.JSONArray()
    val partObj = JSONObject()
    partObj.put("text", "Hello")
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
        val errorString = connection.errorStream?.bufferedReader()?.use { it.readText() }
        println("Error ${connection.responseCode}: $errorString")
    }
}
