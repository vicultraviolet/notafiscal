package com.miliogo.notafiscal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

var shouldScan = true

@Composable
fun AddScreen(modifier: Modifier = Modifier)
{
    val result = remember { mutableStateOf("") }

    if (shouldScan)
        ScanWithPermission(modifier) {
            result.value = it
            shouldScan = false
        }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(result.value)
    }
}

@Composable
fun AccountScreen(modifier: Modifier = Modifier)
{

}

@Composable
fun AppNavHost(
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
                    Destination.ADD     -> {
                        shouldScan = true
                        AddScreen(modifier)
                    }
                    Destination.ACCOUNT -> AccountScreen(modifier)
                }
            }
        }
    }
}
