package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

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



