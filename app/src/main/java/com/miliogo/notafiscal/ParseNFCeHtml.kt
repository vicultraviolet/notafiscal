package com.miliogo.notafiscal

import kotlinx.serialization.json.*
import kotlinx.serialization.json.put
import org.jsoup.Jsoup

const val GET_EAN_CODE = true

fun parseNFCeHtml(html: String, url: String = ""): JsonObject
{
    val doc = Jsoup.parse(html)

    val table = doc.selectFirst("table")
    val tds = table?.select("td")

    val chaveAcesso = tds?.get(3)?.text()?.replace("[^0-9]".toRegex(), "")

    val fieldsets = doc.select("fieldset")

    val toggleBoxes = doc.select("table.toggle.box")
    val toggableBoxes = doc.select("table.toggable.box")

    return buildJsonObject {
        put("chave_acesso", chaveAcesso)
        put("url_consulta", url)

        for (fieldset in fieldsets)
        {
            val legend = fieldset.selectFirst("legend")?.text()
            val spans = fieldset.select("span")

            when (legend)
            {
                "Dados da NF-e" ->
                {
                    put("numero_cfe",        spans[2].text())
                    put("numero_serie_sat",  spans[1].text())
                    put("valor_total",       spans[5].text())

                    put("data_hora_emissao", spans[3]
                        .text()
                        .replace(" ", " - ")
                        .dropLast(6))
                }
                "Dados do Emitente" ->
                {
                    putJsonObject("emitente")
                    {
                        put("cnpj",      spans[2].text())
                        put("ie",        spans[10].text())
                        put("im",        spans[11].text())
                        put("nome",      spans[0].text())
                        put("fantasia",  spans[1].text())
                        put("bairro",    spans[4].text())
                        put("cep",       spans[5].text())

                        put("municipio", spans[6]
                            .text()
                            .substringAfter('-')
                            .trim())

                        put("endereco", spans[3]
                            .text()
                            .replace("\n", "")
                            .replace("\u00a0", "")
                            .replace("\\s+".toRegex(), " "))
                    }
                }
                "Dados do Destinatário" ->
                {
                    putJsonObject("consumidor")
                    {
                        put("cpf_consumidor",          spans[1].text())
                        put("razao_social_consumidor", spans[0].text())
                    }
                }
                "Totais" ->
                {
                    put("total_tributos", spans[spans.size-1].text())
                }
                else -> {}
            }
        }

        putJsonArray("items")
        {
            for (i in 0..<toggleBoxes.size)
            {
                val spans = toggleBoxes[i].select("span")
                val spans2 = toggableBoxes[i].select("span")

                if (spans[0].text().toIntOrNull() == null)
                    break

                addJsonObject {
                    put("seq",         spans[0].text())
                    put("descricao",   spans[1].text())
                    put("qtde",        spans[2].text())
                    put("un",          spans[3].text())
                    put("valor_total", spans[4].text())

                    if (GET_EAN_CODE)
                    {
                        val gtin = spans2[13].text()
                        if (gtin != "SEM GTIN")
                            put("codigo", gtin)
                        else
                            put("codigo", spans2[0].text())

                    } else
                        put("codigo", spans2[0].text())

                    put("valor_unit", spans2[19].text())

                    val desconto = spans2[9].text()
                    if (desconto.isEmpty())
                        put("desconto", "00,00")
                    else
                        put("desconto", desconto)

                    val tributos = spans2[23].text()
                    if (tributos == "\n")
                        put("tributos", "00,00")
                    else
                        put("tributos", tributos)
                }
            }
        }
    }
}

