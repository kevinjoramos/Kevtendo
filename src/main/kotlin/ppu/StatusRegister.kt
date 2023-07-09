package ppu

/**
 * Status Register
 * This register reflects the state of various functions inside the PPU.
 * It is often used for determining timing. To determine when the PPU has reached a given pixel of the screen,
 * put an opaque (non-transparent) pixel of sprite 0 there.
 * READ ONLY
 *
 *      7  bit  0
 *      ---- ----
 *      VSO. ....
 *      |||| ||||
 *      |||+-++++- PPU open bus. Returns stale PPU bus contents.
 *      ||+------- Sprite overflow. The intent was for this flag to be set
 *      ||         whenever more than eight sprites appear on a scanline, but a
 *      ||         hardware bug causes the actual behavior to be more complicated
 *      ||         and generate false positives as well as false negatives; see
 *      ||         PPU sprite evaluation. This flag is set during sprite
 *      ||         evaluation and cleared at dot 1 (the second dot) of the
 *      ||         pre-render line.
 *      |+-------- Sprite 0 Hit.  Set when a nonzero pixel of sprite 0 overlaps
 *      |          a nonzero background pixel; cleared at dot 1 of the pre-render
 *      |          line.  Used for raster timing.
 *      +--------- Vertical blank has started (0: not in vblank; 1: in vblank).
 *                  Set at dot 1 of line 241 (the line *after* the post-render
 *                  line); cleared after reading $2002 and at dot 1 of the
 *                  pre-render line.
 *
 *
 */
class StatusRegister {

    var value: UInt = 0xA0u
        set(value) { field = value and 0xE0u }

    /**
     * Sprite overflow. The intent was for this flag to be set
     * whenever more than eight sprites appear on a scanline, but a
     * hardware bug causes the actual behavior to be more complicated
     * and generate false positives as well as false negatives; see
     * PPU sprite evaluation. This flag is set during sprite
     * evaluation and cleared at dot 1 (the second dot) of the
     * pre-render line.
     */
    val hasSpriteOverflow
        get() = when (value and 0x20u) {
            0u -> false
            else -> true
        }

    /**
     * Sprite 0 Hit.  Set when a nonzero pixel of sprite 0 overlaps
     * a nonzero background pixel; cleared at dot 1 of the pre-render
     * line.  Used for raster timing.
     */
    val hasSpriteHit
        get() = when (value and 0x40u) {
            0u -> false
            else -> true
        }


    var isInVBlank
        get() = when (value and 0x80u) {
            0u -> false
            else -> true
        }
        set(value) {
            when (value) {
                true -> this.value = this.value or 0x80u
                false -> this.value = this.value and (0x80u).inv()
            }
        }

    /**
     * Clear Status Register
     * Reading the status register will clear bit 7.
     * It does not clear the sprite 0 hit or overflow bit.
     */
    fun clearBit7() {
        value = (value and 0x80u.inv())
    }

}