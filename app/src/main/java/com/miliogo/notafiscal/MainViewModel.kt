package com.miliogo.notafiscal

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
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.HttpURLConnection

enum class MiliogoUserAction {
    LOGIN, CREATE
}

class MainViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val _secretKey = MutableStateFlow("")
    val secretKey: StateFlow<String> = _secretKey.asStateFlow()

    init {
        // Load saved data when ViewModel is created
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
        onResult: suspend CoroutineScope.(miliogoResponse: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val html = downloadNFCe(urlString)
            val json = parseNFCeHtml(html, urlString)
            val response = postToMiliogo(
                "cupom/import_json.php",
                json,
                secretKey.value
            )

            onResult(response.data)
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
            val responseJson = Json.decodeFromString<Map<String, JsonElement>>(response.data)
            if (response.code == HttpURLConnection.HTTP_OK) {
                val secretKey = responseJson["secret"]?.jsonPrimitive?.content
                if (secretKey != null) {
                    onResult(secretKey)
                }
            } else {
                val error = responseJson["erro"]?.jsonPrimitive?.content
                if (error != null) {
                    onError(error)
                }
            }
        }
    }
}

