package com.miliogo.nfce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miliogo.nfce.ui.theme.NotaFiscalTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.miliogo.nfce.screens.AddScreen
import com.miliogo.nfce.screens.Destination
import com.miliogo.nfce.screens.HomeScreen
import com.miliogo.nfce.screens.LookupScreen
import com.miliogo.nfce.screens.SettingsScreen
import com.miliogo.nfce.ui.theme.CurrentTheme

class MainActivity : ComponentActivity() {
    var dataStoreManager: DataStoreManager? = null
    var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        dataStoreManager = DataStoreManager(this)
        dataStoreManager?.let { mainViewModel = MainViewModel(it) }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme = mainViewModel?.theme?.collectAsState()?.value ?: CurrentTheme.Default

            NotaFiscalTheme(theme) {
                mainViewModel?.let {
                    App(it)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME
    var selectedDestination by remember { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Cupom Fiscal")
                }
            )
        },
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
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
}
