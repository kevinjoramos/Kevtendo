package ppu

class TRegister {

    var value: UInt = 0u
        set(value) { field = value and FIFTEEN_BITMASK }

    var coarseX: UInt
        get() = value and COARSE_X_BITMASK
        set(value) {
            val coarseX = ((value shr 3) and COARSE_X_BITMASK)
            this.value = value or coarseX
        }

    var coarseY: UInt
        get() = (value and COARSE_Y_BITMASK) shr COARSE_Y_SHIFT
        set(value) {
            val coarseY = (value and 0xF8u) shl 2
            this.value = value or coarseY
        }

    var nameTableSelect: UInt
        get() = (value and NAMETABLE_SELECT_BITMASK) shr NAMETABLE_SELECT_SHIFT
        set(value) {
            val nameTableSelect = value and NAMETABLE_SELECT_BITMASK
            this.value = value or nameTableSelect
        }

    var fineY: UInt
        get() = (value and FINE_Y_BITMASK) shr FINE_Y_SHIFT
        set(value) {
            val fineY = (value and 0x7u) shl 12
            this.value = value or fineY
        }

    var upperLatch: UInt
        get() = value and UPPER_LATCH_BITMASK
        set(value) {
            val upperLatch = (value and 0x3Fu) shl 8
            this.value = value or upperLatch
            this.value = value and 0x7FFFu
        }

    var lowerLatch: UInt
        get() = value and UPPER_LATCH_BITMASK
        set(value) {
            this.value = value or value
        }

    companion object {
        private const val FIFTEEN_BITMASK = 0x7FFFu
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