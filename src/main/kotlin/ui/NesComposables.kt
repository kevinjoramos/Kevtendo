package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
fun NesEmulatorScreen(uiState: NesEmulatorUiState) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        GameView(uiState.gameViewUiState)
        EmulatorHudView(
            mainCpuViewState = uiState.mainCpuViewState,
            zeroPageViewState = uiState.zeroPageViewState,
            onStart = uiState::start,
            onStep = uiState::step,
            onReset = uiState::reset,
            onStop = uiState::stop
        )
    }
}

@Composable
fun GameView(
    gameViewUiState: GameViewUiState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(.66f)
            .fillMaxHeight()
            .background(Color.Black)
    ) {
        for (scanline in gameViewUiState.pixelScreen) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (pixel in scanline) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RectangleShape)
                            .background(pixel)
                            .border(1.dp, Color.Black)
                    )
                }
            }
        }
    }
}

@Composable
fun EmulatorHudView(
    mainCpuViewState: MainCpuViewState,
    zeroPageViewState: ZeroPageViewState,
    onStart: () -> Unit,
    onStep: () -> Unit,
    onReset: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen100)
            .padding(12.dp)
    ) {
        ButtonsView(
            onStart,
            onStep,
            onReset,
            onStop
        )
        Spacer(modifier = Modifier.height(12.dp))
        MainCpuView(mainCpuViewState)
        Spacer(modifier = Modifier.height(12.dp))
        ZeroPageView(zeroPageViewState)
    }
        /*
        Spacer(modifier.height(10.dp))



    }*/
}

@Composable
fun ButtonsView(
    onStart: () -> Unit,
    onStep: () -> Unit,
    onReset: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(50f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGreen100
            )
        ) {
            Text(
                text = "START",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace

            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onReset,
            shape = RoundedCornerShape(50f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGreen100
            )
        ) {
            Text(
                text = "RESET",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace

            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onStop,
            shape = RoundedCornerShape(50f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGreen100
            )
        ) {
            Text(
                text = "STOP",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace

            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onStep,
            shape = RoundedCornerShape(50f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = LightGreen100
            )
        ) {
            Text(
                text = "STEP",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace

            )
        }

    }
}

@Composable
fun MainCpuView(mainCpuViewState: MainCpuViewState) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STATUS: ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )

            for (flag in mainCpuViewState.flags) {
                Text(
                    text = flag.first,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (flag.second) Color.Gray else Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(
                    modifier = Modifier.width(6.dp)
                )
            }
        }

        for (register in mainCpuViewState.registers) {
            Text(
                text = register,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ZeroPageView(
    zeroPageViewState: ZeroPageViewState
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.White)
            .padding(5.dp, 0.dp, 0.dp, 0.dp)
    ) {
        for (array in zeroPageViewState.zeroPage) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                for (value in array) {
                    Text(
                        text = value,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(
                        modifier = Modifier
                            .width(5.dp)
                    )
                }
            }
        }
    }
}