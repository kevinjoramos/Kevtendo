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

    /**
     * Greyscale Or Color
     * (0: normal color, 1: produce a greyscale display)
     */
    val isGreyscale
        get() = (value and GREYSCALE_BITMASK) != 0u
    /**
     * (1: Show background in leftmost 8 pixels of screen, 0: Hide)
     */
    val isShowingBackgroundInLeftMost8Pixels
        get() = (value and SHOW_BACKGROUND_IN_LEFT_MOST_PIXELS) != 0u

    /**
     * (1: Show sprites in leftmost 8 pixels of screen, 0: Hide)
     */
    val isShowingSpritesInLeftMost8Pixels
        get() = (value and SHOW_SPRITES_IN_LEFT_MOST_PIXELS) != 0u

    val isShowingBackground
        get() = (value and SHOW_BACKGROUND_BITMASK) != 0u

    val isShowingSprites
        get() = (value and SHOW_SPRITES_BITMASK) != 0u

    val isEmphasizingRed
        get() = (value and EMPHASIZE_RED) != 0u

    val isEmphasizingGreen
        get() = (value and EMPHASIZE_GREEN) != 0u

    val isEmphasizingBlue
        get() = (value and EMPHASIZE_BLUE) != 0u

    companion object {
        // GREYSCALE
        const val GREYSCALE_BITMASK = 0x01u

        // Show background and sprites
        const val SHOW_BACKGROUND_IN_LEFT_MOST_PIXELS = 0x02u
        const val SHOW_SPRITES_IN_LEFT_MOST_PIXELS = 0x04u
        const val SHOW_BACKGROUND_BITMASK = 0x80u
        const val SHOW_SPRITES_BITMASK = 0x10u

        // Emphasis
        const val EMPHASIZE_RED = 0x20u
        const val EMPHASIZE_GREEN = 0x40u
        const val EMPHASIZE_BLUE = 0x80u
    }

}