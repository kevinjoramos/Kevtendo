package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import controller.GameController

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val nesEmulatorUiState = remember { NesEmulatorUiState() }
        NesEmulatorScreen(nesEmulatorUiState)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NESquick Emulator",
        //state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    ) {
        App()
    }
}



