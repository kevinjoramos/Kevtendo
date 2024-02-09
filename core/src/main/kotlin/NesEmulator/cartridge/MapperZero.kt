package cartridge

import mediator.Mediator
import util.to2DigitHexString
import util.to4DigitHexString

@ExperimentalUnsignedTypes
class MapperZero(val cartridge: Cartridge, bus: Mediator) : Mapper(cartridge, bus) {
    init {
        //mirrorCharacterMemory() //TODO("This needs to be toggled by the header.")
    }
    override fun readCartridgeAddress(address: UShort): UByte {
        if (address in 0x6000u..0x7FFFu) {
            return cartridge.characterRom[(address - 0x6000u).toInt()]
        }

        if (address in 0x8000u.. 0xFFFFu) {
                return cartridge.programRom[(address - 0x8000u).toInt()]
        }

        return 0u
    }

   fun writeToCartridgeAddress(address: UShort, data: UByte) {
       /*
       if (address < 0x8000u) {
            cartridge.characterRom[(address - 0x6000u).toInt()] = data
        }

        cartridge.programRom[(address - 0x8000u).toInt()] = data

        */
    }





    private fun mirrorCharacterMemory() {
        cartridge.programRom = cartridge.programRom + cartridge.programRom
    }
}
