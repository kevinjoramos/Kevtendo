package NesEmulator.cartridge

import com.sun.jdi.Mirror
import common.MirroringMode
import java.math.BigInteger
import kotlin.math.pow
import kotlin.time.times

@ExperimentalUnsignedTypes
class NesFileHeader(
    private val headerBytes: UByteArray
) {

    companion object {
        const val KILOBYTES_SIZE_16 = 16384u
        const val KILOBYTES_SIZE_8 = 8192u
    }

    val hasV2Format: Boolean = when ((headerBytes[7].toUInt() shr 2) and 0x03u) {
        2u -> true
        else -> false
    }

    val programRomSize: UInt =
        if (!hasV2Format) {
            headerBytes[4] * KILOBYTES_SIZE_16
        } else {
            val lsb = headerBytes[4].toUInt()
            val msb = headerBytes[9].toUInt() and 0x0Fu

            // if the msb is 0xE or less, than the size is combines from msb and lsb.
            // otherwise, an exponential modifier is used.
            if (msb in 0x0u..0xEu) {
                ((msb shl 8) or lsb) * KILOBYTES_SIZE_16
            } else {
                val multiplier = lsb and 0x03u
                val exponent = (lsb shr 2) and 0x3Fu

                // this is a "magic" formula from the NES2.0 spec.
                (2.0).pow(exponent.toInt()).toUInt() * ((multiplier * 2u) + 1u)
            }
        }

    val characterRomSize: UInt =
        if (!hasV2Format) {
            headerBytes[5] * KILOBYTES_SIZE_8
        } else {
            val lsb = headerBytes[5].toUInt()
            val msb = headerBytes[9].toUInt() and 0xF0u

            // if the msb is 0xE or less, than the size is combines from msb and lsb.
            // otherwise, an exponential modifier is used.
            if (msb in 0x0u..0xEu) {
                ((msb shl 8) or lsb) * KILOBYTES_SIZE_8
            } else {
                val multiplier = lsb and 0x03u
                val exponent = (lsb shr 2) and 0x3Fu

                // this is a "magic" formula from the NES2.0 spec.
                (2.0).pow(exponent.toInt()).toUInt() * ((multiplier * 2u) + 1u)
            }
        }

    val programRamSize: UInt =
        if (!hasV2Format) {
            if (headerBytes[8].toUInt() == 0u) {
                KILOBYTES_SIZE_8
            } else {
                headerBytes[8] * KILOBYTES_SIZE_8
            }
        } else {
            0u
        }

    val characterRamSize: UInt = 0u

    val mapperId: UInt =
        if (!hasV2Format) {
            (headerBytes[7].toUInt() and 0xF0u) or ((headerBytes[6].toUInt() shr 4) and 0x0Fu)
        } else {
            0u
        }

    val mirroringMode: MirroringMode
        get() {
            val initialMirroring = when (headerBytes[6].toUInt() and 0x01u) {
                0u -> MirroringMode.HORIZONTAL
                else -> MirroringMode.VERTICAL
            }

            return when (headerBytes[6].toUInt() and 0x08u) {
                0u -> initialMirroring
                else -> MirroringMode.FOUR_SCREEN
            }
        }

    val hasExtraStorage: Boolean =
        when (headerBytes[6].toUInt() and 0x02u) {
            0u -> false
            else -> true
        }

    val hasTrainer: Boolean =
        when (headerBytes[6].toUInt() and 0x04u) {
            0u -> false
            else -> true
        }
}
