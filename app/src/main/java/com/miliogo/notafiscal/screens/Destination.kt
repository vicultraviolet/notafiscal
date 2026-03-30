package com.miliogo.notafiscal.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

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
