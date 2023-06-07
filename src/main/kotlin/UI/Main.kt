package UI

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val nesEmulator = remember { NesEmulator() }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            ZeroPageDisplay(
                zeroPage = nesEmulator.zeroPageState,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
            )

            CPUDisplay(
                cpuState = nesEmulator.cpuState,
                currentInstruction = nesEmulator.currentInstruction,
                onStart = nesEmulator::start,
                onStep = nesEmulator::step,
                onReset = nesEmulator::reset,
                onStop = nesEmulator::stop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}



