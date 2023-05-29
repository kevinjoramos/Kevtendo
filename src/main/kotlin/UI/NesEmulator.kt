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

    private val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"
    private val cpu = CPU6502()

    private val ppu = PPU2C02()
    private val cartridge = Cartridge(pathToGame)
    private val mapper = MapperZero(cartridge)
    private var bus = Bus(cpu, UByteArray(2048), ppu, mapper)

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
        cpu.programCounter.toString(16),
        cpu.stackPointer.toString(16),
        cpu.accumulator.toString(16),
        cpu.xRegister.toString(16),
        cpu.yRegister.toString(16),
        cpu.negativeFlag,
        cpu.overflowFlag,
        cpu.extraFlag,
        cpu.breakFlag,
        cpu.decimalFlag,
        cpu.interruptDisableFlag,
        cpu.zeroFlag,
        cpu.carryFlag,
    )
}