package com.miliogo.notafiscal

import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.CookieManager
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.time.Duration.Companion.seconds

const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"

suspend fun downloadNFCe(urlString: String): String
{
    var connection: HttpURLConnection? = null
    var connection2: HttpURLConnection? = null

    val cookieManager = CookieManager()
    java.net.CookieHandler.setDefault(cookieManager)

    try
    {
        val url = URL(urlString)
        connection = url.openConnection() as HttpURLConnection

        connection.setRequestProperty("User-Agent", USER_AGENT)

        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val response = StringBuilder()
        val responseCode = connection.responseCode

        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null)
                response.append(line)

            reader.close()
        }

        val doc = Jsoup.parse(response.toString())

        val inputs = doc.select("input")

        val params = HashMap<String, String>()

        for (input in inputs)
        {
            val name = input.attr("name")
            var value = input.attr("value")

            if (name == "__EVENTTARGET")
            {
                value = "btnVisualizarAbas"
            }

            val names = listOf(
                "__EVENTTARGET",
                "__EVENTARGUMENT",
                "__VIEWSTATE",
                "__VIEWSTATEGENERATOR",
                "__EVENTVALIDATION"
            )

            if (names.contains(name))
                params[name] = value
        }

        val formData = params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }

        val url2 = URL("https://www.nfce.fazenda.sp.gov.br/NFCeConsultaPublica/Paginas/ConsultaResponsiva/ConsultaResumidaRJFrame_v400.aspx")
        connection2 = url2.openConnection() as HttpURLConnection

        connection2.doOutput = true

        connection2.setRequestProperty("User-Agent", USER_AGENT)
        connection2.setRequestProperty("Referer", urlString)

        connection2.requestMethod = "POST"
        connection2.connectTimeout = 10000
        connection2.readTimeout = 10000

        OutputStreamWriter(connection2.outputStream).use { writer ->
            writer.write(formData)
            writer.flush()
        }

        delay(1.seconds)

        val response2 = StringBuilder()
        val responseCode2 = connection2.responseCode

        if (responseCode2 == HttpURLConnection.HTTP_OK)
        {
            val reader = BufferedReader(InputStreamReader(connection2.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null)
                response2.append(line)

            reader.close()
        }

        return response2.toString()
    } catch (e: Exception)
    {
        e.printStackTrace()
    } finally {
        connection?.disconnect()
        connection2?.disconnect()
    }

    return ""
}
