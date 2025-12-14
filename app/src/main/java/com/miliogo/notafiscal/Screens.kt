package com.miliogo.notafiscal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.runtime.setValue

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Home", Icons.Default.Home, "Home"),
    LOOKUP("lookup", "Consulta", Icons.Default.Search, "Lookup"),
    ADD("add", "Adicionar", Icons.Default.Add, "Add"),
    ACCOUNT("account", "Conta", Icons.Default.AccountCircle, "Account")
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier)
{
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Bem vindo!")
    }
}

@Composable
fun LookupScreen(modifier: Modifier = Modifier)
{

}

@Composable
fun AddScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
)
{
    var shouldScan by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf("") }

    if (shouldScan)
    {
        ScanWithPermission(modifier) {
            if (!shouldScan)
                return@ScanWithPermission

            shouldScan = false
            viewModel.processNFCe(it) { miliogoResponse ->
                shouldScan = true
                result = miliogoResponse
            }
        }
    } else
    {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Processando...")
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(result)
    }
}

@Composable
fun AccountScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
)
{
    val secretKey by viewModel.secretKey.collectAsState()

    OutlinedTextField(
        value = secretKey,
        onValueChange = { viewModel.updateSecretKey(it) },
        label = { Text("Secret Key") },
        modifier = modifier.fillMaxWidth()
    )
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
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination)
                {
                    Destination.HOME    -> HomeScreen(modifier)
                    Destination.LOOKUP  -> LookupScreen(modifier)
                    Destination.ADD     -> AddScreen(viewModel, modifier)
                    Destination.ACCOUNT -> AccountScreen(viewModel, modifier)
                }
            }
        }
    }
}
