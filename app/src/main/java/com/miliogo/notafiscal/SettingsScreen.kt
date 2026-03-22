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
    SIGN_IN("sign_in"),
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

    val button_modifier = Modifier.fillMaxWidth().height(64.dp).padding(vertical = 4.dp)

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
                    onClick = { navController.navigate("account") },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(4.dp),
                    modifier = button_modifier
                ) {
                    Text("Minha conta")
                }
            }
        }
        composable(Settings.ACCOUNT.route) {
            if (!secretKey.isEmpty()) {
                OutlinedTextField(
                    value = secretKey,
                    onValueChange = { viewModel.updateSecretKey(it) },
                    label = { Text("Secret Key") },
                    modifier = modifier.fillMaxWidth()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    TextButton(
                        onClick = { navController.navigate("sign_in") },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(4.dp),
                        modifier = button_modifier
                    ) {
                        Text("Entrar")
                    }
                    TextButton(
                        onClick = { navController.navigate("create_account") },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(4.dp),
                        modifier = button_modifier
                    ) {
                        Text("Criar conta")
                    }
                }
            }
        }
        composable(Settings.SIGN_IN.route) {
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
                    onClick = { viewModel.updateSecretKey("ajaj") },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Entrar")
                }
            }
        }
        composable(Settings.CREATE_ACCOUNT.route) {
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
                    onClick = { viewModel.updateSecretKey("ajaj") },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Criar conta")
                }
            }
        }
    }
}
