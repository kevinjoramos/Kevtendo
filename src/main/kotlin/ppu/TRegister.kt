package ppu

class TRegister {

    var value: UInt = 0u
        set(value) { field = value and 0x7FFFu}

    var coarseX: UInt
        get() = value and 0x1Fu
        set(value) {
            val incomingCoarseX = ((value shr 3) and 0x1Fu)
            this.value = (this.value and 0x1Fu.inv()) or incomingCoarseX
        }

    var coarseY: UInt
        get() = (value and 0x3E0u) shr 5
        set(value) {
            val incomingCoarseY = (value and 0xF8u) shl 2
            this.value = (this.value and 0x3E0u.inv()) or incomingCoarseY
        }

    var nameTableSelect: UInt
        get() = (value and 0xC00u) shr 10
        set(value) {
            val nameTableSelectValue = (value and 0x03u) shl 10
            this.value = (this.value and 0xC00u.inv()) or nameTableSelectValue
        }

    var fineY: UInt
        get() = (value and 0x7000u) shr 12
        set(value) {
            val incomingFineY = (value and 0x7u) shl 12
            this.value = (this.value and 0x7000u.inv()) or incomingFineY
        }

    var upperLatch: UInt
        get() = value and 0x3F00u
        set(value) {
            val incomingUpperLatch = (value and 0x3Fu) shl 8
            this.value = (value and 0x3Fu.inv()) or incomingUpperLatch
            this.value = value and 0x7FFFu
        }

    var lowerLatch: UInt
        get() = value and 0xFFu
        set(value) {
            this.value = (this.value or 0xFFu.inv()) or value
        }
}