package com.miliogo.nfce

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miliogo.nfce.ui.theme.CurrentTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.HttpURLConnection

enum class MiliogoUserAction {
    LOGIN, CREATE
}

fun String.isValidURL(): Boolean = Patterns.WEB_URL.matcher(this).matches()

class MainViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val _secretKey = MutableStateFlow("")
    val secretKey: StateFlow<String> = _secretKey.asStateFlow()

    private val _theme = MutableStateFlow(CurrentTheme.Default)
    val theme: StateFlow<CurrentTheme> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.theme.collect { theme ->
                _theme.value = theme
            }
        }

        viewModelScope.launch {
            dataStoreManager.secretKey.collect { savedInput ->
                _secretKey.value = savedInput
            }
        }
    }

    fun updateSecretKey(newSecretKey: String) {
        _secretKey.value = newSecretKey

        viewModelScope.launch {
            dataStoreManager.saveSecretKey(newSecretKey)
        }
    }

    fun updateCurrentTheme(newTheme: CurrentTheme) {
        _theme.value = newTheme

        viewModelScope.launch {
            dataStoreManager.saveTheme(newTheme)
        }
    }

    fun processNFCe(
        urlString: String,
        onResult: suspend CoroutineScope.(message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // url validation
            run {
                val response = postToMiliogo(
                    "cupom/url.php",
                    buildJsonObject {
                        put("url", urlString)
                    },
                    secretKey.value
                )

                if (response.code == 0)
                    return@launch onResult(NO_INTERNET_MESSAGE)

                val parsedResponse = Json.parseToJsonElement(response.data).jsonObject

                if (parsedResponse["sucesso"]?.jsonPrimitive?.boolean != true) {
                    val error = parsedResponse["erro"]?.jsonPrimitive?.content.toString()
                    val details = parsedResponse["detalhes"]?.jsonPrimitive?.content.toString()

                    return@launch onResult("${response.code}: $error: $details")
                }
            }

            val html = downloadNFCe(urlString)
            val json = parseNFCeHtml(html, urlString)

            if (!validateNFCeJson(json)) {
                if (urlString.length <= 180)
                    return@launch onResult("Falha ao processar dados do cupom!")

                return@launch onResult("Falha ao processor dados do cupom. Tente novamente mais tarde!")
            }

            val response = postToMiliogo(
                "cupom/import_json.php",
                json,
                secretKey.value
            )
            val parsedResponse = Json.parseToJsonElement(response.data).jsonObject

            val message = parsedResponse["mensagem"]?.jsonPrimitive?.content.toString()

            onResult(message)
        }
    }

    fun lookupMiliogoProducts(
        info: JsonObject,
        onResult: suspend CoroutineScope.(result: MiliogoLookupResult) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = lookupMiliogoProducts(info)
            onResult(result)
        }
    }

    fun miliogoUser(
        action: MiliogoUserAction,
        username: String,
        password: String,
        onError: suspend CoroutineScope.(error: String) -> Unit,
        onResult: suspend CoroutineScope.(secretKey: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = postToMiliogo(
                "cupom/usuario.php",
                buildJsonObject {
                    put("nome", username)
                    put("senha", password)
                    when (action) {
                        MiliogoUserAction.LOGIN -> put("funcao", "login")
                        MiliogoUserAction.CREATE -> put("funcao", "criar")
                    }
                },
                null
            )

            if (response.code == 0)
                return@launch onError(NO_INTERNET_MESSAGE)

            val parsedResponse = Json.parseToJsonElement(response.data).jsonObject

            if (response.code == HttpURLConnection.HTTP_OK) {
                val secretKey = parsedResponse["secret"]?.jsonPrimitive?.content
                if (secretKey != null) {
                    onResult(secretKey)
                }
            } else {
                val error = parsedResponse["erro"]?.jsonPrimitive?.content
                if (error != null) {
                    onError(error)
                }
            }
        }
    }
}

