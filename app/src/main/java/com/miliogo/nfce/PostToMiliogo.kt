package com.miliogo.notafiscal

import kotlinx.serialization.json.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class MiliogoPostResponse(val data: String, val code: Int)

suspend fun postToMiliogo(
    phpScript: String,
    json: JsonObject,
    secretKey: String? = null
): MiliogoPostResponse {
    var connection: HttpURLConnection? = null
    var response = MiliogoPostResponse("", 0)

    try {
        val url = URL("https://miliogo.com/$phpScript")
        connection = url.openConnection() as HttpURLConnection

        connection.doOutput = true

        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("User-Agent", "miliogo-cupom-android-client/1.0")
        if (secretKey != null)
            connection.setRequestProperty("secret-key", secretKey)

        connection.requestMethod = "POST"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        connection.outputStream.use { writer ->
            val jsonString = json.toString()
            val data = jsonString.toByteArray(StandardCharsets.UTF_8)

            writer.write(data)
            writer.flush()
        }

        val responseBuilder = StringBuilder()
        val responseCode = connection.responseCode

        val stream = if (responseCode >= 400) {
            connection.errorStream
        } else {
            connection.inputStream
        }

        val reader = BufferedReader(InputStreamReader(stream))
        var line: String?

        while (reader.readLine().also { line = it } != null)
            responseBuilder.append(line)

        reader.close()

        response = MiliogoPostResponse(responseBuilder.toString(), responseCode)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        connection?.disconnect()
    }

    return response
}
