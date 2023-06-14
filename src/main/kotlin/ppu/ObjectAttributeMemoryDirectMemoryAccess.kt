package ppu

class ObjectAttributeMemoryDirectMemoryAccess {

    var value = 0u
        set(value) { field = value.toUByte().toUInt()}


}
