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
class NesEmulator {
    private val projectRootPath = System.getProperty("user.dir")
    private val ramSize = 0x2000

    //private val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"
    private val pathToGame = "$projectRootPath/src/main/kotlin/games/nestest.nes"
    private var bus = Bus(pathToGame, ramSize)

    var cpuState by mutableStateOf(getCurrentCPUState())
    var currentInstruction by mutableStateOf("")
    var zeroPageState by mutableStateOf(Array<Array<String>>(16) { Array<String>(16) {"00"} })
    //var instructionSlidingWindowState by mutableStateOf(getCurrentInstructionsSlidingWindowState())


    private var isRunning = false
    var isPaused = false

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        GlobalScope.launch {
            try {
                isRunning = true
                while (isRunning) {
                    step()
                    cpuState = getCurrentCPUState()
                    zeroPageState = getCurrentZeroPageState()
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
        cpuState = getCurrentCPUState()
        zeroPageState = getCurrentZeroPageState()
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

    private fun getCurrentCPUState(): CPUState = CPUState(
        bus.cpu.programCounter.to4DigitHexString(),
        bus.cpu.stackPointer.to4DigitHexString(),
        bus.cpu.accumulator.to2DigitHexString(),
        bus.cpu.xRegister.to2DigitHexString(),
        bus.cpu.yRegister.to2DigitHexString(),
        bus.cpu.negativeFlag,
        bus.cpu.overflowFlag,
        bus.cpu.extraFlag,
        bus.cpu.breakFlag,
        bus.cpu.decimalFlag,
        bus.cpu.interruptDisableFlag,
        bus.cpu.zeroFlag,
        bus.cpu.carryFlag,
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


    private fun getCurrentZeroPageState(): Array<Array<String>> {
        val zeroPageMatrix = bus.ram
            .sliceArray(0..255)
            .map { it.to2DigitHexString() }
            .chunked(16)
            .map { it.toTypedArray() }
            .toTypedArray()

        return zeroPageMatrix
    }

}