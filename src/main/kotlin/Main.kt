import Bus.Bus
import CPU.CPU6502
import Cartridge.Cartridge
import Cartridge.MapperZero
import PPU.PPU2C02

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    val projectRootPath = System.getProperty("user.dir")
    val pathSeparator = System.getProperty("file.separator")
    val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"

    val cpu = CPU6502()
    val ppu = PPU2C02()
    val cartridge = Cartridge(pathToGame)
    val mapper = MapperZero(cartridge)
    Bus(cpu, UByteArray(2048), ppu, mapper)

    while (true) {
        cpu.run()
    }
}

