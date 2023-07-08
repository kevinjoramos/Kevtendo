package ui

import bus.Bus
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import util.Logger
import util.to2DigitHexString
import util.to4DigitHexString
import kotlin.system.measureTimeMillis

@ExperimentalUnsignedTypes
class NesEmulatorUiState {
    private val projectRootPath = System.getProperty("user.dir")

    //private val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"
    private val pathToGame = "$projectRootPath/src/main/kotlin/games/nestest.nes"
    private var bus = Bus(pathToGame)

    private val _gameViewUiState = MutableStateFlow(GameViewUiState())
    val gameViewUiState = _gameViewUiState.asStateFlow()

    //val patternTableState by mutableStateOf("")
    private val _mainCpuViewState = MutableStateFlow(MainCpuViewState())
    val mainCpuViewState = _mainCpuViewState.asStateFlow()

    val programCounterViewState = bus.cpu.programCounterState
    val accumulatorViewState = bus.cpu.accumulatorState
    val xRegisterViewState = bus.cpu.xRegisterState
    val yRegisterViewState = bus.cpu.yRegisterState
    val stackPointerViewState = bus.cpu.stackPointerState
    val negativeFlagViewState = bus.cpu.negativeFlagState
    val overflowFlagViewState = bus.cpu.overflowFlagState
    val extraFlagViewState = bus.cpu.extraFlagState
    val breakFlagViewState = bus.cpu.breakFlagState
    val decimalFlagViewState = bus.cpu.decimalFlagState
    val interruptDisableViewState = bus.cpu.interruptDisableFlagState
    val zeroFlagViewState = bus.cpu.zeroFlagState
    val carryFlagViewState = bus.cpu.carryFlagState

    val instructionViewState1 = bus.cpu.instructionState1
    val instructionViewState2 = bus.cpu.instructionState2
    val instructionViewState3 = bus.cpu.instructionState3
    val instructionViewState4 = bus.cpu.instructionState4
    val instructionViewState5 = bus.cpu.instructionState5
    val instructionViewState6 = bus.cpu.instructionState6

    val zeroPageRow1ViewState = bus.ram.zeroPageRow1StateFlow
    val zeroPageRow2ViewState = bus.ram.zeroPageRow2StateFlow
    val zeroPageRow3ViewState = bus.ram.zeroPageRow3StateFlow
    val zeroPageRow4ViewState = bus.ram.zeroPageRow4StateFlow
    val zeroPageRow5ViewState = bus.ram.zeroPageRow5StateFlow
    val zeroPageRow6ViewState = bus.ram.zeroPageRow6StateFlow
    val zeroPageRow7ViewState = bus.ram.zeroPageRow7StateFlow
    val zeroPageRow8ViewState = bus.ram.zeroPageRow8StateFlow
    val zeroPageRow9ViewState = bus.ram.zeroPageRow9StateFlow
    val zeroPageRow10ViewState = bus.ram.zeroPageRow10StateFlow
    val zeroPageRow11ViewState = bus.ram.zeroPageRow11StateFlow
    val zeroPageRow12ViewState = bus.ram.zeroPageRow12StateFlow
    val zeroPageRow13ViewState = bus.ram.zeroPageRow13StateFlow
    val zeroPageRow14ViewState = bus.ram.zeroPageRow14StateFlow
    val zeroPageRow15ViewState = bus.ram.zeroPageRow15StateFlow
    val zeroPageRow16ViewState = bus.ram.zeroPageRow16StateFlow

    private var isRunning = false
    var isPaused = false

