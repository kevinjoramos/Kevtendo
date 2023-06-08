package cartridge

import mediator.Component
import mediator.Mediator

@ExperimentalUnsignedTypes
abstract class Mapper(cartridge: Cartridge, override var bus: Mediator) : Component {

    abstract fun readCartridgeAddress(address: UShort): UByte

}