package ppu

class OamAddressRegister {

    var value: UInt = 0u
        set(value) {field = value and 0xFFu}

    fun increment() { value++ }

}
