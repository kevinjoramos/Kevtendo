package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toBitmap
import org.jetbrains.skiko.toImage
import util.to2DigitHexString
import java.awt.image.BufferedImage

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalComposeUiApi::class)
@Composable
fun NesEmulatorScreen(uiState: NesEmulatorUiState) {
    val controller1 = uiState.controller1
    val controller2 = uiState.controller2

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

    val instructionViewState1 = uiState.instructionViewState1.collectAsState()
    val instructionViewState2 = uiState.instructionViewState2.collectAsState()
    val instructionViewState3 = uiState.instructionViewState3.collectAsState()
    val instructionViewState4 = uiState.instructionViewState4.collectAsState()
    val instructionViewState5 = uiState.instructionViewState5.collectAsState()
    val instructionViewState6 = uiState.instructionViewState6.collectAsState()

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
            .fillMaxSize()
            .onKeyEvent {

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


                // Controller 2 Inputs
                true
            }
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

            instructionViewState1 = instructionViewState1,
            instructionViewState2 = instructionViewState2,
            instructionViewState3 = instructionViewState3,
            instructionViewState4 = instructionViewState4,
            instructionViewState5 = instructionViewState5,
            instructionViewState6 = instructionViewState6,

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
                .background(ProjectColors.DarkGreen100)
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
                                    color = when (pixel.toUInt()) {
                                        0x00u -> ProjectColors.COLOR_00
                                        0x01u -> ProjectColors.COLOR_01
                                        0x02u -> ProjectColors.COLOR_02
                                        0x03u -> ProjectColors.COLOR_03
                                        0x04u -> ProjectColors.COLOR_04
                                        0x05u -> ProjectColors.COLOR_05
                                        0x06u -> ProjectColors.COLOR_06
                                        0x07u -> ProjectColors.COLOR_07
                                        0x08u -> ProjectColors.COLOR_08
                                        0x09u -> ProjectColors.COLOR_09
                                        0x0Au -> ProjectColors.COLOR_0A
                                        0x0Bu -> ProjectColors.COLOR_0B
                                        0x0Cu -> ProjectColors.COLOR_0C
                                        0x0Du -> ProjectColors.COLOR_0D
                                        0x0Eu -> ProjectColors.COLOR_0E
                                        0x0Fu -> ProjectColors.COLOR_0F
                                        0x10u -> ProjectColors.COLOR_10
                                        0x11u -> ProjectColors.COLOR_11
                                        0x12u -> ProjectColors.COLOR_12
                                        0x13u -> ProjectColors.COLOR_13
                                        0x14u -> ProjectColors.COLOR_14
                                        0x15u -> ProjectColors.COLOR_15
                                        0x16u -> ProjectColors.COLOR_16
                                        0x17u -> ProjectColors.COLOR_17
                                        0x18u -> ProjectColors.COLOR_18
                                        0x19u -> ProjectColors.COLOR_19
                                        0x1Au -> ProjectColors.COLOR_1A
                                        0x1Bu -> ProjectColors.COLOR_1B
                                        0x1Cu -> ProjectColors.COLOR_1C
                                        0x1Du -> ProjectColors.COLOR_1D
                                        0x1Eu -> ProjectColors.COLOR_1E
                                        0x1Fu -> ProjectColors.COLOR_1F
                                        0x20u -> ProjectColors.COLOR_20
                                        0x21u -> ProjectColors.COLOR_21
                                        0x22u -> ProjectColors.COLOR_22
                                        0x23u -> ProjectColors.COLOR_23
                                        0x24u -> ProjectColors.COLOR_24
                                        0x25u -> ProjectColors.COLOR_25
                                        0x26u -> ProjectColors.COLOR_26
                                        0x27u -> ProjectColors.COLOR_27
                                        0x28u -> ProjectColors.COLOR_28
                                        0x29u -> ProjectColors.COLOR_29
                                        0x2Au -> ProjectColors.COLOR_2A
                                        0x2Bu -> ProjectColors.COLOR_2B
                                        0x2Cu -> ProjectColors.COLOR_2C
                                        0x2Du -> ProjectColors.COLOR_2D
                                        0x2Eu -> ProjectColors.COLOR_2E
                                        0x2Fu -> ProjectColors.COLOR_1F
                                        0x30u -> ProjectColors.COLOR_30
                                        0x31u -> ProjectColors.COLOR_31
                                        0x32u -> ProjectColors.COLOR_32
                                        0x33u -> ProjectColors.COLOR_33
                                        0x34u -> ProjectColors.COLOR_34
                                        0x35u -> ProjectColors.COLOR_35
                                        0x36u -> ProjectColors.COLOR_36
                                        0x37u -> ProjectColors.COLOR_37
                                        0x38u -> ProjectColors.COLOR_38
                                        0x39u -> ProjectColors.COLOR_39
                                        0x3Au -> ProjectColors.COLOR_3A
                                        0x3Bu -> ProjectColors.COLOR_3B
                                        0x3Cu -> ProjectColors.COLOR_3C
                                        0x3Du -> ProjectColors.COLOR_3D
                                        0x3Eu -> ProjectColors.COLOR_3E
                                        else -> ProjectColors.COLOR_3F
                                    }
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

@OptIn(ExperimentalUnsignedTypes::class)
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
    instructionViewState1: State<String>,
    instructionViewState2: State<String>,
    instructionViewState3: State<String>,
    instructionViewState4: State<String>,
    instructionViewState5: State<String>,
    instructionViewState6: State<String>,
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
        CurrentInstructionView(
            instructionViewState1 = instructionViewState1,
            instructionViewState2 = instructionViewState2,
            instructionViewState3 = instructionViewState3,
            instructionViewState4 = instructionViewState4,
            instructionViewState5 = instructionViewState5,
            instructionViewState6 = instructionViewState6,
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
                backgroundColor = ProjectColors.LightGreen100
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
                backgroundColor = ProjectColors.LightGreen100
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
                backgroundColor = ProjectColors.LightGreen100
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
                backgroundColor = ProjectColors.LightGreen100
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

@Composable
fun CurrentInstructionView(
    instructionViewState1: State<String>,
    instructionViewState2: State<String>,
    instructionViewState3: State<String>,
    instructionViewState4: State<String>,
    instructionViewState5: State<String>,
    instructionViewState6: State<String>,
) {
    Text(
        text = instructionViewState1.value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
    Text(
        text = instructionViewState2.value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
    Text(
        text = instructionViewState3.value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
    Text(
        text =instructionViewState4.value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
    Text(
        text = instructionViewState5.value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
    Text(
        text = instructionViewState6.value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
}

