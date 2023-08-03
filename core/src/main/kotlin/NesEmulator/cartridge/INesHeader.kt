package cartridge

import common.MirroringMode
import ppu.PPU2C02

@ExperimentalUnsignedTypes
class INesHeader(
    private val headerByteCode: UByteArray
)  {

    val programRomSize = headerByteCode[PROGRAM_ROM_INDEX] * SixteenKilobyteUnitSize

    val characterRomSize = headerByteCode[CHARACTER_ROM_INDEX] * EightKilobyteUnitSize

    lateinit var mirroringMode: MirroringMode


    /**
     * Flags 6 – Mapper, mirroring, battery, trainer
     *
     * 76543210
     * ||||||||
     * |||||||+- Mirroring: 0: horizontal (vertical arrangement) (CIRAM A10 = PPU A11)
     * |||||||              1: vertical (horizontal arrangement) (CIRAM A10 = PPU A10)
     * ||||||+-- 1: Cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
     * |||||+--- 1: 512-byte trainer at $7000-$71FF (stored before PRG data)
     * ||||+---- 1: Ignore mirroring control or above mirroring bit; instead provide four-screen VRAM
     * ++++----- Lower nybble of mapper number
     */
    private val flag6: UInt = headerByteCode[FLAG_6_INDEX].toUInt()

    val isVerticalMirroringMode: Boolean
        get() = when (flag6 and 0x01u) {
            0u -> false
            else -> true
        }

    val hasPersistentMemory: Boolean
        get() = when (flag6 and 0x02u) {
            0u -> false
            else -> true
        }

    val hasTrainerSection: Boolean
        get() = when (flag6 and 0x04u) {
            0u -> false
            else -> true
        }

    val is4ScreenMirrorMode: Boolean
        get() = when (flag6 and 0x08u) {
            0u -> false
            else -> true
        }

    val mapperIdLowerNibble
        get() = (flag6 and 0xF0u) shr 4


    /**
     * 	Flags 7 – Mapper, VS/Playchoice, NES 2.0
     *
     * 7654 32 1 0
     * |||| || | |
     * |||| || | +- VS Unisystem
     * |||| || +-- PlayChoice-10 (8 KB of Hint Screen data stored after CHR data)
     * |||| ++--- If equal to 2, flags 8-15 are in NES 2.0 format
     * ++++----- Upper nybble of mapper number
     */
    private val flag7: UInt = headerByteCode[FLAG_7_INDEX].toUInt()

    val isVsUnisystem: Boolean
        get() = when (flag7 and 0x01u) {
            0u -> false
            else -> true
        }

    val isPlayChoice10: Boolean
        get() = when (flag7 and 0x02u) {
            0u -> false
            else -> true
        }

    val isNes2Format: Boolean
        get() = when ((flag7 and 0x0Cu) shr 2) {
            2u -> true
            else -> false
        }

    private val flag8: UInt = headerByteCode[FLAG_8_INDEX].toUInt()

    private val flag9: UInt = headerByteCode[FLAG_9_INDEX].toUInt()

    private val flag10: UInt = headerByteCode[FLAG_10_INDEX].toUInt()

    init {

    }

    companion object {
        const val SixteenKilobyteUnitSize = 16384u
        const val EightKilobyteUnitSize = 8192u

        const val PROGRAM_ROM_INDEX = 4
        const val CHARACTER_ROM_INDEX = 5
        const val FLAG_6_INDEX = 6
        const val FLAG_7_INDEX = 7
        const val FLAG_8_INDEX = 8
        const val FLAG_9_INDEX = 9
        const val FLAG_10_INDEX = 10
    }
}
