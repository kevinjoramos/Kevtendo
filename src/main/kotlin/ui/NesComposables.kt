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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
fun NesEmulatorScreen(uiState: NesEmulatorUiState) {
    val gameViewUiState = uiState.gameViewUiState.collectAsState()

    val programCounterViewState = uiState.programCounterViewState.collectAsState()
    val accumulatorViewState = uiState.accumulatorViewState.collectAsState()
    val xRegisterViewState = uiState.xRegisterViewState.collectAsState()
    val yRegisterViewState = uiState.yRegisterViewState.collectAsState()
    val stackPointerViewState = uiState.stackPointerViewState.collectAsState()
    val negativeFlagViewState = uiState.negativeFlagViewState.collectAsState()
    val overflowFlagViewState = uiState.overflowFlagViewState.collectAsState()
    val extraFlagViewState = uiState.extraFlagViewState.collectAsState()
    val breakFlagViewState = uiState.breakFlagViewState.collectAsState()
    val decimalFlagViewState = uiState.decimalFlagViewState.collectAsState()
    val interruptDisableViewState = uiState.interruptDisableViewState.collectAsState()
    val zeroFlagViewState = uiState.zeroFlagViewState.collectAsState()
    val carryFlagViewState = uiState.carryFlagViewState.collectAsState()

    val zeroPageRow1ViewState = uiState.zeroPageRow1ViewState.collectAsState()
    val zeroPageRow2ViewState = uiState.zeroPageRow2ViewState.collectAsState()
    val zeroPageRow3ViewState = uiState.zeroPageRow3ViewState.collectAsState()
    val zeroPageRow4ViewState = uiState.zeroPageRow4ViewState.collectAsState()
    val zeroPageRow5ViewState = uiState.zeroPageRow5ViewState.collectAsState()
    val zeroPageRow6ViewState = uiState.zeroPageRow6ViewState.collectAsState()
    val zeroPageRow7ViewState = uiState.zeroPageRow7ViewState.collectAsState()
    val zeroPageRow8ViewState = uiState.zeroPageRow8ViewState.collectAsState()
    val zeroPageRow9ViewState = uiState.zeroPageRow9ViewState.collectAsState()
    val zeroPageRow10ViewState = uiState.zeroPageRow10ViewState.collectAsState()
    val zeroPageRow11ViewState = uiState.zeroPageRow11ViewState.collectAsState()
    val zeroPageRow12ViewState = uiState.zeroPageRow12ViewState.collectAsState()
    val zeroPageRow13ViewState = uiState.zeroPageRow13ViewState.collectAsState()
    val zeroPageRow14ViewState = uiState.zeroPageRow14ViewState.collectAsState()
    val zeroPageRow15ViewState = uiState.zeroPageRow15ViewState.collectAsState()
    val zeroPageRow16ViewState = uiState.zeroPageRow16ViewState.collectAsState()

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
            programCounterViewState = programCounterViewState,
            accumulatorViewState = accumulatorViewState,
            xRegisterViewState = xRegisterViewState,
            yRegisterViewState = yRegisterViewState,
            stackPointerViewState = stackPointerViewState,
            negativeFlagViewState = negativeFlagViewState,
            overflowFlagViewState = overflowFlagViewState,
            extraFlagViewState = extraFlagViewState,
            breakFlagViewState = breakFlagViewState,
            decimalFlagViewState = decimalFlagViewState,
            interruptDisableViewState = interruptDisableViewState,
            zeroFlagViewState = zeroFlagViewState,
            carryFlagViewState = carryFlagViewState,
            zeroPageRow1ViewState = zeroPageRow1ViewState,
            zeroPageRow2ViewState = zeroPageRow2ViewState,
            zeroPageRow3ViewState = zeroPageRow3ViewState,
            zeroPageRow4ViewState = zeroPageRow4ViewState,
            zeroPageRow5ViewState = zeroPageRow5ViewState,
            zeroPageRow6ViewState = zeroPageRow6ViewState,
            zeroPageRow7ViewState = zeroPageRow7ViewState,
            zeroPageRow8ViewState = zeroPageRow8ViewState,
            zeroPageRow9ViewState = zeroPageRow9ViewState,
            zeroPageRow10ViewState = zeroPageRow10ViewState,
            zeroPageRow11ViewState = zeroPageRow11ViewState,
            zeroPageRow12ViewState = zeroPageRow12ViewState,
            zeroPageRow13ViewState = zeroPageRow13ViewState,
            zeroPageRow14ViewState = zeroPageRow14ViewState,
            zeroPageRow15ViewState = zeroPageRow15ViewState,
            zeroPageRow16ViewState = zeroPageRow16ViewState,
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
        Spacer(
            modifier = Modifier
                .aspectRatio(1.07f)
                .fillMaxSize()
                .drawWithCache {
                    val pixelWidth = size.width / 256f
                    val pixelHeight = size.height / 240f
                    val pixelSize = Size(width = pixelWidth, height = pixelHeight)

                    onDrawWithContent {
                        for ((i, scanline) in gameViewUiState.value.pixelScreen.withIndex()) {
                            for ((j, pixel) in scanline.withIndex()) {
                                drawRect(
                                    topLeft = Offset(x = pixelWidth * j, y= pixelHeight * i),
                                    size = pixelSize,
                                    color = pixel,
                                )
                            }
                        }
                    }
                },
        )
    }
}

