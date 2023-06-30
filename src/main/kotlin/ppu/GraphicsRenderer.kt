package ppu

@ExperimentalUnsignedTypes
class GraphicsRenderer() {

    var currentVRAMAddress: UInt = 0u
        set(value) { field = value and FIFTEEN_BITMASK }

    val currentCoarseXScroll: UInt
        get() = currentVRAMAddress and COARSE_X_BITMASK


    private var temporaryVRAMAddress: UInt = 0u
        set(value) { field = value and FIFTEEN_BITMASK }

    private var fineXScroll: UInt = 0u
        set(value) { field = value and THREE_BITMASK }

    private val firstOrSecondWriteToggle = false

    private val patternTableShiftRegister1 = ShiftRegister16Bit()
    private val patternTableShiftRegister2 = ShiftRegister16Bit()

    private val paletteAttributeShiftRegister1 = ShiftRegister8Bit()
    private val paletteAttributeShiftRegister2 = ShiftRegister8Bit()

    companion object {
        private val FIFTEEN_BITMASK = 0x7FFFu
        private val THREE_BITMASK = 0x7u
        private val COARSE_X_BITMASK = 0x1Fu
    }

}