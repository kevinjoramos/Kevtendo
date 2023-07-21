package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toBitmap
import org.jetbrains.skiko.toImage
import util.to2DigitHexString
import util.to4DigitHexString
import java.awt.image.BufferedImage
import kotlin.math.floor

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalComposeUiApi::class)
@Composable
fun NesEmulatorScreen(
    uiState: NesEmulatorUiState,
    isDebuggerVisible: MutableState<Boolean>
) {


    val gameViewUiState = uiState.gameViewUiState.collectAsState()
    val zeroPageState = uiState.zeroPageState
    val mainRegistersState = uiState.mainRegistersState
    val mainFlagsState = uiState.mainFlagsState
    val disassemblerState = uiState.disassemblerState
    val patternTableState = uiState.patternTableState
    val paletteColorsState = uiState.paletteColorsState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .focusRequester(focusRequester = FocusRequester())

    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()

        ) {
            GameView(
                gameViewUiState,
                modifier = Modifier
                    .focusable()
                    .weight(1f)
                    .background(Color.Black)

                //.fillMaxSize()
            )

            if (isDebuggerVisible.value) {
                EmulatorHudView(
                    zeroPageState = zeroPageState,
                    mainRegistersState = mainRegistersState,
                    mainFlagsState = mainFlagsState,
                    disassemblerState = disassemblerState,
                    patternTableState = patternTableState,
                    paletteColorsState = paletteColorsState,
                    onStart = uiState::start,
                    onStep = uiState::step,
                    onReset = uiState::reset,
                    onStop = uiState::stop,
                    forcePaletteSelect = uiState::forcePaletteSwap,
                    modifier = Modifier
                        .background(ProjectColors.DarkGreen100)
                        .padding(12.dp)
                        .fillMaxHeight()
                )
            }
        }
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
                    val pixelWidth = floor(size.width / 256f)
                    val pixelHeight = floor(size.height / 240f)
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

@Composable
fun EmulatorHudView(
    zeroPageState: MutableState<List<UByte>>,
    mainRegistersState: MutableState<List<UInt>>,
    mainFlagsState: MutableState<List<Boolean>>,
    disassemblerState: MutableState<String>,
    patternTableState: MutableState<List<UInt>>,
    paletteColorsState: MutableState<List<UInt>>,
    onStart: () -> Unit,
    onStep: () -> Unit,
    onReset: () -> Unit,
    onStop: () -> Unit,
    forcePaletteSelect: (UInt) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        PatternTablesView(
            patternTableState = patternTableState,
        )
        Spacer(modifier = Modifier.height(6.dp))

        PaletteColorView(
            paletteColorsState = paletteColorsState,
            forcePaletteSelect = forcePaletteSelect
        )

        Spacer(modifier = Modifier.height(12.dp))

        ButtonsView(
            onStart,
            onStep,
            onReset,
            onStop
        )
        Spacer(modifier = Modifier.height(12.dp))
        MainCpuView(
            mainRegistersState = mainRegistersState,
            mainFlagsState = mainFlagsState
        )
        Spacer(modifier = Modifier.height(12.dp))
        CurrentInstructionView(
            disassemblerState = disassemblerState
        )
        Spacer(modifier = Modifier.height(12.dp))
        ZeroPageView(zeroPageState = zeroPageState)
    }
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
    mainRegistersState: MutableState<List<UInt>>,
    mainFlagsState: MutableState<List<Boolean>>
) {
    Column {
        StatusFlagsView(
            mainFlagsState = mainFlagsState
        )

        RegisterView("PC:", mainRegistersState.value[0].to4DigitHexString())
        RegisterView("A:", mainRegistersState.value[1].to2DigitHexString())
        RegisterView("X:", mainRegistersState.value[2].to2DigitHexString())
        RegisterView("Y:", mainRegistersState.value[3].to2DigitHexString())
        RegisterView("SP:", mainRegistersState.value[4].to4DigitHexString())
    }
}

@Composable
fun StatusFlagsView(
    mainFlagsState: MutableState<List<Boolean>>
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

       FlagView("N", mainFlagsState.value[0])
       FlagView("V", mainFlagsState.value[1])
       FlagView("-", mainFlagsState.value[2])
       FlagView("B", mainFlagsState.value[3])
       FlagView("D", mainFlagsState.value[4])
       FlagView("I", mainFlagsState.value[5])
       FlagView("Z", mainFlagsState.value[6])
       FlagView("C", mainFlagsState.value[7])
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
    zeroPageState: MutableState<List<UByte>>
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.White)
            .padding(5.dp, 5.dp, 5.dp, 0.dp)
    ) {
        for (index in 0..255 step 16) {
            Row {
                for (j in index..index + 15) {
                    Text(
                        text = zeroPageState.value[j].toUInt().to2DigitHexString(),
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

@Composable
fun CurrentInstructionView(
    disassemblerState: MutableState<String>
) {
    Text(
        text = disassemblerState.value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
fun PatternTablesView(
    patternTableState: MutableState<List<UInt>>,
) {
    Row(
    ) {
        PatternTable(
            section = patternTableState.value.take(16384),
            modifier = Modifier
                .background(Color.Black))
        Spacer(modifier = Modifier.width(5.dp))
        PatternTable(
            section = patternTableState.value.takeLast(16384),
            modifier = Modifier
                .background(Color.Black)
        )
    }
}

@Composable
fun PatternTable(
    section: List<UInt>,
    modifier: Modifier
) {
    Box(
        modifier = modifier
    ) {
        Spacer(
            Modifier
                .size(300.dp)
                .drawWithCache {
                    val pixelWidth = floor(size.width / 128f)
                    val pixelHeight = floor(size.height / 128f)
                    val pixelSize = Size(width = pixelWidth, height = pixelHeight)

                    onDrawWithContent {

                        for ((i, row) in section.chunked(1024).withIndex()) {
                            for ((j, tile) in row.chunked(64).withIndex()) {
                                for ((k, tileRow) in tile.chunked(8).withIndex()) {
                                    for ((l, pixel) in tileRow.withIndex()) {
                                        drawRect(
                                            topLeft = Offset(
                                                (pixelWidth * l) + (j * 8 * pixelWidth),
                                                (pixelHeight * k) + (i * 8 * pixelHeight),
                                            ),
                                            size = pixelSize,
                                            color = when (pixel) {
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
                        }
                    }
                },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaletteColorView(
    paletteColorsState: MutableState<List<UInt>>,
    forcePaletteSelect: (UInt) -> Unit
) {
    Row {
        for ((index, palette) in paletteColorsState.value.chunked(4).withIndex()) {
            Canvas(
                modifier = Modifier
                    .height(16.dp)
                    .width(64.dp)
                    .onClick { forcePaletteSelect(index.toUInt()) },
                onDraw = {
                    val pixelWidth = floor(size.width / 4f)
                    val pixelSize = Size(width = pixelWidth, height = size.height)
                    for ((index, color) in palette.withIndex()) {
                        drawRect(
                            topLeft = Offset(
                                x = index * pixelWidth,
                                y = 0f
                            ),
                            size = pixelSize,
                            color = when (color) {
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
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
    }
}

