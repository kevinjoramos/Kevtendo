package ui

import bus.Bus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
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

    var gameViewUiState by mutableStateOf(GameViewUiState())
    var patternTableState by mutableStateOf("")
    var mainCpuViewState by mutableStateOf(updateMainCpuViewState())
    var disassemblerState by mutableStateOf("f")
    var zeroPageViewState by mutableStateOf(updateZeroPageViewState())



    private var isRunning = false
    var isPaused = false

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        GlobalScope.launch {
            try {
                isRunning = true
                while (isRunning) {
                    step()
                    mainCpuViewState = updateMainCpuViewState()
                    zeroPageViewState = updateZeroPageViewState()
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
        mainCpuViewState = updateMainCpuViewState()
        zeroPageViewState = updateZeroPageViewState()
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

    private fun updateMainCpuViewState(): MainCpuViewState = MainCpuViewState(
        listOf(
            "PC: ${bus.cpu.programCounter.to4DigitHexString()}",
            "A: ${bus.cpu.accumulator.to2DigitHexString()}",
            "X: ${bus.cpu.xRegister.to2DigitHexString()}",
            "Y: ${bus.cpu.yRegister.to2DigitHexString()}",
            "SP: ${bus.cpu.stackPointer.to4DigitHexString()}"
        ),
        listOf(
            Pair("N", bus.cpu.negativeFlag),
            Pair("V", bus.cpu.overflowFlag),
            Pair("-", bus.cpu.extraFlag),
            Pair("B", bus.cpu.breakFlag),
            Pair("D", bus.cpu.decimalFlag),
            Pair("I", bus.cpu.interruptDisableFlag),
            Pair("Z", bus.cpu.zeroFlag),
            Pair("C", bus.cpu.carryFlag),
        )
    )

    private fun updateZeroPageViewState(): ZeroPageViewState =
        ZeroPageViewState(
            bus.ram
                .sliceArray(0..255)
                .map { it.to2DigitHexString() }
                .chunked(16)
        )

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
}