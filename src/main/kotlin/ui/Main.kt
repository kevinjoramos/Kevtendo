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
fun App(
    uiState: NesEmulatorUiState,
    isDebuggerVisible: MutableState<Boolean>
) {
    MaterialTheme {

        NesEmulatorScreen(
            uiState,
            isDebuggerVisible
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalUnsignedTypes::class)
fun main() = application {
    val nesEmulatorUiState = remember { NesEmulatorUiState() }
    val controller1 = nesEmulatorUiState.controller1
    val controller2 = nesEmulatorUiState.controller2
    val isDebuggerVisible = nesEmulatorUiState.isHudVisible

    Window(
        onCloseRequest = ::exitApplication,
        title = "NESquick Emulator",
        //state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified)
        onKeyEvent = {
        // Controller 1 Inputs.
        if (it.key == Key.J && it.type == KeyEventType.KeyDown) {
            controller1.buttonA = true
        }

        if (it.key == Key.J && it.type == KeyEventType.KeyUp) {
            controller1.buttonA = false
        }

        if (it.key == Key.K && it.type == KeyEventType.KeyDown) {
            controller1.buttonB = true
        }

        if (it.key == Key.K && it.type == KeyEventType.KeyUp) {
            controller1.buttonB = false
        }

        if (it.key == Key.NumPad1 && it.type == KeyEventType.KeyDown) {
            controller1.buttonSelect = true
        }

        if (it.key == Key.NumPad1 && it.type == KeyEventType.KeyUp) {
            controller1.buttonSelect = false
        }

        if (it.key == Key.NumPad2 && it.type == KeyEventType.KeyDown) {
            controller1.buttonStart = true
        }

        if (it.key == Key.NumPad2 && it.type == KeyEventType.KeyUp) {
            controller1.buttonStart = false
        }

        if (it.key == Key.W && it.type == KeyEventType.KeyDown) {
            controller1.buttonUp = true
        }

        if (it.key == Key.W && it.type == KeyEventType.KeyUp) {
            controller1.buttonUp = false
        }

        if (it.key == Key.S && it.type == KeyEventType.KeyDown) {
            controller1.buttonDown = true
        }

        if (it.key == Key.S && it.type == KeyEventType.KeyUp) {
            controller1.buttonDown = false
        }

        if (it.key == Key.A && it.type == KeyEventType.KeyDown) {
            controller1.buttonLeft = true
        }

        if (it.key == Key.A && it.type == KeyEventType.KeyUp) {
            controller1.buttonLeft = false
        }

        if (it.key == Key.D && it.type == KeyEventType.KeyDown) {
            controller1.buttonRight = true
        }

        if (it.key == Key.D && it.type == KeyEventType.KeyUp) {
            controller1.buttonRight = false
        }
        if (it.key == Key.F11 && it.type == KeyEventType.KeyDown) {
            isDebuggerVisible.value = !isDebuggerVisible.value

        }

        // Controller 2 Inputs
        true
    }
    ) {
        App(
            uiState = nesEmulatorUiState,
            isDebuggerVisible = isDebuggerVisible
        )
    }
}



