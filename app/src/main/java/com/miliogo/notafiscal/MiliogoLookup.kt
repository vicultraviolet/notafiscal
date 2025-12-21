package com.miliogo.notafiscal

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
class Product(
    val codigo: String,
    val nome: String,
    val un: String,
    val valor: Float,
    val desconto: Float,
    val data: String,
    val emitente: String
)

suspend fun lookupMiliogoProducts(info: JsonObject): List<Product>
{
    val json = Json {
        encodeDefaults = false
    }

    val response = postToMiliogo(
        "cupom/consulta.php",
        info
    )

    return json.decodeFromString(response)
}
