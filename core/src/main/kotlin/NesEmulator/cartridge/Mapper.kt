package cartridge

import bus.Bus
import mediator.Component
import mediator.Mediator

@ExperimentalUnsignedTypes
abstract class Mapper(
    val cartridge: Cartridge,
    val bus: Bus
) {
    abstract fun readAddressFromProgramRom(address: UShort): UByte
    abstract fun readAddressFromCharacterRom(address: UShort): UByte
    abstract fun writeToAddressInProgramRom(address: UShort, data: UByte)
}