/*
State<String>
 */

@Composable
fun EmulatorHudView(
    programCounterViewState: State<String>,
    accumulatorViewState: State<String>,
    xRegisterViewState: State<String>,
    yRegisterViewState: State<String>,
    stackPointerViewState: State<String>,
    negativeFlagViewState: State<Boolean>,
    overflowFlagViewState: State<Boolean>,
    extraFlagViewState: State<Boolean>,
    breakFlagViewState: State<Boolean>,
    decimalFlagViewState: State<Boolean>,
    interruptDisableViewState: State<Boolean>,
    zeroFlagViewState: State<Boolean>,
    carryFlagViewState: State<Boolean>,
    zeroPageRow1ViewState: State<String>,
    zeroPageRow2ViewState: State<String>,
    zeroPageRow3ViewState: State<String>,
    zeroPageRow4ViewState: State<String>,
    zeroPageRow5ViewState: State<String>,
    zeroPageRow6ViewState: State<String>,
    zeroPageRow7ViewState: State<String>,
    zeroPageRow8ViewState: State<String>,
    zeroPageRow9ViewState: State<String>,
    zeroPageRow10ViewState: State<String>,
    zeroPageRow11ViewState: State<String>,
    zeroPageRow12ViewState: State<String>,
    zeroPageRow13ViewState: State<String>,
    zeroPageRow14ViewState: State<String>,
    zeroPageRow15ViewState: State<String>,
    zeroPageRow16ViewState: State<String>,
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
        MainCpuView(
            programCounterViewState = programCounterViewState,
            accumulatorViewState = accumulatorViewState,
            xRegisterViewState = xRegisterViewState,
            yRegisterViewState = yRegisterViewState,
            stackPointerViewState = stackPointerViewState,
            negativeFlagViewState = negativeFlagViewState,
            overflowFlagViewState = overflowFlagViewState,
            extraFlagViewState = extraFlagViewState,
            breakFlagViewState = breakFlagViewState,
            decimalFlagViewState = decimalFlagViewState,
            interruptDisableViewState = interruptDisableViewState,
            zeroFlagViewState = zeroFlagViewState,
            carryFlagViewState = carryFlagViewState,
        )
        Spacer(modifier = Modifier.height(12.dp))
        ZeroPageView(
            zeroPageRow1ViewState = zeroPageRow1ViewState,
            zeroPageRow2ViewState = zeroPageRow2ViewState,
            zeroPageRow3ViewState = zeroPageRow3ViewState,
            zeroPageRow4ViewState = zeroPageRow4ViewState,
            zeroPageRow5ViewState = zeroPageRow5ViewState,
            zeroPageRow6ViewState = zeroPageRow6ViewState,
            zeroPageRow7ViewState = zeroPageRow7ViewState,
            zeroPageRow8ViewState = zeroPageRow8ViewState,
            zeroPageRow9ViewState = zeroPageRow9ViewState,
            zeroPageRow10ViewState = zeroPageRow10ViewState,
            zeroPageRow11ViewState = zeroPageRow11ViewState,
            zeroPageRow12ViewState = zeroPageRow12ViewState,
            zeroPageRow13ViewState = zeroPageRow13ViewState,
            zeroPageRow14ViewState = zeroPageRow14ViewState,
            zeroPageRow15ViewState = zeroPageRow15ViewState,
            zeroPageRow16ViewState = zeroPageRow16ViewState
        )
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
fun MainCpuView(
    programCounterViewState: State<String>,
    accumulatorViewState: State<String>,
    xRegisterViewState: State<String>,
    yRegisterViewState: State<String>,
    stackPointerViewState: State<String>,
    negativeFlagViewState: State<Boolean>,
    overflowFlagViewState: State<Boolean>,
    extraFlagViewState: State<Boolean>,
    breakFlagViewState: State<Boolean>,
    decimalFlagViewState: State<Boolean>,
    interruptDisableViewState: State<Boolean>,
    zeroFlagViewState: State<Boolean>,
    carryFlagViewState: State<Boolean>,
) {
    Column {
        StatusFlagsView(
            negativeFlagViewState = negativeFlagViewState,
            overflowFlagViewState = overflowFlagViewState,
            extraFlagViewState = extraFlagViewState,
            breakFlagViewState = breakFlagViewState,
            decimalFlagViewState = decimalFlagViewState,
            interruptDisableViewState = interruptDisableViewState,
            zeroFlagViewState = zeroFlagViewState,
            carryFlagViewState = carryFlagViewState,
        )

        RegisterView("PC:", programCounterViewState.value)
        RegisterView("A:", accumulatorViewState.value)
        RegisterView("X:", xRegisterViewState.value)
        RegisterView("Y:", yRegisterViewState.value)
        RegisterView("SP:", stackPointerViewState.value)
    }
}

@Composable
fun StatusFlagsView(
    negativeFlagViewState: State<Boolean>,
    overflowFlagViewState: State<Boolean>,
    extraFlagViewState: State<Boolean>,
    breakFlagViewState: State<Boolean>,
    decimalFlagViewState: State<Boolean>,
    interruptDisableViewState: State<Boolean>,
    zeroFlagViewState: State<Boolean>,
    carryFlagViewState: State<Boolean>
) {
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

       FlagView("N", negativeFlagViewState.value)
       FlagView("V", overflowFlagViewState.value)
       FlagView("-", extraFlagViewState.value)
       FlagView("B", breakFlagViewState.value)
       FlagView("D", decimalFlagViewState.value)
       FlagView("I", interruptDisableViewState.value)
       FlagView("Z", zeroFlagViewState.value)
       FlagView("C", carryFlagViewState.value)
    }
}

