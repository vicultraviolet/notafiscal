package com.miliogo.nfce

import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.time.Duration.Companion.seconds

const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"

private fun fillConnection(connection: HttpURLConnection) {
    connection.connectTimeout = 15000
    connection.readTimeout = 15000

    connection.setRequestProperty("User-Agent", USER_AGENT)
    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    connection.setRequestProperty("Accept-Language", "pt-BR,pt;q=0.9,en;q=0.5")
}

suspend fun downloadNFCe(urlString: String): String {
    var connection: HttpURLConnection? = null
    var connection2: HttpURLConnection? = null

    val cookieManager = CookieManager()
    CookieHandler.setDefault(cookieManager)

    try {
        val url = URL(urlString)
        connection = url.openConnection() as HttpURLConnection

        fillConnection(connection)
        connection.requestMethod = "GET"

        val response = StringBuilder()
        val responseCode = connection.responseCode

        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null)
                response.append(line)

            reader.close()
        }

        val doc = Jsoup.parse(response.toString())

        val inputs = doc.select("input")

        val params = HashMap<String, String>()

        for (input in inputs) {
            val name = input.attr("name")
            var value = input.attr("value")

            if (name == "__EVENTTARGET")
                value = "btnVisualizarAbas"

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

        fillConnection(connection2)
        connection2.doOutput = true
        connection2.setRequestProperty("Referer", urlString)
        connection2.requestMethod = "POST"

        OutputStreamWriter(connection2.outputStream).use { writer ->
            writer.write(formData)
            writer.flush()
        }

        delay(1.seconds)

        val response2 = StringBuilder()
        val responseCode2 = connection2.responseCode

        if (responseCode2 == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection2.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null)
                response2.append(line)

            reader.close()
        }

        return response2.toString()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        connection?.disconnect()
        connection2?.disconnect()
    }

    return ""
}
