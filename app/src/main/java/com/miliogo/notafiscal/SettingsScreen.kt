package com.miliogo.notafiscal

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class Settings(val route: String) {
    MAIN("main"),
    LOGIN("login"),
    CREATE_ACCOUNT("create_account"),
    ACCOUNT("account")
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val startDestination = Settings.MAIN

    val secretKey by viewModel.secretKey.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }

    val buttonModifier = Modifier.fillMaxWidth().height(64.dp).padding(vertical = 4.dp)

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Settings.MAIN.route) {
            Column(
                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                verticalArrangement = Arrangement.Top
            ) {
                TextButton(
                    onClick = { navController.navigate(Settings.ACCOUNT.route) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(4.dp),
                    modifier = buttonModifier
                ) {
                    Text("Minha conta")
                }
            }
        }
        composable(Settings.ACCOUNT.route) {
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
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    TextButton(
                        onClick = { navController.navigate(Settings.LOGIN.route) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(4.dp),
                        modifier = buttonModifier
                    ) {
                        Text("Entrar")
                    }
                    TextButton(
                        onClick = { navController.navigate(Settings.CREATE_ACCOUNT.route) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(4.dp),
                        modifier = buttonModifier
                    ) {
                        Text("Criar conta")
                    }
                }
            }
        }
        composable(Settings.LOGIN.route) {
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

                TextButton(
                    onClick = {
                        viewModel.miliogoUser(
                            MiliogoUserAction.LOGIN,
                            username,
                            password,
                            onError = { error = it }
                        ) {
                            error = ""
                            viewModel.updateSecretKey(it)
                        }
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Entrar")
                }

                if (!error.isEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )

                    Text(error)
                }
            }
        }
        composable(Settings.CREATE_ACCOUNT.route) {
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

                TextButton(
                    onClick = {
                        viewModel.miliogoUser(
                            MiliogoUserAction.CREATE,
                            username,
                            password,
                            onError = { error = it }
                        ) {
                            error = ""
                            viewModel.updateSecretKey(it)
                        }
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Criar conta")
                }

                if (!error.isEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )

                    Text(error)
                }
            }
        }
    }
}
