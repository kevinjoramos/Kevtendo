package UI

import Bus.Bus
import CPU.CPU6502
import Cartridge.Cartridge
import Cartridge.MapperZero
import PPU.PPU2C02
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@ExperimentalUnsignedTypes
class NesEmulator {
    private val projectRootPath = System.getProperty("user.dir")
    private val ramSize = 2048

    private val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"
    private var bus = Bus(pathToGame, ramSize)

    var cpuState by mutableStateOf(getCurrentCPUState())

    private var isRunning = false
    var isPaused = false

    fun start() {
        while (isRunning) {
            bus.cpu.run()
        }
    }

    fun step() {
        bus.cpu.run()
        cpuState = getCurrentCPUState()
    }

    fun reset() {

    }

    private fun getCurrentCPUState(): CPUState = CPUState(
        bus.cpu.programCounter.toString(16),
        bus.cpu.stackPointer.toString(16),
        bus.cpu.accumulator.toString(16),
        bus.cpu.xRegister.toString(16),
        bus.cpu.yRegister.toString(16),
        bus.cpu.negativeFlag,
        bus.cpu.overflowFlag,
        bus.cpu.extraFlag,
        bus.cpu.breakFlag,
        bus.cpu.decimalFlag,
        bus.cpu.interruptDisableFlag,
        bus.cpu.zeroFlag,
        bus.cpu.carryFlag,
    )
}