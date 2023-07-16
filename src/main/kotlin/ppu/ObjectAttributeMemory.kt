package ppu

@OptIn(ExperimentalUnsignedTypes::class)
class ObjectAttributeMemory {

    // This is the buffer used for the sprite evaluation.
    val secondaryMemory = List(8) { Sprite() }
    var openSlot = 0

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
        openSlot = 0

        // Pick out the first 8 sprites on this scanline to draw
        for (index in 0..255 step 4) {

            // if the current y value is in between the height of the tile, it is being rendered.
            if (scanline >= primaryMemory[index] && scanline <= primaryMemory[index] + if (isSpriteSize8x16) 16u else 8u ) {

                // If 8 sprites are found check to see if we need to set the sprite overflow flag.
                if (openSlot == 8) {
                    hasSpriteOverflow = true
                    break;
                }

                // Store the sprites into our structure.
                secondaryMemory[openSlot].yPosition = primaryMemory[index].toUInt()
                secondaryMemory[openSlot].tileIndex = primaryMemory[index + 1].toUInt()
                secondaryMemory[openSlot].attributes = primaryMemory[index + 2].toUInt()
                secondaryMemory[openSlot].xPosition = primaryMemory[index + 3].toUInt()

                openSlot++
            }
        }

        // Fill any empty spaces with FF.
        while (openSlot < 8) {
            secondaryMemory[openSlot].clear()
            openSlot++
        }

        return hasSpriteOverflow
    }


    /**
     * Sprite class is just a structure I made for easier access.
     * Since each sprite takes up 4 bytes.
     */
    class Sprite {
        var yPosition: UInt = 0u
            set(value) {field = field and 0xFFu}
        var tileIndex: UInt = 0u
            set(value) {field = field and 0xFFu}
        var attributes: UInt = 0u
            set(value) {field = field and 0xFFu}
        var xPosition: UInt = 0u
            set(value) {field = field and 0xFFu}

        fun clear() {
            yPosition = 0xFFu
            tileIndex = 0xFFu
            attributes = 0xFFu
            xPosition = 0xFFu
        }
    }

}