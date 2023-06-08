package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun CPUDisplay(
    cpuState: CPUState,
    currentInstruction: String,
    onStart: () -> Unit,
    onStep: () -> Unit,
    onReset: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp, 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "N",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (cpuState.negativeFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "V",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (cpuState.overflowFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "-",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "B",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (cpuState.breakFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "D",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (cpuState.decimalFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "I",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (cpuState.interruptDisableFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Z",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (cpuState.zeroFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "C",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (cpuState.carryFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
        }

        Text(
            text = "PC: ${cpuState.programCounter}",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "SP: ${cpuState.stackPointer}",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "A: ${cpuState.accumulator}",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "X: ${cpuState.xRegister}",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "Y: ${cpuState.yRegister}",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier.height(10.dp))

        Text(
            text = currentInstruction,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier.height(10.dp))

        Button(onStep) {
            Text("Step")
        }

        Button(onStart) {
            Text("Start")
        }

        Button(onReset) {
            Text("Reset")
        }

        Button(onStop) {
            Text("Stop")
        }

    }
}

@Composable
fun ZeroPageDisplay(zeroPage: Array<Array<String>>, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        for (array in zeroPage) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                for (value in array) {
                    Text(
                        text = value,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}