package NesEmulator.cartridge

import cartridge.INesHeader
import common.MirroringMode

@ExperimentalUnsignedTypes
class NesFileHeader(
    headerBytes: UByteArray
) {

    companion object {
        const val KILOBYTES_SIZE_16 = 16384u
        const val KILOBYTES_SIZE_8 = 8192u
    }

    val inesHeader = INes(headerBytes)
    val nes2Header = Nes2(headerBytes)

    class INes(
        headerBytes: UByteArray
    ) {
        /**
         * Bytes 4 & 5
         */
        val programRomSize = headerBytes[INesHeader.PROGRAM_ROM_INDEX] * INesHeader.KILOBYTES_SIZE_16
        val characterRomSize = headerBytes[INesHeader.CHARACTER_ROM_INDEX] * INesHeader.KILOBYTES_SIZE_8

        /**
         * Byte 6
         * 7654 3 2 1 0
         * |||| | | | |
         * |||| | | | +- Mirroring: 0: horizontal (vertical arrangement) (CIRAM A10 = PPU A11)
         * |||| | | |              1: vertical (horizontal arrangement) (CIRAM A10 = PPU A10)
         * |||| | | +-- 1: Cartridge contains battery-backed PRG RAM ($6000-7FFF) or other persistent memory
         * |||| | +--- 1: 512-byte trainer at $7000-$71FF (stored before PRG data)
         * |||| +---- 1: Ignore mirroring control or above mirroring bit; instead provide four-screen VRAM
         * ++++----- Lower nybble of mapper number
         */
        val mirroring = when (headerBytes[6].toUInt() and 0x01u) {
            0u -> MirroringMode.HORIZONTAL
            else -> MirroringMode.VERTICAL
        }

        val hasPersistentMemory = when (headerBytes[6].toUInt() and 0x02u) {
            0u -> false
            else -> true
        }

        val hasTrainerSection = when (headerBytes[6].toUInt() and 0x04u) {
            0u -> false
            else -> true
        }

        val is4ScreenInstead = when (headerBytes[6].toUInt() and 0x08u) {
            0u -> false
            else -> true
        }

        val mapperIdLowerNibble = (headerBytes[6].toUInt() shr 4) and 0x0Fu

        /**
         * Byte 7
         * 7654  32 1 0
         * |||| || | |
         * |||| || | +- VS Unisystem
         * |||| || +-- PlayChoice-10 (8 KB of Hint Screen data stored after CHR data)
         * |||| ++--- If equal to 2, flags 8-15 are in NES 2.0 format
         * ++++----- Upper nybble of mapper number
         */
        val isVsUnisystem = when (headerBytes[7].toUInt() and 0x01u) {
            0u -> false
            else -> true
        }

        val isPlayChoice10 = when (headerBytes[7].toUInt() and 0x02u) {
            0u -> false
            else -> true
        }

        val isNes2Format = when ((headerBytes[7].toUInt() shr 2) and 0x03u) {
            2u -> true
            else -> false
        }

        val mapperIdUpperNibble = (headerBytes[7].toUInt() shr 4) and 0x0Fu

        /**
         * Byte 8
         * 76543210
         * ||||||||
         * ++++++++- PRG RAM size
         */
        val programRamSize = if (headerBytes[8].toUInt() == 0u)
                KILOBYTES_SIZE_8
            else
                headerBytes[8].toUInt() * KILOBYTES_SIZE_8

        /**
         * Byte 9
         * 76543210
         * ||||||||
         * |||||||+- TV system (0: NTSC; 1: PAL)
         * +++++++-- Reserved, set to zero
         */
        val tvSystem = when (headerBytes[9].toUInt() and 0x01u) {
            0u -> TvSystem.NTSC
            else -> TvSystem.PAL
        }
    }

    class Nes2(
        headerBytes: UByteArray
    ) {

    }

    init {

    }
}
