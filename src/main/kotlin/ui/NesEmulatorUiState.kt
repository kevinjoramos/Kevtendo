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

    var programCounterViewState = bus.cpu.programCounterState
    var accumulatorViewState = bus.cpu.accumulatorState
    var xRegisterViewState = bus.cpu.xRegisterState
    var yRegisterViewState = bus.cpu.yRegisterState
    var stackPointerViewState = bus.cpu.stackPointerState
    var negativeFlagViewState = bus.cpu.negativeFlagState
    var overflowFlagViewState = bus.cpu.overflowFlagState
    var extraFlagViewState = bus.cpu.extraFlagState
    var breakFlagViewState = bus.cpu.breakFlagState
    var decimalFlagViewState = bus.cpu.decimalFlagState
    var interruptDisableViewState = bus.cpu.interruptDisableFlagState
    var zeroFlagViewState = bus.cpu.zeroFlagState
    var carryFlagViewState = bus.cpu.carryFlagState
    var instructionViewState1 = bus.cpu.instructionState1
    var instructionViewState2 = bus.cpu.instructionState2
    var instructionViewState3 = bus.cpu.instructionState3
    var instructionViewState4 = bus.cpu.instructionState4
    var instructionViewState5 = bus.cpu.instructionState5
    var instructionViewState6 = bus.cpu.instructionState6
    var zeroPageRow1ViewState = bus.ram.zeroPageRow1StateFlow
    var zeroPageRow2ViewState = bus.ram.zeroPageRow2StateFlow
    var zeroPageRow3ViewState = bus.ram.zeroPageRow3StateFlow
    var zeroPageRow4ViewState = bus.ram.zeroPageRow4StateFlow
    var zeroPageRow5ViewState = bus.ram.zeroPageRow5StateFlow
    var zeroPageRow6ViewState = bus.ram.zeroPageRow6StateFlow
    var zeroPageRow7ViewState = bus.ram.zeroPageRow7StateFlow
    var zeroPageRow8ViewState = bus.ram.zeroPageRow8StateFlow
    var zeroPageRow9ViewState = bus.ram.zeroPageRow9StateFlow
    var zeroPageRow10ViewState = bus.ram.zeroPageRow10StateFlow
    var zeroPageRow11ViewState = bus.ram.zeroPageRow11StateFlow
    var zeroPageRow12ViewState = bus.ram.zeroPageRow12StateFlow
    var zeroPageRow13ViewState = bus.ram.zeroPageRow13StateFlow
    var zeroPageRow14ViewState = bus.ram.zeroPageRow14StateFlow
    var zeroPageRow15ViewState = bus.ram.zeroPageRow15StateFlow
    var zeroPageRow16ViewState = bus.ram.zeroPageRow16StateFlow

    private var isRunning = false
    var isPaused = false

    private var systemClock = 0

    private var emulatorProcess: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        isRunning = true
        runSystem()
    }

    fun step() {
        bus.cpu.run()


        //instructionSlidingWindowState = getCurrentInstructionsSlidingWindowState()
    }

    fun reset() {
        isRunning = false
        emulatorProcess?.cancel()
        this.bus = Bus(pathToGame)
        programCounterViewState = bus.cpu.programCounterState
        accumulatorViewState = bus.cpu.accumulatorState
        xRegisterViewState = bus.cpu.xRegisterState
        yRegisterViewState = bus.cpu.yRegisterState
        stackPointerViewState = bus.cpu.stackPointerState
        negativeFlagViewState = bus.cpu.negativeFlagState
        overflowFlagViewState = bus.cpu.overflowFlagState
        extraFlagViewState = bus.cpu.extraFlagState
        breakFlagViewState = bus.cpu.breakFlagState
        decimalFlagViewState = bus.cpu.decimalFlagState
        interruptDisableViewState = bus.cpu.interruptDisableFlagState
        zeroFlagViewState = bus.cpu.zeroFlagState
        carryFlagViewState = bus.cpu.carryFlagState

        instructionViewState1 = bus.cpu.instructionState1
        instructionViewState2 = bus.cpu.instructionState2
        instructionViewState3 = bus.cpu.instructionState3
        instructionViewState4 = bus.cpu.instructionState4
        instructionViewState5 = bus.cpu.instructionState5
        instructionViewState6 = bus.cpu.instructionState6

        zeroPageRow1ViewState = bus.ram.zeroPageRow1StateFlow
        zeroPageRow2ViewState = bus.ram.zeroPageRow2StateFlow
        zeroPageRow3ViewState = bus.ram.zeroPageRow3StateFlow
        zeroPageRow4ViewState = bus.ram.zeroPageRow4StateFlow
        zeroPageRow5ViewState = bus.ram.zeroPageRow5StateFlow
        zeroPageRow6ViewState = bus.ram.zeroPageRow6StateFlow
        zeroPageRow7ViewState = bus.ram.zeroPageRow7StateFlow
        zeroPageRow8ViewState = bus.ram.zeroPageRow8StateFlow
        zeroPageRow9ViewState = bus.ram.zeroPageRow9StateFlow
        zeroPageRow10ViewState = bus.ram.zeroPageRow10StateFlow
        zeroPageRow11ViewState = bus.ram.zeroPageRow11StateFlow
        zeroPageRow12ViewState = bus.ram.zeroPageRow12StateFlow
        zeroPageRow13ViewState = bus.ram.zeroPageRow13StateFlow
        zeroPageRow14ViewState = bus.ram.zeroPageRow14StateFlow
        zeroPageRow15ViewState = bus.ram.zeroPageRow15StateFlow
        zeroPageRow16ViewState = bus.ram.zeroPageRow16StateFlow

        start()
    }

    fun stop() {
        isRunning = false
    }

    private fun runSystem() {
        emulatorProcess = GlobalScope.launch {

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