package ppu

@ExperimentalUnsignedTypes
class GraphicsRenderer() {

    private var currentVRAMAddress = UInt
        set(value) { field = value and FIFTEEN_BIT_MASK}

    private val patternTableShiftRegister1 = ShiftRegister16Bit()
    private val patternTableShiftRegister2 = ShiftRegister16Bit()

    private val paletteAttributeShiftRegister1 = ShiftRegister8Bit()
    private val paletteAttributeShiftRegister2 = ShiftRegister8Bit()

    companion object {
        private val FIFTEEN_BIT_MASK = 0x8000u
    }

}