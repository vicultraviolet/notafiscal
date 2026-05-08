package com.miliogo.nfce

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
data class Product(
    val codigo: String,
    val nome: String,
    val un: String,
    val valor: Float,
    val desconto: Float,
    val data: String,
    val emitente: String
)

data class MiliogoLookupResult(
    val data: List<Product>,
    val message: String
)

suspend fun lookupMiliogoProducts(info: JsonObject): MiliogoLookupResult {
    val json = Json {
        encodeDefaults = false
    }

    val response = postToMiliogo(
        "cupom/consulta.php",
        info
    )
    if (response.code != 200) {
        val message =
            if (response.code != 0)
                "Falha em conectar ao Miliogo! (Código ${response.code})"
            else
                NO_INTERNET_MESSAGE

        return MiliogoLookupResult(emptyList(), message)
    }

    return MiliogoLookupResult(json.decodeFromString(response.data), "")
}
