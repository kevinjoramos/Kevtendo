package Cartridge

@ExperimentalUnsignedTypes
abstract class Mapper(cartridge: Cartridge) {

    abstract fun readCartridgeAddress(address: UShort): UByte

}