    private var systemClock = 0

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        isRunning = true
        runSystem()
    }

    fun step() {
        val programCounterValue = bus.cpu.programCounter
        bus.cpu.run()
        updateMainCpuViewState()
        updateZeroPageViewState()


        //instructionSlidingWindowState = getCurrentInstructionsSlidingWindowState()
    }

    fun reset() {
        this.bus = Bus(pathToGame)
    }

    fun stop() {
        isRunning = false
    }

    private fun runSystem() {
        val emulatorJob = GlobalScope.launch {

            while (isRunning) {

                val elapsedTime = measureTimeMillis {
                    executeMainCycle()
                }

                println(elapsedTime)

                systemClock = 0
            }

            Logger.writeLogsToFile()
        }



    }

    private fun executeMainCycle() {

        while (systemClock < TOTAL_SYSTEM_CYCLES_PER_FRAME) {
            // execute one cycle of ppu (outputs one pixel)
            bus.ppu.run()

            // execute one cycle of cpu (1 cpu cycle for every 3 ppu cycles).
            if (systemClock % 3 == 0) {
                bus.cpu.run()
            }

            // At this point visible scan lines are complete.
            if (systemClock == FIRST_CYCLE_AFTER_RENDER) {
                //generateNoise()
                updateGameViewState()
            }

            systemClock++
        }
    }

    private fun updateGameViewState() {
        val pixelScreen = bus.ppu.frameBuffer.map { row ->
            row.map { pixel ->
                getColorFromValue(pixel.toUInt() )
            }.toImmutableList()
        }.toImmutableList()

        _gameViewUiState.update {
            it.copy(pixelScreen = pixelScreen)
        }
    }

    private fun getColorFromValue(colorValue: UInt): Color {
        return when (colorValue) {
            0x00u -> COLOR_00
            0x01u -> COLOR_01
            0x02u -> COLOR_02
            0x03u -> COLOR_03
            0x04u -> COLOR_04
            0x05u -> COLOR_05
            0x06u -> COLOR_06
            0x07u -> COLOR_07
            0x08u -> COLOR_08
            0x09u -> COLOR_09
            0x0Au -> COLOR_0A
            0x0Bu -> COLOR_0B
            0x0Cu -> COLOR_0C
            0x0Du -> COLOR_0D
            0x0Eu -> COLOR_0E
            0x0Fu -> COLOR_0F

            0x10u -> COLOR_10
            0x11u -> COLOR_11
            0x12u -> COLOR_12
            0x13u -> COLOR_13
            0x14u -> COLOR_14
            0x15u -> COLOR_15
            0x16u -> COLOR_16
            0x17u -> COLOR_17
            0x18u -> COLOR_18
            0x19u -> COLOR_19
            0x1Au -> COLOR_1A
            0x1Bu -> COLOR_1B
            0x1Cu -> COLOR_1C
            0x1Du -> COLOR_1D
            0x1Eu -> COLOR_1E
            0x1Fu -> COLOR_1F

            0x20u -> COLOR_20
            0x21u -> COLOR_21
            0x22u -> COLOR_22
            0x23u -> COLOR_23
            0x24u -> COLOR_24
            0x25u -> COLOR_25
            0x26u -> COLOR_26
            0x27u -> COLOR_27
            0x28u -> COLOR_28
            0x29u -> COLOR_29
            0x2Au -> COLOR_2A
            0x2Bu -> COLOR_2B
            0x2Cu -> COLOR_2C
            0x2Du -> COLOR_2D
            0x2Eu -> COLOR_2E
            0x2Fu-> COLOR_1F

            0x30u -> COLOR_30
            0x31u -> COLOR_31
            0x32u -> COLOR_32
            0x33u -> COLOR_33
            0x34u -> COLOR_34
            0x35u -> COLOR_35
            0x36u -> COLOR_36
            0x37u -> COLOR_37
            0x38u -> COLOR_38
            0x39u -> COLOR_39
            0x3Au -> COLOR_3A
            0x3Bu -> COLOR_3B
            0x3Cu -> COLOR_3C
            0x3Du -> COLOR_3D
            0x3Eu -> COLOR_3E
            else -> COLOR_3F
        }
    }

    private fun updateMainCpuViewState() {
        val registers = listOf(
            "PC: ${bus.cpu.programCounter.to4DigitHexString()}",
            "A: ${bus.cpu.accumulator.to2DigitHexString()}",
            "X: ${bus.cpu.xRegister.to2DigitHexString()}",
            "Y: ${bus.cpu.yRegister.to2DigitHexString()}",
            "SP: ${bus.cpu.stackPointer.to4DigitHexString()}"
        )

        val flags = listOf(
            Pair("N", bus.cpu.negativeFlag),
            Pair("V", bus.cpu.overflowFlag),
            Pair("-", bus.cpu.extraFlag),
            Pair("B", bus.cpu.breakFlag),
            Pair("D", bus.cpu.decimalFlag),
            Pair("I", bus.cpu.interruptDisableFlag),
            Pair("Z", bus.cpu.zeroFlag),
            Pair("C", bus.cpu.carryFlag),
        )

        _mainCpuViewState.update {
            it.copy(registers = registers.toImmutableList(), flags = flags.toImmutableList())
        }
    }

    private fun updateZeroPageViewState() {

    }

    private fun getCurrentInstructionsSlidingWindowState(): List<String> {
        val instructionList = mutableListOf<String>()

        if (bus.cpu.programCounter < 15u) {
           for (index in 0..16) {
               instructionList.add( "${(index).toString(16)}: ")
           }

           return instructionList
        }

        for (index in (bus.cpu.programCounter - 8u)..(bus.cpu.programCounter + 7u)) {
            instructionList.add( "${(index).toString(16)}: ")
        }

        return instructionList
    }

    private fun generateNoise() {
        val pixelScreen = List(240) {
            List(257) {
                if ((0..1).random() == 0) Color.Black else Color.White
            }.toImmutableList()
        }.toImmutableList()

        _gameViewUiState.update {
            it.copy(pixelScreen = pixelScreen)
        }
    }

    companion object {
        const val TOTAL_SYSTEM_CYCLES_PER_FRAME = 89_342
        const val FIRST_CYCLE_AFTER_RENDER = 81_841
        //const val FIRST_CYCLE_AFTER_RENDER = 82_080

        const val MILLISECONDS_PER_FRAME = 17
    }
}