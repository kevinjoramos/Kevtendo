package ppu

class VRegister {

    /**
     * Current VRAM Address
     */
    var value: UInt = 0u
        set(value) { field = value and 0x7FFFu}

    val coarseX: UInt
        get() = value and 0x1Fu

    val coarseY: UInt
        get() = (value and 0x3E0u) shr 5

    val nameTableSelect: UInt
        get() = (value and 0xC00u) shr 10

    val fineY: UInt
        get() = (value and 0x7000u) shr 12

    var addressRegister: UInt
        get() = (value and 0x3FFFu)
        set(value) {
            this.value = value and 0x3FFFu
        }


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


    fun incrementCoarseX() {
        if (coarseX == 31u) {
            this.value = this.value and 0x1Fu.inv()
            this.value = this.value xor 0x0400u
        } else {
            this.value++
        }
    }

    fun incrementY() {
        if (fineY < 7u) {
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