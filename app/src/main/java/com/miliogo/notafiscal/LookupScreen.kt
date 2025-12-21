package com.miliogo.notafiscal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Locale

fun formatFloat(value: Float): String {
    return String.format(Locale.CANADA, "%.2f", value)
}

@Composable
fun LookupScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
)
{
    var query by remember { mutableStateOf("") }
    var products by remember { mutableStateOf(emptyList<Product>()) }

    Column(
        modifier = modifier
    ) {

        SearchBar(
            query,
            onQueryChange = { query = it }
        ) {
            viewModel.lookupMiliogoProducts(
                buildJsonObject {
                    put("nome", query)
                }
            ) {
                products = it
            }
        }

        Text(
            text = "${products.size} produtos encontrados",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )

        ProductList(products)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (newQuery: String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSearch) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        }

        TextField(
            value = query,
            modifier = Modifier.weight(1f),
            onValueChange = onQueryChange,
            placeholder = { Text("Nome do produto") },
            singleLine = true
        )

        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search"
                )
            }
        }
    }
}

@Composable
fun ProductList(products: List<Product>) {
    if (products.isEmpty())
        return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
        {
            Text(
                text = product.nome,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp),
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            )
            {
                val valor = formatFloat(product.valor)

                if (product.desconto != 0.0f)
                {
                    val desconto = formatFloat(product.desconto)
                    Text(
                        text = "R$${valor} / ${product.un} - R$${desconto}",
                        fontSize = 16.sp,
                    )
                } else {
                    Text(
                        text = "R$${valor} / ${product.un}",
                        fontSize = 16.sp,
                    )
                }

                Text(
                    text = product.data,
                    fontSize = 16.sp,
                )
                Text(
                    text = product.emitente,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

