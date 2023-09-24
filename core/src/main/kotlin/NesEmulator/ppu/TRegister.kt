package ppu

class TRegister {

    var value: UInt = 0u
        set(value) { field = value and 0x7FFFu}

    val coarseX: UInt
        get() = value and 0x1Fu

    /**
     * t: 0... .... ...A BCDE <- d: ABCD E...
     */
    fun parseCoarseX(data: UInt) {
        val newCoarseX = (data shr 3) and (0x1Fu)
        this.value = (this.value and (0x1Fu).inv()) or newCoarseX
    }

    val coarseY: UInt
        get() = (value and 0x03E0u) shr 5

    /**
     * t: 0... ..AB CDE. .... <- d: ABCD E...
     */
    fun parseCoarseY(data: UInt) {
        val newCoarseY = (data and 0xF8u) shl 2
        this.value = (this.value and (0x03E0u).inv()) or newCoarseY
    }

    val nameTableSelect: UInt
        get() = (value and 0xC00u) shr 10

    /**
     * t: 0... GH.. .... .... <- d: .... ..GH
     */
    fun parseNameTableSelect(data: UInt) {
        val newNameTableSelect = (data and 0x03u) shl 10
        this.value = (this.value and (0xC00u).inv()) or newNameTableSelect
    }

    val fineY: UInt
        get() = (value and 0x7000u) shr 12

    /**
     * t: 0FGH .... .... .... <- d: .... .FGH
     */
    fun parseFineY(data: UInt) {
        val newFineY = (data and 0x07u) shl 12
        this.value = (this.value and (0x7000u).inv()) or newFineY
    }

    /**
     * t: 0.CD EFGH .... .... <- d: ..CD EFGH
     *        <unused>     <- d: AB.. ....
     * t: 0Z.. .... .... .... <- 0 (bit Z is cleared)
     */
    fun parseUpperLatch(data: UInt) {
        val newUpperLatch = (data and 0x3Fu) shl 8
        this.value = (this.value and 0x7F00u.inv()) or newUpperLatch
    }

    /**
     * t: 0... .... ABCD EFGH <- d: ABCD EFGH
     */
    fun parseLowerLatch(data: UInt) {
        this.value = (this.value and 0xFF00u) or data
    }
}
