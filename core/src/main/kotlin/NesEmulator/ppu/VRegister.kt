package ppu

class VRegister {

    /**
     * Current VRAM Address
     */
    var value: UInt = 0u
        set(value) { field = value and 0x7FFFu }

    val coarseX: UInt
        get() = value and 0x1Fu

    /**
     * t: 0... .... ...A BCDE <- d: ABCD E...
     */
    fun parseCoarseX(data: UInt) {
        val newCoarseX = (data shr 3) and (0x1Fu)
        this.value = (this.value and 0x1Fu.inv()) or newCoarseX
    }

    val coarseY: UInt
        get() = (value and 0x03E0u) shr 5

    /**
     * t: 0... ..AB CDE. .... <- d: ABCD E...
     */
    fun parseCoarseY(data: UInt) {
        val newCoarseY = (data and 0xF8u) shl 2
        this.value = (this.value and 0x03E0u.inv()) or newCoarseY
    }

    val nameTableSelect: UInt
        get() = (value and 0xC00u) shr 10

    /**
     * t: 0... GH.. .... .... <- d: .... ..GH
     */
    fun parseNameTableSelect(data: UInt) {
        val newNameTableSelect = (data and 0x03u) shl 10
        this.value = (this.value and 0xC00u.inv()) or newNameTableSelect
    }

    val fineY: UInt
        get() = (value and 0x7000u) shr 12

    /**
     * t: 0FGH .... .... .... <- d: .... .FGH
     */
    fun parseFineY(data: UInt) {
        val newFineY = (data and 0x07u) shl 12
        this.value = (this.value and 0x7000u.inv()) or newFineY
    }


    /**
     * The tile address is the value represented by excluding fine y.
     */
    val patternTileAddress: UInt
        get() = 0x2000u or (value and 0x0FFFu)

    /**
     * The attribute address is hard to explain, but it comes from dividing the name table into 8 by 8 sections.
     * This is done by shifting our Coarse X and Y
     * The free bits become the attribute offset. Attribute tiles start at 23C0
     */
    val attributeTileAddress: UInt
        get() = 0x23C0u or ((value and 0x0C00u) or ((value shr 4) and 0x38u) or ((value shr 2) and 0x07u))


    fun incrementCoarseX() {
        if (coarseX == 31u) {
            this.value = this.value and 0x1Fu.inv()
            this.value = this.value xor 0x0400u
        } else {
            this.value++
        }
    }

    fun incrementY() {
        if (fineY != 7u) {
            value += 0x1000u
        } else {
            value = value and (0x7000u).inv()
            var y = coarseY
            when (y) {
                29u -> {
                    y = 0u
                    value = value xor 0x0800u
                }
                31u -> {
                    y = 0u
                }
                else -> {
                    y += 1u
                }
            }
            value = (value and 0x03E0u.inv()) or (y shl 5)
        }
    }
}
