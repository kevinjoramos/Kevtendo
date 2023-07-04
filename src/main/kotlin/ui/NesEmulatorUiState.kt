package ui

import bus.Bus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import util.Logger
import util.to2DigitHexString
import util.to4DigitHexString

@ExperimentalUnsignedTypes
class NesEmulatorUiState {
    private val projectRootPath = System.getProperty("user.dir")
    private val ramSize = 0x2000

    //private val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"
    private val pathToGame = "$projectRootPath/src/main/kotlin/games/nestest.nes"
    private var bus = Bus(pathToGame, ramSize)

    private val _gameViewUiState = MutableStateFlow(GameViewUiState())
    val gameViewUiState = _gameViewUiState.asStateFlow()

    //val patternTableState by mutableStateOf("")
    private val _mainCpuViewState = MutableStateFlow(MainCpuViewState())
    val mainCpuViewState = _mainCpuViewState.asStateFlow()

    //val disassemblerState by mutableStateOf("f")
    private val _zeroPageViewState = MutableStateFlow(ZeroPageViewState())
    val zeroPageViewState = _zeroPageViewState.asStateFlow()

    private var isRunning = false
    var isPaused = false

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        GlobalScope.launch {
            try {
                isRunning = true
                while (isRunning) {
                    step()
                    updateMainCpuViewState()
                    updateZeroPageViewState()
                    generateNoise()
                }
                
            } catch (e: Exception) {
                isRunning = false
                println(e.toString())
                Logger.writeLogsToFile()
                println(bus.cpu.readAddress(0x02FFu))
                println(bus.cpu.readAddress(0x0300u))
            }
        }
    }

    fun step() {
        val programCounterValue = bus.cpu.programCounter
        bus.cpu.run()
        updateMainCpuViewState()
        updateZeroPageViewState()
        //currentInstruction = bus.cpu.disassembledProgram[programCounterValue] ?: "i dunno"


        //instructionSlidingWindowState = getCurrentInstructionsSlidingWindowState()
    }

    fun reset() {
        this.bus = Bus(pathToGame, ramSize)
    }

    fun stop() {
        isRunning = false
        Logger.writeLogsToFile()
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
        val zeroPage = bus.ram
            .slice(0..255)
            .map { it.to2DigitHexString() }
            .chunked(16).map { it.toImmutableList() }.toImmutableList()

        _zeroPageViewState.update {
            it.copy(zeroPage = zeroPage)
        }
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
}