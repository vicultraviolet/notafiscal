package com.miliogo.nfce.screens

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miliogo.nfce.MainViewModel
import com.miliogo.nfce.MiliogoUserAction
import com.miliogo.nfce.ui.theme.CurrentTheme

enum class Settings(val route: String) {
    MAIN("main"),
    APPEARANCES("appearances"),
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
fun MainSettings(
    viewModel: MainViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Top
    ) {
        SettingsButton(text = "Minha conta") {
            navController.navigate(Settings.ACCOUNT.route)
        }

        SettingsButton(text = "Aparência") {
            navController.navigate(Settings.APPEARANCES.route)
        }
    }
}

@Composable
fun AppearancesSettings(
    viewModel: MainViewModel,
) {
    val themeLabels = listOf("Claro", "Escuro", "Padrão do Sistema")
    val currentTheme by viewModel.theme.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Top
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            themeLabels.forEachIndexed { index, label ->
                SegmentedButton(
                    modifier = Modifier.height(56.dp),
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = themeLabels.size
                    ),
                    onClick = { viewModel.updateCurrentTheme(CurrentTheme.entries[index])},
                    selected = currentTheme.ordinal == index,
                    label = { Text(label) }
                )
            }
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

    var privacyChecked by remember { mutableStateOf(false) }

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

        if (action == MiliogoUserAction.CREATE) {
            val context = LocalContext.current
            val privacyUrl = "https://miliogo.com/privacidade.html"

            val annotatedText = buildAnnotatedString {
                append("Ao criar uma conta Miliogo, concordo com a ")

                withLink(
                    LinkAnnotation.Clickable(
                        tag = "privacy_policy",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = Color(0xFF4DB6AC),
                                textDecoration = TextDecoration.Underline
                            )
                        ),
                        linkInteractionListener = {
                            CustomTabsIntent.Builder()
                                .build()
                                .launchUrl(context, privacyUrl.toUri())
                        }
                    )
                ) {
                    append("Política de Privacidade")
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.height(64.dp).padding(horizontal = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = privacyChecked,
                    onCheckedChange = { privacyChecked = it }
                )

                Text(text = annotatedText)
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        SettingsButton(
            when (action) {
                MiliogoUserAction.LOGIN -> "Entrar"
                MiliogoUserAction.CREATE -> "Criar conta"
            }
        ) {
            if (action == MiliogoUserAction.CREATE && !privacyChecked) {
                error = "Certifique-se de que tenha concordado com a Política de Privacidade"
                return@SettingsButton
            }

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
        composable(Settings.MAIN.route) { MainSettings(viewModel, navController) }
        composable(Settings.APPEARANCES.route) { AppearancesSettings(viewModel) }
        composable(Settings.ACCOUNT.route) { AccountSettings(viewModel, navController) }
        composable(Settings.LOGIN.route) { LogInSettings(viewModel, navController, MiliogoUserAction.LOGIN)}
        composable(Settings.CREATE_ACCOUNT.route) { LogInSettings(viewModel, navController, MiliogoUserAction.CREATE)}
    }
}
