package bus

import cpu.CPU6502
import cartridge.Cartridge
import cartridge.Mapper0
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
): Mediator {
    val mapper = Mapper0(Cartridge(cartridgePath), this)
    val ram = UByteArray(0x0800) { 0u }
    val ppu = PPU2C02(this)
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

    fun readAddressAsCpu(address: UShort): UByte {
        // 2kb ram, also mirrored 3 times.
        if (address in 0x0000u..0x1FFFu) {
            return ram[address.toInt().mod(0x0800)]
        }
        // PPU registers, mirrored every 8 bytes.
        if (address in 0x2000u..0x3FFFu) {
            val ppuRegisterAddress = ((address - 0x2000u).mod(0x0008u)).toUShort()
            when (ppuRegisterAddress) {
                (0x00u).toUShort() -> return ppu.readControllerRegister().toUByte()
                (0x01u).toUShort() -> return ppu.readMaskRegister().toUByte()
                (0x02u).toUShort() -> return ppu.readStatusRegister().toUByte()
                (0x03u).toUShort() -> return ppu.readOamAddressRegister().toUByte()
                (0x04u).toUShort() -> return ppu.readOamDataRegister().toUByte()
                (0x05u).toUShort() -> return ppu.readScrollRegister().toUByte()
                (0x06u).toUShort() -> return ppu.readAddressRegister().toUByte()
                (0x07u).toUShort() -> return ppu.readDataRegister().toUByte()
            }
        }
        // APU and I/O registers
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
        // Cartridge PRG ROM, PRG RAM, mapper registers
        if (address in 0x4020u..0xFFFFu) {
            return mapper.readAddressFromProgramRom(address)
        }
        return 0x00u
    }

    fun writeToAddressAsCpu(address: UShort, data: UByte) {
        // 2kb ram, also mirrored 3 times.
        if (address in 0x0000u..0x1FFFu) {
            ram[address.toInt().mod(0x0800)] = data
            return
        }
        // PPU registers, mirrored every 8 bytes.
        if (address in 0x2000u..0x3FFFu) {
            val ppuRegisterAddress = ((address - 0x2000u).mod(0x0008u)).toUShort()
            when (ppuRegisterAddress) {
                (0x00u).toUShort() -> ppu.writeToControllerRegister(data.toUInt())
                (0x01u).toUShort() -> ppu.writeToMaskRegister(data.toUInt())
                (0x02u).toUShort() -> ppu.writeToStatusRegister(data.toUInt())
                (0x03u).toUShort() -> ppu.writeToOamAddressRegister(data.toUInt())
                (0x04u).toUShort() -> ppu.writeToOamDataRegister(data.toUInt())
                (0x05u).toUShort() -> ppu.writeToScrollRegister(data.toUInt())
                (0x06u).toUShort() -> ppu.writeToAddressRegister(data.toUInt())
                (0x07u).toUShort() -> ppu.writeToDataRegister(data.toUInt())
            }
            return
        }
        // APU and I/O registers
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
            mapper.writeToAddressInProgramRom(address, data)
            return
        }
    }

    fun readAddressAsPpu(address: UShort): UByte = mapper.readAddressFromCharacterRom(address)
}
