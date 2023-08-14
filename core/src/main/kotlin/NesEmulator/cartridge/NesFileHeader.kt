package NesEmulator.cartridge

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
        val programRomSize = headerBytes[4] * KILOBYTES_SIZE_16
        val characterRomSize = headerBytes[5] * KILOBYTES_SIZE_8

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
        /**
         * Bytes 4 & 5
         */
        val programRomSizeLSB = headerBytes[4] * KILOBYTES_SIZE_16
        val characterRomSizeLSB = headerBytes[5] * KILOBYTES_SIZE_8

        /**
         * Byte 6
         * D~7654 3210
         *  ---------
         *  NNNN FTBM
         *  |||| |||+-- Hard-wired nametable mirroring type
         *  |||| |||     0: Horizontal (vertical arrangement) or mapper-controlled
         *  |||| |||     1: Vertical (horizontal arrangement)
         *  |||| ||+--- "Battery" and other non-volatile memory
         *  |||| ||      0: Not present
         *  |||| ||      1: Present
         *  |||| |+--- 512-byte Trainer
         *  |||| |      0: Not present
         *  |||| |      1: Present between Header and PRG-ROM data
         *  |||| +---- Hard-wired four-screen mode
         *  ||||        0: No
         *  ||||        1: Yes
         *  ++++------ Mapper Number D0..D3
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
         * D~7654 3210
         *  ---------
         *  NNNN 10TT
         *  |||| ||++- Console type
         *  |||| ||     0: Nintendo Entertainment System/Family Computer
         *  |||| ||     1: Nintendo Vs. System
         *  |||| ||     2: Nintendo Playchoice 10
         *  |||| ||     3: Extended Console Type
         *  |||| ++--- NES 2.0 identifier
         *  ++++------ Mapper Number D4..D7
         */
        val consoleType = when (headerBytes[7].toUInt() and 0x03u) {
            0u -> ConsoleType.NES
            1u -> ConsoleType.VSSYSTEM
            2u -> ConsoleType.PLAYCHOICE10
            else -> ConsoleType.EXTENDED
        }

        val isNes2Format = when ((headerBytes[7].toUInt() shr 2) and 0x03u) {
            2u -> true
            else -> false
        }

        val mapperIdX = (headerBytes[7].toUInt() shr 4) and 0x0Fu

        /**
         * Byte 8
         * Mapper MSB/Submapper
         * D~7654 3210
         *  ---------
         *  SSSS NNNN
         *  |||| ++++- Mapper number D8..D11
         *  ++++------ Submapper number
         */
        val mapperIdY = headerBytes[8].toUInt() and 0x0Fu
        val submapperNumber = (headerBytes[8].toUInt() shr 4) and 0x0Fu

        /**
         * Byte 9
         * D~7654 3210
         *  ---------
         *  CCCC PPPP
         *  |||| ++++- PRG-ROM size MSB
         *  ++++------ CHR-ROM size MSB
         */
        val programRomSizeMSB = headerBytes[9].toUInt() and 0x0Fu
        val characterRomSizeMSB = (headerBytes[9].toUInt() shr 4) and 0x0Fu

        /**
         * Byte 10
         * D~7654 3210
         *    ---------
         *    pppp PPPP
         *    |||| ++++- PRG-RAM (volatile) shift count
         *    ++++------ PRG-NVRAM/EEPROM (non-volatile) shift count
         *  If the shift count is zero, there is no PRG-(NV)RAM.
         *  If the shift count is non-zero, the actual size is
         *  "64 << shift count" bytes, i.e. 8192 bytes for a shift count of 7.
         */
        val programRamShiftCount = headerBytes[10].toUInt() and 0x0Fu
        val programNonVolatileRamShiftCount = (headerBytes[10].toUInt() shr 4) and 0x0Fu

        /**
         * Byte 11
         * D~7654 3210
         *    ---------
         *    cccc CCCC
         *    |||| ++++- CHR-RAM size (volatile) shift count
         *    ++++------ CHR-NVRAM size (non-volatile) shift count
         *  If the shift count is zero, there is no CHR-(NV)RAM.
         *  If the shift count is non-zero, the actual size is
         *  "64 << shift count" bytes, i.e. 8192 bytes for a shift count of 7.
         */
        val characterRamShiftCount = headerBytes[11].toUInt() and 0x0Fu
        val characterNonVolatileRamShiftCount = (headerBytes[11].toUInt() shr 4) and 0x0Fu

        /**
         * Byte 12 - CPU/PPU Timing
         * D~7654 3210
         *          ---------
         *          .... ..VV
         *                 ++- CPU/PPU timing mode
         *                      0: RP2C02 ("NTSC NES")
         *                      1: RP2C07 ("Licensed PAL NES")
         *                      2: Multiple-region
         *                      3: UA6538 ("Dendy")
         */
        val timingMode = when (headerBytes[12].toUInt() and 0x03u) {
            0u -> TvSystem.NTSC
            1u -> TvSystem.PAL
            2u -> TvSystem.MULTIREGIONAL
            else -> TvSystem.DENDY
        }
    }
}
