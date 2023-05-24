package Bus

import CPU.CPU6502
import Cartridge.Cartridge
import PPU.PPU2C02

/**
 * The connection to all other pieces of the system.
 * @param ram is an array of memory addresses.
 */
@ExperimentalUnsignedTypes
class Bus (
    val cpu: CPU6502,
    var ram: UByteArray,
    var ppu: PPU2C02,
    var mapper: PPU2C02
) {

    init {
        registerAllComponents()
    }

    private fun registerAllComponents() {
        cpu.bus = this
        ppu.bus = this
    }


    fun readAddress(address: UShort): UByte {
        if (address < 0x2000u) return ram[address.toInt()]

        if (address < 0x4000u) {
            val ppuRegisterAddress = (address.mod(0x0008u) + 0x2000u).toUShort()
            when (ppuRegisterAddress) {
                (0x2000u).toUShort() -> return ppu.ppuCtrlRegister
                (0x2001u).toUShort() -> return ppu.ppuMaskRegister
                (0x2002u).toUShort() -> return ppu.ppuStatusRegister
                (0x2003u).toUShort() -> return ppu.oamAddrRegister
                (0x2004u).toUShort() -> return ppu.oamDataRegister
                (0x2005u).toUShort() -> return ppu.ppuScrollRegister
                (0x2006u).toUShort() -> return ppu.ppuAddrRegister
                (0x2007u).toUShort() -> return ppu.ppuDataRegister
            }
        }


        return 0x01u
    }

    fun writeToAddress(address: UShort, data: UByte) {
        if (address < 0x2000u) {
            ram[address.toInt()] = data
            ram[(address + 0x0800u).mod(0x2000u).toInt()] = data
            ram[(address + 0x1000u).mod(0x2000u).toInt()] = data
            ram[(address + 0x1800u).mod(0x2000u).toInt()] = data
            return
        }

        if (address < 0x4000u) {
            val ppuRegisterAddress = (address.mod(0x0008u) + 0x2000u).toUShort()
            when (ppuRegisterAddress) {
                (0x2000u).toUShort() -> ppu.ppuCtrlRegister = data
                (0x2001u).toUShort() -> ppu.ppuMaskRegister = data
                (0x2002u).toUShort() -> ppu.ppuStatusRegister = data
                (0x2003u).toUShort() -> ppu.oamAddrRegister = data
                (0x2004u).toUShort() -> ppu.oamDataRegister = data
                (0x2005u).toUShort() -> ppu.ppuScrollRegister = data
                (0x2006u).toUShort() -> ppu.ppuAddrRegister = data
                (0x2007u).toUShort() -> ppu.ppuDataRegister = data
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
            TODO("Game Cartridge memory map.")
        }

    }


    fun clearRam() {
        val size = this.ram.size
        this.ram = UByteArray(size)
    }
}