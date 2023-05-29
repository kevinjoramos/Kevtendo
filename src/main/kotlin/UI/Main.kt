package UI

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
        val nesEmulator = remember { NesEmulator() }
        CPUDisplay(
            cpuState = nesEmulator.cpuState,
            onStart = nesEmulator::start,
            onStep = nesEmulator::step,
        )
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}



