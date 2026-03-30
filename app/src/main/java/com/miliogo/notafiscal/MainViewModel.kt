package com.miliogo.notafiscal

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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

    init {
        viewModelScope.launch {
            dataStoreManager.secretKey.collect { savedInput ->
                _secretKey.value = savedInput
            }
        }
    }

    fun updateSecretKey(newSecretKey: String) {
        _secretKey.value = newSecretKey

        viewModelScope.launch {
            dataStoreManager.saveUserInput(newSecretKey)
        }
    }

    fun processNFCe(
        urlString: String,
        onResult: suspend CoroutineScope.(message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if ("www.nfce.fazenda.sp.gov.br" !in urlString)
                return@launch onResult("QR Code inválido: Não contém o URL www.nfce.fazenda.sp.gov.br")

            if (urlString.length < 80)
                return@launch onResult("QR Code inválido: URL não atingiu o mínimo de caractéres de um cupom!")

            if (urlString.count { it.isDigit() } < 44)
                return@launch onResult("QR Code inválido: URL não contém uma chave de acesso NFC-e!")

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
        onResult: suspend CoroutineScope.(products: List<Product>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val products = lookupMiliogoProducts(info)

            onResult(products)
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

