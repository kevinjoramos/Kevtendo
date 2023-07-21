package ppu

/**
 * Controller Register
 * contains various flags that control PPU behavior.
 * write only
 * TODO "After power/reset, writes to this register are ignored for about 30,000 cycles."
 */
class ControllerRegister {

    var value = 0x00u
        set(value) { field = value and 0xFFu }

    /**
     * Base name table address
     * (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
     */
    val baseNameTableAddress: UInt
        get() {
            return when (value and 0x03u) {
                0u -> 0x2000u
                1u -> 0x2400u
                2u -> 0x2800u
                else -> 0x2C00u
            }
        }

    /**
     * VRAM address increment per CPU read/write of PPUDATA
     * (0: add 1, going across; 1: add 32, going down)
     */
    val vRamAddressIncrement: UInt
        get() = when (value and 0x04u) {
            0u -> 1u
            else -> 32u
        }

    /**
     * Sprite pattern table address for 8x8 sprites
     * (0: $0000; 1: $1000; ignored in 8x16 mode)
     */
    val squareSpritePatternTableAddress: UInt
        get() = when (value and 0x08u) {
            0u -> 0x0000u
            else -> 0x1000u
        }


    /**
     * Background pattern table address
     * (0: $0000; 1: $1000)
     */
    val backgroundPatternTableAddress: UInt
        get() = when (value and 0x10u) {
            0u -> 0x0000u
            else -> 0x1000u
        }

    /**
     * Sprite size
     * (0: 8x8 pixels; 1: 8x16 pixels – see PPU OAM#Byte 1)
     */
    val isSpriteSize8x16: Boolean
        get() = when (value and 0x20u) {
            0u -> false
            else -> true
        }

    /**
     * PPU master/slave select
     * (0: read backdrop from EXT pins; 1: output color on EXT pins)
     */
    val masterSlaveSelect
        get() = when (value and 0x40u) {
            0u -> MasterSlaveSelect.READ_BACKDROP_FROM_EXT_PINS
            else -> MasterSlaveSelect.OUTPUT_COLOR_ON_EXT_PINS

        }

    /**
     * Generate an NMI at the start of the vertical blanking interval
     * (0: off; 1: on)
     */
    val generateNMIAtStartVBlank: Boolean
        get() = when (value and 0x80u) {
            0u -> false
            else -> true
        }

    companion object {

        enum class MasterSlaveSelect {
            READ_BACKDROP_FROM_EXT_PINS,
            OUTPUT_COLOR_ON_EXT_PINS
        }
    }

}