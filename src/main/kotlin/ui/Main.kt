package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val nesEmulatorUiState = remember { NesEmulatorUiState() }
        NesEmulatorScreen(nesEmulatorUiState)
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NESquick Emulator",
    ) {
        App()
    }
}



