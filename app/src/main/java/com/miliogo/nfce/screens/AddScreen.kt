package com.miliogo.nfce.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.miliogo.nfce.MainViewModel
import com.miliogo.nfce.ScanWithPermission
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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
            viewModel.processNFCe(it) { msg ->
                shouldScan = false
                waitText = msg

                delay(3.seconds)

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
