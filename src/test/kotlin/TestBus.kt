package Bus

import CPU.CPU6502
import Cartridge.Cartridge
import Cartridge.Mapper
import Cartridge.MapperZero
import PPU.PPU2C02
import mediator.Event
import mediator.Mediator

/**
 * The connection to all other pieces of the system.
 * @param ram is an array of memory addresses.
 */
@ExperimentalUnsignedTypes
class TestBus(ramSize: Int) : Mediator {
    val cpu = CPU6502(this)
    var ram = UByteArray(ramSize)

    override fun notify(event: Event) {
        TODO("Not yet implemented")
    }

    override fun readAddress(address: UShort): UByte {
        return ram[address.toInt()]
    }

    override fun writeToAddress(address: UShort, data: UByte) {
        ram[address.toInt()] = data
    }


    fun clearRam() {
        val size = this.ram.size
        this.ram = UByteArray(size)
    }
}