@Composable
fun FlagView(name: String, isActive: Boolean) {
    Text(
        text = name,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = if (isActive) Color.White else Color.Gray,
        fontFamily = FontFamily.Monospace
    )
    Spacer(
        modifier = Modifier.width(6.dp)
    )
}

@Composable
fun RegisterView(label: String, value: String) {
    Text(
        text = "$label $value",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
fun ZeroPageView(
    zeroPageRow1ViewState: State<String>,
    zeroPageRow2ViewState: State<String>,
    zeroPageRow3ViewState: State<String>,
    zeroPageRow4ViewState: State<String>,
    zeroPageRow5ViewState: State<String>,
    zeroPageRow6ViewState: State<String>,
    zeroPageRow7ViewState: State<String>,
    zeroPageRow8ViewState: State<String>,
    zeroPageRow9ViewState: State<String>,
    zeroPageRow10ViewState: State<String>,
    zeroPageRow11ViewState: State<String>,
    zeroPageRow12ViewState: State<String>,
    zeroPageRow13ViewState: State<String>,
    zeroPageRow14ViewState: State<String>,
    zeroPageRow15ViewState: State<String>,
    zeroPageRow16ViewState: State<String>,
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.White)
            .padding(5.dp, 5.dp, 5.dp, 0.dp)
    ) {
        ZeroPageRowView(zeroPageRow1ViewState.value)
        ZeroPageRowView(zeroPageRow2ViewState.value)
        ZeroPageRowView(zeroPageRow3ViewState.value)
        ZeroPageRowView(zeroPageRow4ViewState.value)
        ZeroPageRowView(zeroPageRow5ViewState.value)
        ZeroPageRowView(zeroPageRow6ViewState.value)
        ZeroPageRowView(zeroPageRow7ViewState.value)
        ZeroPageRowView(zeroPageRow8ViewState.value)
        ZeroPageRowView(zeroPageRow9ViewState.value)
        ZeroPageRowView(zeroPageRow10ViewState.value)
        ZeroPageRowView(zeroPageRow11ViewState.value)
        ZeroPageRowView(zeroPageRow12ViewState.value)
        ZeroPageRowView(zeroPageRow13ViewState.value)
        ZeroPageRowView(zeroPageRow14ViewState.value)
        ZeroPageRowView(zeroPageRow15ViewState.value)
        ZeroPageRowView(zeroPageRow16ViewState.value)
    }
}

@Composable
fun ZeroPageRowView(row: String) {
    Text(
        text = row,
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