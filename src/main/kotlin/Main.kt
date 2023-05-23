import Bus.Bus
import CPU.CPU6502
import PPU.PPU2C02

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    val cpu = CPU6502()
    val ppu = PPU2C02()
    Bus(cpu, UByteArray(2000), ppu)
}

