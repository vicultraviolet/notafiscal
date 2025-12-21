package com.miliogo.notafiscal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

            onResult(response)
        }
    }
}

