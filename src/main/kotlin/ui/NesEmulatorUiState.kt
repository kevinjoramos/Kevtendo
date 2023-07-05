package ui

import androidx.compose.runtime.collectAsState
import bus.Bus
import androidx.compose.ui.graphics.Color
import bus.Ram
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

    //val disassemblerState by mutableStateOf("f")
    //private val _zeroPageViewState = MutableStateFlow(ZeroPageViewState())
    //val zeroPageViewState = _zeroPageViewState.asStateFlow()

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
        Logger.writeLogsToFile()
    }

    private fun runSystem() {
        GlobalScope.launch {

            while (isRunning) {

                val elapsedTime = measureTimeMillis {
                    executeMainCycle()
                }

                println(elapsedTime)

                systemClock = 0
            }

            /*} catch (e: Exception) {
                isRunning = false
                println(e.toString())
                Logger.writeLogsToFile()
                println(bus.cpu.readAddress(0x02FFu))
                println(bus.cpu.readAddress(0x0300u))
            }*/
        }
    }

    private fun executeMainCycle() {

        while (systemClock < TOTAL_SYSTEM_CYCLES_PER_FRAME) {
            // execute one cycle of ppu (outputs one pixel)
            bus.ppu.run()

            // execute one cycle of cpu (1 cpu cycle for every 3 ppu cycles).
            if (systemClock % 3 == 0) {
                bus.cpu.run()
                updateMainCpuViewState()
                //updateZeroPageViewState()
            }

            // At this point visible scan lines are complete.
            if (systemClock == FIRST_CYCLE_AFTER_RENDER) {
                generateNoise()
            }

            systemClock++
        }
    }

    private fun updateGameViewState() {
        val pair = Pair(1, 2)
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
            List(256) {
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