package ppu

/**
 * Controller Register
 * contains various flags that control PPU behavior.
 * write only
 * TODO "After power/reset, writes to this register are ignored for about 30,000 cycles."
 */
class ControllerRegister {

    var value = 0x00u

    /**
     * Base nametable address
     * (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
     */
    val baseNameTableAddress: UInt
        get() {
            return when (value and BASE_NAMETABLE_BITMASK) {
                0u -> BASE_NAMETABLE_ADDRESS_2000
                1u -> BASE_NAMETABLE_ADDRESS_2400
                2u -> BASE_NAMETABLE_ADDRESS_2800
                else -> BASE_NAMETABLE_ADDRESS_2C00
            }
        }

    /**
     * VRAM address increment per CPU read/write of PPUDATA
     * (0: add 1, going across; 1: add 32, going down)
     */
    val vRamAddressIncrement: UInt
        get() = when (value and VRAM_ADDRESS_INCREMENT_BITMASK) {
            0u -> INCREMENT_GOING_ACROSS
            else -> INCREMENT_GOING_DOWN
        }

    /**
     * Sprite pattern table address for 8x8 sprites
     * (0: $0000; 1: $1000; ignored in 8x16 mode)
     */
    val sprite8x8PatternTableAddress: UInt
        get() = when (value and SPRITE_PATTERN_TABLE_ADDRESS_BITMASK) {
            0u -> SPRITE_TABLE_ADDRESS_0000
            else -> SPRITE_TABLE_ADDRESS_1000
        }


    /**
     * Background pattern table address
     * (0: $0000; 1: $1000)
     */
    val backgroundPatternTableAddress: UInt
        get() = when (value and BACKGROUND_PATTERN_TABLE_ADDRESS_BITMASK) {
            0u -> BACKGROUND_PATTERN_TABLE_ADDRESS_0000
            else -> BACKGROUND_PATTERN_TABLE_ADDRESS_1000
        }

    /**
     * Sprite size
     * (0: 8x8 pixels; 1: 8x16 pixels – see PPU OAM#Byte 1)
     */
    val spriteSize: SpriteSize
        get() = when (value and SPRITE_SIZE_BITMASK) {
            0u -> SpriteSize.EIGHT_X_EIGHT
            else -> SpriteSize.EIGHT_X_SIXTEEN
        }

    /**
     * PPU master/slave select
     * (0: read backdrop from EXT pins; 1: output color on EXT pins)
     */
    val masterSlaveSelect
        get() = when (value and MASTER_SLAVE_SELECT_BITMASK) {
            0u -> MasterSlaveSelect.READ_BACKDROP_FROM_EXT_PINS
            else -> MasterSlaveSelect.OUTPUT_COLOR_ON_EXT_PINS

        }

    /**
     * Generate an NMI at the start of the vertical blanking interval
     * (0: off; 1: on)
     */
    val generateNMIAtStartVBlank: Boolean
        get() = (value and GENERATE_NMI_AT_START_VBLANK) == 1u

    companion object {
        // BASE NAME TABLE
        const val BASE_NAMETABLE_BITMASK = 0x03u
        const val BASE_NAMETABLE_ADDRESS_2000 = 0x2000u
        const val BASE_NAMETABLE_ADDRESS_2400 = 0x2400u
        const val BASE_NAMETABLE_ADDRESS_2800 = 0x2800u
        const val BASE_NAMETABLE_ADDRESS_2C00 = 0x2C00u

        // VRAM INCREMENT
        const val VRAM_ADDRESS_INCREMENT_BITMASK = 0x04u
        const val INCREMENT_GOING_ACROSS = 1u
        const val INCREMENT_GOING_DOWN = 32u

        // SPRITE PATTERN TABLE ADDRESS
        const val SPRITE_PATTERN_TABLE_ADDRESS_BITMASK = 0x08u
        const val SPRITE_TABLE_ADDRESS_0000 = 0x0000u
        const val SPRITE_TABLE_ADDRESS_1000 = 0x1000u

        // BACKGROUND PATTERN TABLE ADDRESS
        const val BACKGROUND_PATTERN_TABLE_ADDRESS_BITMASK = 0x10u
        const val BACKGROUND_PATTERN_TABLE_ADDRESS_0000 = 0x0000u
        const val BACKGROUND_PATTERN_TABLE_ADDRESS_1000 = 0x1000u

        // SPRITE SIZE BITMASK
        const val SPRITE_SIZE_BITMASK = 0x20u
        enum class SpriteSize {
            EIGHT_X_EIGHT, EIGHT_X_SIXTEEN
        }

        // PPU MASTER / SLAVE SELECT
        const val MASTER_SLAVE_SELECT_BITMASK = 0x40u
        enum class MasterSlaveSelect {
            READ_BACKDROP_FROM_EXT_PINS,
            OUTPUT_COLOR_ON_EXT_PINS
        }

        // GENERATE NMI AT START OF VERTICAL BLANK PERIOD
        const val GENERATE_NMI_AT_START_VBLANK = 0x80u
    }

}