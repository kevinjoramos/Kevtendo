package UI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CPUDisplay(
    cpuState: CPUState,
    onStart: () -> Unit,
    onStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "N",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = if (cpuState.negativeFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "V",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = if (cpuState.overflowFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "-",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "B",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = if (cpuState.breakFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "D",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = if (cpuState.decimalFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "I",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = if (cpuState.interruptDisableFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Z",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = if (cpuState.zeroFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "C",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = if (cpuState.carryFlag) Color.Black else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
        }

        Text(
            text = "PC: ${cpuState.programCounter}",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "SP: ${cpuState.stackPointer}",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "A: ${cpuState.accumulator}",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "X: ${cpuState.xRegister}",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "Y: ${cpuState.yRegister}",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.Black,
            fontFamily = FontFamily.Monospace
        )

        Button(onStep) {
            Text("Step")
        }
    }

}