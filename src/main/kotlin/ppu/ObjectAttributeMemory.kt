package ppu

@OptIn(ExperimentalUnsignedTypes::class)
class ObjectAttributeMemory {

    // This is the buffer used for the sprite evaluation.
    val secondaryMemory = List(8) { Sprite() }
    var secondaryMemorySize = 0

    // This is the actual space that stores the 64 sprites.
    val primaryMemory = UByteArray(0x100) { 0u }


    /**
     * This function finds the next 8 bytes on the provided scanline,
     * and loads their data into the secondary OAM Space.
     */
    fun evaluateNextEightSprites() {

        // Clear the list of sprites to draw.
        secondaryMemory.forEach { sprite ->
            sprite.clear()
        }
        secondaryMemorySize = 0

        // Pick out the first 8 sprites on this scanline to draw





        // If 8 sprites are found check to see if we need to set the sprite overflow flag.

        // Store the sprites into our structure.

        // Fill any empty spaces with FF.

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