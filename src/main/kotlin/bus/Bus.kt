package bus

import cpu.CPU6502
import cartridge.Cartridge
import cartridge.MapperZero
import ppu.PPU2C02
import mediator.Event
import mediator.Mediator

/**
 * The connection to all other pieces of the system.
 * @param ram is an array of memory addresses.
 */
@ExperimentalUnsignedTypes
class Bus(
    cartridgePath: String
) : Mediator {
    var ram = Ram()
    var ppu = PPU2C02(this)
    private var mapper = MapperZero(Cartridge(cartridgePath), this)
    val cpu = CPU6502(this)

    override fun notify(event: Event) {
        TODO("Not yet implemented")
    }

    override fun readAddress(address: UShort): UByte {


        if (address < 0x2000u) return ram.memory[address.toInt()]

        if (address < 0x4000u) {
            val ppuRegisterAddress = (address.mod(0x0008u) + 0x2000u).toUShort()
            when (ppuRegisterAddress) {
                (0x2000u).toUShort() -> return ppu.readControllerRegister().toUByte()
                (0x2001u).toUShort() -> return ppu.readMaskRegister().toUByte()
                (0x2002u).toUShort() -> return ppu.readStatusRegister().toUByte()
                (0x2003u).toUShort() -> return ppu.readOamAddressRegister().toUByte()
                (0x2004u).toUShort() -> return ppu.readOamDataRegister().toUByte()
                (0x2005u).toUShort() -> return ppu.readScrollRegister().toUByte()
                (0x2006u).toUShort() -> return ppu.readAddressRegister().toUByte()
                (0x2007u).toUShort() -> return ppu.readDataRegister().toUByte()
            }
        }

        if (address < 0x4018u) {
            return 0x00u // TODO("APU and IO memory map not implemented")
        }

        if (address < 0x4020u) {
            return 0x00u //TODO("APU and IO disabled memory map not implemented")
        }

        if (address < 0x6000u) {
            return 0x00u
        }

        if (address <= 0xFFFFu) {
            return mapper.readCartridgeAddress(address)
        }


        return 0x00u
    }

    override fun writeToAddress(address: UShort, data: UByte) {
        if (address < 0x2000u) {
            ram.writeToMemory(address, data)
            ram.writeToMemory((address + 0x0800u).mod(0x2000u).toUShort(), data)
            ram.writeToMemory((address + 0x1000u).mod(0x2000u).toUShort(), data)
            ram.writeToMemory((address + 0x1800u).mod(0x2000u).toUShort(), data)

            return
        }

        if (address < 0x4000u) {
            val ppuRegisterAddress = (address.mod(0x0008u) + 0x2000u).toUShort()
            when (ppuRegisterAddress) {
                (0x2000u).toUShort() -> ppu.writeToControllerRegister(data.toUInt())
                (0x2001u).toUShort() -> ppu.writeToMaskRegister(data.toUInt())
                (0x2002u).toUShort() -> ppu.writeToStatusRegister(data.toUInt())
                (0x2003u).toUShort() -> ppu.writeToOamAddressRegister(data.toUInt())
                (0x2004u).toUShort() -> ppu.writeToOamDataRegister(data.toUInt())
                (0x2005u).toUShort() -> ppu.writeToScrollRegister(data.toUInt())
                (0x2006u).toUShort() -> ppu.writeToAddressRegister(data.toUInt())
                (0x2007u).toUShort() -> ppu.writeToDataRegister(data.toUInt())
            }
            return
        }

        if (address < 0x4018u) {
            return
        }

        if (address < 0x4020u) {
            return
        }

        if (address <= 0xFFFFu) {
            mapper.writeToCartridgeAddress(address, data)
        }

    }

    fun reset() {
        cpu.reset()
    }
}