package ppu

class OamAddressRegister {

    var value: UInt = 0u
        set(value) {field = value.toUByte().toUInt()}

    fun increment() { value++ }

}
