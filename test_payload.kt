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
    
    val contentObj = JSONObject()
    contentObj.put("role", "user")
    val partsArray = org.json.JSONArray()
    val partObj = JSONObject()
    partObj.put("text", "Hello this is a test.")
    partsArray.put(partObj)
    contentObj.put("parts", partsArray)
    contentsArray.put(contentObj)

    val contentObj2 = JSONObject()
    contentObj2.put("role", "model")
    val partsArray2 = org.json.JSONArray()
    val partObj2 = JSONObject()
    partObj2.put("text", "Hi! I am a model.")
    partsArray2.put(partObj2)
    contentObj2.put("parts", partsArray2)
    contentsArray.put(contentObj2)

    val contentObj3 = JSONObject()
    contentObj3.put("role", "user")
    val partsArray3 = org.json.JSONArray()
    val partObj3 = JSONObject()
    partObj3.put("text", "Test question.")
    partsArray3.put(partObj3)
    contentObj3.put("parts", partsArray3)
    contentsArray.put(contentObj3)
    
    requestBody.put("contents", contentsArray)

    println(requestBody.toString())

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
