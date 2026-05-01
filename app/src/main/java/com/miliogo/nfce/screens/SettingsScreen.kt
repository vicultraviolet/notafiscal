package com.miliogo.nfce.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miliogo.nfce.MainViewModel
import com.miliogo.nfce.MiliogoUserAction

enum class Settings(val route: String) {
    MAIN("main"),
    LOGIN("login"),
    CREATE_ACCOUNT("create_account"),
    ACCOUNT("account")
}

@Composable
fun SettingsButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.fillMaxWidth().height(64.dp).padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}

@Composable
fun MainSettings(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Top
    ) {
        SettingsButton(text = "Minha conta") {
            navController.navigate(Settings.ACCOUNT.route)
        }
    }
}

@Composable
fun AccountSettings(
    viewModel: MainViewModel,
    navController: NavController,
) {
    val secretKey by viewModel.secretKey.collectAsState()

    if (!secretKey.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = secretKey,
                onValueChange = { viewModel.updateSecretKey(it) },
                label = { Text("Secret Key") },
                modifier = Modifier.fillMaxWidth()
            )

            SettingsButton("Sair da conta") {
                viewModel.updateSecretKey("")
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            SettingsButton("Entrar") {
                navController.navigate(Settings.LOGIN.route)
            }
            SettingsButton("Criar conta") {
                navController.navigate(Settings.CREATE_ACCOUNT.route)
            }
        }
    }
}

@Composable
fun LogInSettings(
    viewModel: MainViewModel,
    navController: NavController,
    action: MiliogoUserAction
) {
    val secretKey by viewModel.secretKey.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }

    LaunchedEffect(secretKey) {
        if (!secretKey.isEmpty()) {
            navController.navigate(Settings.ACCOUNT.route) {
                popUpTo(Settings.LOGIN.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nome de usuário") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        SettingsButton(
            when (action) {
                MiliogoUserAction.LOGIN -> "Entrar"
                MiliogoUserAction.CREATE -> "Criar conta"
            }
        ) {
            viewModel.miliogoUser(action, username, password, onError={ error=it }) {
                error = ""
                viewModel.updateSecretKey(it)
            }
        }

        if (!error.isEmpty()) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Text(error)
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val startDestination = Settings.MAIN

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Settings.MAIN.route) { MainSettings(navController) }
        composable(Settings.ACCOUNT.route) { AccountSettings(viewModel, navController) }
        composable(Settings.LOGIN.route) { LogInSettings(viewModel, navController, MiliogoUserAction.LOGIN)}
        composable(Settings.CREATE_ACCOUNT.route) { LogInSettings(viewModel, navController, MiliogoUserAction.CREATE)}
    }
}
