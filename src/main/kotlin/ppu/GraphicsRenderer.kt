package ppu

@ExperimentalUnsignedTypes
class GraphicsRenderer() {

    /**
     * Current VRAM Address
     */
    var currentVRAMAddress: UInt = 0u
        set(value) { field = value and FIFTEEN_BITMASK }

    val currentCoarseXScroll: UInt
        get() = currentVRAMAddress and COARSE_X_BITMASK

    val currentCoarseYScroll: UInt
        get() = (currentVRAMAddress and COARSE_Y_BITMASK) shr COARSE_Y_SHIFT

    val currentNameTableSelect: UInt
        get() = (currentVRAMAddress and NAMETABLE_SELECT_BITMASK) shr NAMETABLE_SELECT_SHIFT

    val currentFineYScroll: UInt
        get() = (currentVRAMAddress and FINE_Y_BITMASK) shr FINE_Y_SHIFT


    /**
     * Temporary VRAM Address
     */
    var temporaryVRAMAddress: UInt = 0u
        set(value) { field = value and FIFTEEN_BITMASK }

    var temporaryCoarseXScroll: UInt
        get() = temporaryVRAMAddress and COARSE_X_BITMASK
        set(value) {
            val coarseX = ((value shr 3) and COARSE_X_BITMASK)
            temporaryVRAMAddress = temporaryVRAMAddress or coarseX
        }

    var temporaryCoarseYScroll: UInt
        get() = (temporaryVRAMAddress and COARSE_Y_BITMASK) shr COARSE_Y_SHIFT
        set(value) {
            val coarseY = (value and 0xF8u) shl 2
            temporaryVRAMAddress = temporaryVRAMAddress or coarseY
        }

    var temporaryNameTableSelect: UInt
        get() = (temporaryVRAMAddress and NAMETABLE_SELECT_BITMASK) shr NAMETABLE_SELECT_SHIFT
        set(value) {
            val nameTableSelect = value and NAMETABLE_SELECT_BITMASK
            temporaryVRAMAddress = temporaryVRAMAddress or nameTableSelect
        }

    var temporaryFineYScroll: UInt
        get() = (temporaryVRAMAddress and FINE_Y_BITMASK) shr FINE_Y_SHIFT
        set(value) {
            val fineY = (value and 0x7u) shl 12
            temporaryVRAMAddress = temporaryVRAMAddress or fineY
        }

    var temporaryUpperLatch: UInt
        get() = temporaryVRAMAddress and UPPER_LATCH_BITMASK
        set(value) {
            val upperLatch = (value and 0x3Fu) shl 8
            temporaryVRAMAddress = temporaryVRAMAddress or upperLatch
            temporaryVRAMAddress = temporaryVRAMAddress and 0x7FFFu
        }

    var temporaryLowerLatch: UInt
        get() = temporaryVRAMAddress and UPPER_LATCH_BITMASK
        set(value) {
            temporaryVRAMAddress = temporaryVRAMAddress or value
            currentVRAMAddress = temporaryVRAMAddress
        }

    /**
     * Fine X Scroll
     */
    var fineXScroll: UInt = 0u
        set(value) { field = value and THREE_BITMASK }

    /**
     * First / Second Write Toggle Switch.
     */
    var hasFirstWrite = false

    private val patternTableShiftRegister1 = ShiftRegister16Bit()
    private val patternTableShiftRegister2 = ShiftRegister16Bit()

    private val paletteAttributeShiftRegister1 = ShiftRegister8Bit()
    private val paletteAttributeShiftRegister2 = ShiftRegister8Bit()

    //private val frameBuffer = List<>

    fun executeRenderCycle() {

    }




    companion object {
        private const val FIFTEEN_BITMASK = 0x7FFFu
        private const val THREE_BITMASK = 0x7u
        private const val COARSE_X_BITMASK = 0x1Fu
        private const val COARSE_Y_BITMASK = 0x3Eu
        private const val COARSE_Y_SHIFT = 5
        private const val NAMETABLE_SELECT_BITMASK = 0xC00u
        private const val NAMETABLE_SELECT_SHIFT = 10
        private const val FINE_Y_BITMASK = 0x7000u
        private const val FINE_Y_SHIFT = 12
        private const val UPPER_LATCH_BITMASK = 0xFF00u
    }

}