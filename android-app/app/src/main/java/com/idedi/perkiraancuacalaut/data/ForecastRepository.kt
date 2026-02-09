package com.idedi.perkiraancuacalaut.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class ForecastRepository {

    private val baseUrl = "http://192.168.1.9:5055" // gunakan 10.0.2.2 agar emulator bisa akses localhost

    suspend fun getForecast(region: String): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/predict")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val jsonBody = JSONObject()
                jsonBody.put("region", region)

                val output: OutputStream = connection.outputStream
                output.write(jsonBody.toString().toByteArray())
                output.flush()
                output.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    reader.forEachLine { response.append(it) }
                    reader.close()
                    JSONObject(response.toString())
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
