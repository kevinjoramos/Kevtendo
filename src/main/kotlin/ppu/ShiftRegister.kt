package ppu

class ShiftRegister16Bit {
    var value: UInt = 0u
        set(value) { field = value.toUShort().toUInt() }
}

class ShiftRegister8Bit {
    var value: UInt = 0u
        set(value) { field = value.toUByte().toUInt() }
}