package com.miliogo.notafiscal

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.time.Duration.Companion.seconds


suspend fun postToMiliogo(json: JsonObject, secretKey: String): String
{
    var connection: HttpURLConnection? = null
    var response = ""

    try
    {
        val url = URL("https://miliogo.com/cupom/import_json.php")
        connection = url.openConnection() as HttpURLConnection

        connection.doOutput = true

        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("User-Agent", "miliogo-cupom-android-client/1.0")
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

        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null)
                responseBuilder.append(line)

            reader.close()
        }

        response = responseBuilder.toString()
    } catch (e: Exception)
    {
        e.printStackTrace()
    } finally {
        connection?.disconnect()
    }

    return response
}
