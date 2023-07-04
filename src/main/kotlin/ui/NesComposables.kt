package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
fun NesEmulatorScreen(uiState: NesEmulatorUiState) {
    val gameViewUiState = uiState.gameViewUiState.collectAsState()
    val mainCpuViewState = uiState.mainCpuViewState.collectAsState()
    val zeroPageViewState = uiState.zeroPageViewState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        GameView(
            gameViewUiState,
            modifier = Modifier
                .weight(1f)
                .background(Color.Black)

            //.fillMaxSize()
        )
        EmulatorHudView(
            mainCpuViewState = mainCpuViewState,
            zeroPageViewState = zeroPageViewState,
            onStart = uiState::start,
            onStep = uiState::step,
            onReset = uiState::reset,
            onStop = uiState::stop,
            modifier = Modifier
                .background(DarkGreen100)
                .padding(12.dp)
                .fillMaxHeight()
        )
    }
}

@Composable
fun GameView(
    gameViewUiState: State<GameViewUiState>,
    modifier: Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(1.07f)
                .fillMaxSize(),
        ) {
            val pixelWidth = size.width / 256f
            val pixelHeight = size.height / 240f

            for ((i, scanline) in gameViewUiState.value.pixelScreen.withIndex()) {
               for ((j, pixel) in scanline.withIndex()) {
                   drawRect(
                       topLeft = Offset(x = pixelWidth * j, y= pixelHeight * i),
                       size = Size(width = pixelWidth, height = pixelHeight),
                       color = pixel
                   )
               }
            }

            /*for ((index, pixel) in gameViewUiState.pixelScreen[0].withIndex()) {
                drawRect(
                    topLeft = Offset(x = (pixelWidth * index) - 1, y= 0f ),
                    size = Size(width = pixelWidth, height = pixelHeight),
                    color = pixel
                )
            }*/
        }
    }
}

@Composable
fun EmulatorHudView(
    mainCpuViewState: State<MainCpuViewState>,
    zeroPageViewState: State<ZeroPageViewState>,
    onStart: () -> Unit,
    onStep: () -> Unit,
    onReset: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
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
fun MainCpuView(mainCpuViewState: State<MainCpuViewState>) {
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

            for (flag in mainCpuViewState.value.flags) {
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

        for (register in mainCpuViewState.value.registers) {
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
    zeroPageViewState: State<ZeroPageViewState>
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.White)
            .padding(5.dp, 0.dp, 0.dp, 0.dp)
    ) {
        for (array in zeroPageViewState.value.zeroPage) {
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