package ppu

class ObjectAttributeMemoryAddressRegister {

    var value: UInt = 0u
        set(value) {field = value.toUByte().toUInt()}

    fun increment() { value++ }

}
