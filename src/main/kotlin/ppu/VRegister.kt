package ppu

class VRegister {

    /**
     * Current VRAM Address
     */
    var value: UInt = 0u
        set(value) { field = value and FIFTEEN_BITMASK }

    val currentCoarseXScroll: UInt
        get() = value and COARSE_X_BITMASK

    val currentCoarseYScroll: UInt
        get() = (value and COARSE_Y_BITMASK) shr COARSE_Y_SHIFT

    val currentNameTableSelect: UInt
        get() = (value and NAMETABLE_SELECT_BITMASK) shr NAMETABLE_SELECT_SHIFT

    val currentFineYScroll: UInt
        get() = (value and FINE_Y_BITMASK) shr FINE_Y_SHIFT


    /**
     * The tile address is the value represented by excluding fine y.
     */
    val tileAddress: UInt
        get() = 0x2000u or (value and 0x0FFFu)

    /**
     * The attribute address is hard to explain, but it comes from dividing the name table into 8 by 8 sections.
     * The free bits become the attribute offset. Attribute tiles start at 23C0
     */
    val attributeDataAddress: UInt
        get() = 0x23C0u or (value and 0x0C00u) or ((value shr 4) and 0x38u) or ((value shr 2) and 0x07u)


    companion object {
        private const val FIFTEEN_BITMASK = 0x7FFFu
        private const val COARSE_X_BITMASK = 0x1Fu
        private const val COARSE_Y_BITMASK = 0x3Eu
        private const val COARSE_Y_SHIFT = 5
        private const val NAMETABLE_SELECT_BITMASK = 0xC00u
        private const val NAMETABLE_SELECT_SHIFT = 10
        private const val FINE_Y_BITMASK = 0x7000u
        private const val FINE_Y_SHIFT = 12
    }
}