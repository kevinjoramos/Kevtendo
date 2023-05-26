package Cartridge

@ExperimentalUnsignedTypes
class MapperZero(val cartridge: Cartridge) : Mapper(cartridge) {
    override fun readCartridgeAddress(address: UShort): UByte {
        if (address < 0x8000u) {
            return cartridge.dataPrgRom[(address - 0x6000u).toInt()]
        }

        if (address < 0xBFFFu) {
            return cartridge.dataChrRom[(address - 0x8000u).toInt()]
        }

        if (cartridge.header.sizeOfCharacterRom > 16384) {
            return cartridge.dataChrRom[(address - 0xC000u).toInt()]
        }

        return cartridge.dataChrRom[(address - 0x8000u).mod(16384u).toInt()]
    }
}