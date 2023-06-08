package cartridge

import mediator.Mediator

@ExperimentalUnsignedTypes
class MapperZero(private val cartridge: Cartridge, bus: Mediator) : Mapper(cartridge, bus) {
    init {
        mirrorCharacterMemory() //TODO("This needs to be toggled by the header.")
    }
    override fun readCartridgeAddress(address: UShort): UByte {
        if (address < 0x8000u) {
            return cartridge.characterRom[(address - 0x6000u).toInt()]
        }

        return cartridge.programRom[(address - 0x8000u).toInt()]
    }

   fun writeToCartridgeAddress(address: UShort, data: UByte) {
        if (address < 0x8000u) {
            cartridge.characterRom[(address - 0x6000u).toInt()] = data
        }

        cartridge.programRom[(address - 0x8000u).toInt()] = data
    }




    private fun mirrorCharacterMemory() {
        cartridge.programRom = cartridge.programRom + cartridge.programRom
    }
}