package com.miliogo.notafiscal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.seconds

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Home", Icons.Default.Home, "Home"),
    LOOKUP("lookup", "Consulta", Icons.Default.Search, "Lookup"),
    ADD("add", "Adicionar", Icons.Default.Add, "Add"),
    SETTINGS("settings", "Configurações", Icons.Default.Settings, "Settings")
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Bem vindo!")
    }
}

@Composable
fun AddScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
)  {
    var shouldScan by remember { mutableStateOf(true) }
    var waitText by remember { mutableStateOf("Processando...") }

    if (shouldScan) {
        ScanWithPermission(modifier) {
            if (!shouldScan)
                return@ScanWithPermission

            shouldScan = false
            viewModel.processNFCe(it) { miliogoResponse ->
                val json = Json.parseToJsonElement(miliogoResponse)

                val msg = json.jsonObject["mensagem"]?.jsonPrimitive?.content
                if (msg != null)
                    waitText = msg

                delay(2.seconds)

                shouldScan = true
                waitText = "Processando..."
            }
        }
    } else
    {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(waitText)
        }
    }
}

@Composable
fun AppNavHost(
    viewModel: MainViewModel,
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        composable(Destination.HOME.route)     { HomeScreen(modifier) }
        composable(Destination.LOOKUP.route)   { LookupScreen(viewModel, modifier) }
        composable(Destination.ADD.route)      { AddScreen(viewModel, modifier) }
        composable(Destination.SETTINGS.route) { SettingsScreen(viewModel, modifier) }
    }
}
