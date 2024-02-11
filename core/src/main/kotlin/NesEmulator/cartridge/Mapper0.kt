package cartridge

import bus.Bus
import mediator.Mediator

@ExperimentalUnsignedTypes
class Mapper0(
    cartridge: Cartridge,
    bus: Bus
): Mapper(cartridge, bus) {
    init {
        mirrorCharacterMemory() //TODO("This needs to be toggled by the header.")
    }

    override fun readAddressFromProgramRom(address: UShort): UByte {
        if (address in 0x8000u.. 0xFFFFu) {
            return cartridge.programRom[(address - 0x8000u).toInt()]
        }
        return 0u
    }

    override fun readAddressFromCharacterRom(address: UShort): UByte {
        return cartridge.characterRom[address.toInt()]
    }

    override fun writeToAddressInProgramRom(address: UShort, data: UByte) = Unit

    private fun mirrorCharacterMemory() {
        cartridge.programRom = cartridge.programRom + cartridge.programRom
    }


}
