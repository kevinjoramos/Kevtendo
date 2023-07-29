package ppu

/**
 * Mask Register
 * controls the rendering of sprites and backgrounds, as well as colour effects.
 * write only
 *
 *       7  bit  0
 *       ---- ----
 *       BGRs bMmG
 *       |||| ||||
 *       |||| |||+- Greyscale (0: normal color, 1: produce a greyscale display)
 *       |||| ||+-- 1: Show background in leftmost 8 pixels of screen, 0: Hide
 *       |||| |+--- 1: Show sprites in leftmost 8 pixels of screen, 0: Hide
 *       |||| +---- 1: Show background
 *       |||+------ 1: Show sprites
 *       ||+------- Emphasize red (green on PAL/Dendy)
 *       |+-------- Emphasize green (red on PAL/Dendy)
 *       +--------- Emphasize blue
 */
class MaskRegister {

    var value = 0x00u
        set(value) { field = value and 0xFFu }

    /**
     * Greyscale Or Color
     * (0: normal color, 1: produce a greyscale display)
     */
    val isGreyscale
        get() = when (value and 0x01u) {
            0u -> false
            else -> true
        }
    /**
     * (1: Show background in leftmost 8 pixels of screen, 0: Hide)
     */
    val isShowingBackgroundInLeftMost8Pixels
        get() = when (value and 0x02u) {
            0u -> false
            else -> true
        }

    /**
     * (1: Show sprites in leftmost 8 pixels of screen, 0: Hide)
     */
    val isShowingSpritesInLeftMost8Pixels
        get() = when (value and 0x04u) {
            0u -> false
            else -> true
        }

    val isShowingBackground
        get() = when (value and 0x08u) {
            0u -> false
            else -> true
        }

    val isShowingSprites
        get() = when (value and 0x10u) {
            0u -> false
            else -> true
        }

    val isEmphasizingRed
        get() = when (value and 0x20u) {
            0u -> false
            else -> true
        }

    val isEmphasizingGreen
        get() = when (value and 0x40u) {
            0u -> false
            else -> true
        }


    val isEmphasizingBlue
        get() = when (value and 0x80u) {
            0u -> false
            else -> true
        }
}