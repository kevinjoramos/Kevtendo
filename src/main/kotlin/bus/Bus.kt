package bus

import cpu.CPU6502
import cartridge.Cartridge
import cartridge.MapperZero
import controller.GameController
import ppu.PPU2C02
import mediator.Event
import mediator.Mediator
import mediator.Sender

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
    var mapper = MapperZero(Cartridge(cartridgePath), this)
    val cpu = CPU6502(this)

    val controller1 = GameController()
    val controller2 = GameController()

    var controller1Snap = 0u
    var controller2Snap = 0u


    override fun notify(sender: Sender, event: Event) {
        when (sender) {

            Sender.PPU -> {

                when (event) {
                    Event.NMI -> {
                        cpu.isPendingNMI = true
                    }
                    Event.DMA -> {
                        cpu.isSuspendedForDMA = true
                    }
                }

            }

            else -> {}
        }
    }

    override fun readAddress(address: UShort): UByte {

        // 2KB internal Ram and Mirrors
        if (address in 0x0000u..0x1FFFu) {
            return ram.memory[address.toInt()]
        }

        // PPU registers and mirrors.
        if (address in 0x2000u..0x3FFFu) {
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

        // APU and I/O
        if (address in 0x4000u..0x4017u) {

            if (address == (0x4016u).toUShort()) {
                val data = when (controller1Snap and 0x80u) {
                    0u -> 0u
                    else -> 1u
                }

                controller1Snap = controller1Snap shl 1

                return data.toUByte()
            }

            return 0u
        }

        // 	APU and I/O functionality that is normally disabled
        if (address in 0x4018u..0x401Fu) {
            return 0u
        }

        // Cartridge Space
        if (address in 0x4020u..0xFFFFu) {
            return mapper.readCartridgeAddress(address)
        }

        return 0x00u
    }

    override fun writeToAddress(address: UShort, data: UByte) {

        // 2KB internal Ram and Mirrors
        if (address in 0x0000u..0x1FFFu) {
            ram.writeToMemory(address, data)
            ram.writeToMemory((address + 0x0800u).mod(0x2000u).toUShort(), data)
            ram.writeToMemory((address + 0x1000u).mod(0x2000u).toUShort(), data)
            ram.writeToMemory((address + 0x1800u).mod(0x2000u).toUShort(), data)

            return
        }

        // PPU registers and mirrors.
        if (address in 0x2000u..0x3FFFu) {
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


        // APU and I/O
        if (address in 0x4000u..0x4017u) {

            if (address == (0x4014u).toUShort()) {
                ppu.writeToDMARegister(data.toUInt())
            }

            if (address == (0x4016u).toUShort()) {
                controller1Snap = controller1.controllerState
            }

            return
        }

        // 	APU and I/O functionality that is normally disabled
        if (address in 0x4018u..0x401Fu) {
            return
        }

        // Cartridge Space
        if (address in 0x4020u..0xFFFFu) {
            mapper.writeToCartridgeAddress(address, data)
            return
        }

    }

    fun reset() {
        cpu.reset()
    }
}