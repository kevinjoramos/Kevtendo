package ppu

import util.to2DigitHexString

@OptIn(ExperimentalUnsignedTypes::class)
class ObjectAttributeMemory {

    // This is the buffer used for the sprite evaluation.
    val secondaryMemory = List(8) { Sprite() }

    var isSpriteZeroPossible = false
    var isSpriteZeroBeingRendered = false

    // This is the actual space that stores the 64 sprites.
    val primaryMemory = UByteArray(0x100) { 0u }


    /**
     * This function finds the next 8 bytes on the provided scanline,
     * and loads their data into the secondary OAM Space.
     */
    fun evaluateNextEightSprites(scanline: UInt, isSpriteSize8x16: Boolean): Boolean {

        var hasSpriteOverflow = false

        // Clear the list of sprites to draw.
        secondaryMemory.forEach { sprite ->
            sprite.clear()
        }

        isSpriteZeroPossible = false

        primaryMemory
            .chunked(4)

            // check to see if sprite 0 will be rendered.
            .also { it ->
                if (scanline >= it[0][0] && scanline <= (it[0][0] + if (isSpriteSize8x16) 15u else 7u))  {
                    isSpriteZeroPossible = true
                }
            }

            // filter out the sprites not on this scanline.
            .filter { bytes -> scanline >= bytes[0] && scanline <= (bytes[0] + if (isSpriteSize8x16) 15u else 7u)}

            // naively check for sprite overflow.
            .also {
                if (it.size > 8) hasSpriteOverflow = true
            }

            // pick out the first 8 sprites on the scanline.
            .take(8)

            // update the secondary memory sprite values.
            .forEachIndexed { index, bytes ->
                secondaryMemory[index].yPosition = bytes[0].toUInt()
                secondaryMemory[index].tileIndex = bytes[1].toUInt()
                secondaryMemory[index].attributes = bytes[2].toUInt()
                secondaryMemory[index].xPosition = bytes[3].toUInt()
            }

        return hasSpriteOverflow
    }

    fun decrementAllX() {
        secondaryMemory.forEach { sprite -> if (sprite.xPosition != 0u) sprite.xPosition-- }
    }

    fun shiftAllActiveSprites() {
        for (sprite in secondaryMemory) {
            if (sprite.xPosition == 0u) {
                sprite.lowSpriteShiftRegister = sprite.lowSpriteShiftRegister shl 1
                sprite.highSpriteShiftRegister = sprite.highSpriteShiftRegister shl 1
            }
        }
    }

    fun getPrioritizedActiveSprite(): Sprite? {

        // Look for first active sprite with non 0 pixel value.
        isSpriteZeroBeingRendered = false
        for ((index, sprite) in secondaryMemory.withIndex()) {
            if (sprite.xPosition == 0u) {
                val colorSelect = ((sprite.highSpriteShiftRegister and 0x80u) shr 6) or ((sprite.lowSpriteShiftRegister and 0x80u) shr 7)
                if (colorSelect != 0u) {
                    if (index == 0 && isSpriteZeroPossible) isSpriteZeroBeingRendered = true
                    return sprite
                }
            }
        }

        return null
    }

    /**
     * Sprite class is just a structure I made for easier access.
     * Since each sprite takes up 4 bytes.
     */
    class Sprite {
        var yPosition: UInt = 0u
            set(value) {field = value and 0xFFu}
        var tileIndex: UInt = 0u
            set(value) {field = value and 0xFFu}
        var attributes: UInt = 0u
            set(value) {field = value and 0xFFu}

        val palette: UInt get() = (attributes and 0x03u) + 4u

        val hasPriority: Boolean get() = when (attributes and 0x20u) {
            0u -> false
            else -> true
        }

        val isFlippedHorizontally: Boolean get() = when (attributes and 0x40u) {
            0u -> false
            else -> true
        }

        val isFlippedVertically: Boolean get() = when (attributes and 0x80u) {
            0u -> false
            else -> true
        }

        var xPosition: UInt = 0u
            set(value) {field = value and 0xFFu}

        var lowSpriteShiftRegister = 0u
            set(value) {field = value and 0xFFu}

        var highSpriteShiftRegister = 0u
            set(value) {field = value and 0xFFu}

        fun clear() {
            yPosition = 0xFFu
            tileIndex = 0xFFu
            attributes = 0xFFu
            xPosition = 0xFFu

            lowSpriteShiftRegister = 0u
            highSpriteShiftRegister = 0u
        }

        fun orientTileSliver(sliver: UInt, isSpriteSize8x16: Boolean) {

        }

        override fun toString(): String {
            return "Sprite(yPosition=$yPosition, tileIndex=$tileIndex, attributes=$attributes, palette=$palette, hasPriority=$hasPriority, isFlippedHorizontally=$isFlippedHorizontally, isFlippedVertically=$isFlippedVertically, xPosition=$xPosition)"
        }
    }

}
