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
class Bus(cartridgePath: String, ramSize: Int) : Mediator {
    var ram = UByteArray(ramSize)
    var ppu = PPU2C02(this)
    private var mapper = MapperZero(Cartridge(cartridgePath), this)
    val cpu = CPU6502(this)

    override fun notify(event: Event) {
        TODO("Not yet implemented")
    }

    override fun readAddress(address: UShort): UByte {


        if (address < 0x2000u) return ram[address.toInt()]

        if (address < 0x4000u) {
            val ppuRegisterAddress = (address.mod(0x0008u) + 0x2000u).toUShort()
            when (ppuRegisterAddress) {
                (0x2002u).toUShort() -> return ppu.readStatusRegister()
                (0x2003u).toUShort() -> return ppu.oamAddressRegister
                (0x2004u).toUShort() -> return ppu.oamDataRegister
                (0x2005u).toUShort() -> return ppu.scrollRegister
                (0x2007u).toUShort() -> return ppu.readDataRegister()
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
        println("WRITE data: ${data.toString(16)} to Address: ${address.toString(16)}")
        if (address < 0x2000u) {
            ram[address.toInt()] = data
            ram[(address + 0x0800u).mod(0x2000u).toUShort().toInt()] = data
            ram[(address + 0x1000u).mod(0x2000u).toUShort().toInt()] = data
            ram[(address + 0x1800u).mod(0x2000u).toUShort().toInt()] = data
            return
        }

        if (address < 0x4000u) {
            val ppuRegisterAddress = (address.mod(0x0008u) + 0x2000u).toUShort()
            when (ppuRegisterAddress) {
                (0x2000u).toUShort() -> ppu.writeToControlRegister(data)
                (0x2001u).toUShort() -> ppu.writeToMaskRegister(data)
                (0x2003u).toUShort() -> ppu.oamAddressRegister = data
                (0x2004u).toUShort() -> ppu.oamDataRegister = data
                (0x2005u).toUShort() -> ppu.scrollRegister = data
                (0x2006u).toUShort() -> ppu.writeToAddressRegister(data)
                (0x2007u).toUShort() -> ppu.writeToDataRegister(data)
            }
            return
        }

        if (address < 0x4018u) {
            TODO("APU and IO memory map not implemented")
        }

        if (address < 0x4020u) {
            TODO("APU and IO disabled memory map not implemented")
        }

        if (address <= 0xFFFFu) {
            mapper.writeToCartridgeAddress(address, data)
        }

    }


    fun clearRam() {
        val size = this.ram.size
        this.ram = UByteArray(size)
    }
}