package ppu

import androidx.compose.ui.graphics.Color
import mediator.Component
import mediator.Mediator

@ExperimentalUnsignedTypes
class PPU2C02(override var bus: Mediator) : Component {

    val controllerRegister = ControllerRegister()


    /**
     * Mask Register
     */

    private var maskRegister: UByte = 0x00u

    val isGreyscale: Boolean
        get() = maskRegister and (0x01).toUByte() == (0x01u).toUByte()

    val showBackgroundInLeftMost: Boolean
        get() = maskRegister and (0x02).toUByte() == (0x02u).toUByte()

    val showSpritesInLeftMost: Boolean
        get() = maskRegister and (0x04).toUByte() == (0x04u).toUByte()

    val showBackground: Boolean
        get() = maskRegister and (0x08).toUByte() == (0x08u).toUByte()

    val showSprites: Boolean
        get() = maskRegister and (0x10).toUByte() == (0x10u).toUByte()

    val emphasizeRed: Boolean
        get() = maskRegister and (0x20).toUByte() == (0x20u).toUByte()

    val emphasizeGreen: Boolean
        get() = maskRegister and (0x40).toUByte() == (0x40u).toUByte()

    val emphasizeBlue: Boolean
        get() = maskRegister and (0x80).toUByte() == (0x80u).toUByte()

    fun writeToMaskRegister(data: UByte) {
        maskRegister = data
    }

    /**
     * Status Register
     */

    var statusRegister: UByte = 0x00u

    val hasSpriteOverflow: Boolean
        get() = statusRegister and (0x20).toUByte() == (0x20u).toUByte()

    val hasSpriteHit: Boolean
        get() = statusRegister and (0x40).toUByte() == (0x40u).toUByte()

    val isInVerticalBlank: Boolean
        get() = statusRegister and (0x80).toUByte() == (0x80u).toUByte()

    fun readStatusRegister(): UByte {
        addressLatch = 0x00u

        return statusRegister and (0x7Fu).toUByte()
    }

    var oamAddressRegister: UByte = 0x00u
    var oamDataRegister: UByte = 0x00u
    var scrollRegister: UByte = 0x00u

    /**
     * Address Register
     */

    var addressRegister: UByte = 0x00u
    private var addressLatch: UShort = 0x0000u

    fun writeToAddressRegister(data: UByte) {
        addressRegister = data
        addressLatch = (addressLatch.toUInt() shl 8).toUShort() or data.toUShort()
    }


    var dataRegister: UByte = 0x00u
    var isDataBufferPrimed = false
    fun readDataRegister(): UByte {
        if (addressLatch > 0x3EFFu) {
            val data = paletteTable[(addressLatch - 0x3F00u).mod(32u).toInt()]
            dataRegister = paletteTable[(addressLatch - 0x3F00u).mod(32u).toInt()]
            return data
        }

        val data = dataRegister

        dataRegister =
            if ( addressLatch < (0x2000u).toUShort() ) {
                patternTable[addressLatch.toInt()]
            } else if ( addressLatch < (0x3000u).toUShort()) {
                nameTable[((addressLatch - 0x2000u).mod(2048u)).toInt()]
            } else {
                0x0000u // this case should never occur.
            }

        addressRegister = (addressRegister + vramIncrement.toUInt()).toUByte()
        addressLatch = (addressLatch + vramIncrement.toUInt()).toUShort()

        return data
    }

    fun writeToDataRegister(data: UByte) {
        dataRegister = data

        if ( addressLatch < (0x2000u).toUShort() ) {
            patternTable[addressLatch.toInt()] = data
            return
        }

        if ( addressLatch < (0x3000u).toUShort()) {
            nameTable[((addressLatch - 0x2000u).mod(2048u)).toInt()] = data
            return
        }

        addressRegister = (addressRegister + vramIncrement.toUInt()).toUByte()
        addressLatch = (addressLatch + vramIncrement.toUInt()).toUShort()

        return
    }


    var oamDMARegister: UByte = 0x00u

    private val nameTable: UByteArray = UByteArray(NAMETABLE_MEMORY_SIZE)
    private val objectAttributeMemory: UByteArray = UByteArray(OAM_MEMORY_SIZE)
    private val paletteTable = UByteArray(PALETTE_TABLE_MEMORY_SIZE)

    companion object {
        const val NAMETABLE_MEMORY_SIZE = 0x2000
        const val OAM_MEMORY_SIZE = 0x100
        const val PALETTE_TABLE_MEMORY_SIZE = 0x20

        val colorLookUpTable = listOf(
            Color(0x626262),
            Color(0x0D226B),
            Color(0x241476),
            Color(0x3B0A6B),
            Color(0x4C074D),
            Color(0x520C24),
            Color(0x4C1700),
            Color(0x3B2600),
            Color(0x243400),
            Color(0x0D3D00),
            Color(0x004000),
            Color(0x003B24),
            Color(0x00304D),
            Color(0x000000),
            Color(0x000000),
            Color(0x000000),
            Color(0xABABAB),
            Color(0x3156B1),
            Color(0x5043C5),
            Color(0x7034BB),
            Color(0x892F95),
            Color(0x94345F),
            Color(0x8E4226),
            Color(0x795500),
            Color(0x5B6800),
            Color(0x3B7700),
            Color(0x227C15),
            Color(0x17774C),
            Color(0x1D6985),
            Color(0x000000),
            Color(0x000000),
            Color(0x000000),
            Color(0xFFFFFF),
            Color(0x7CAAFF),
            Color(0x9B96FF),
            Color(0xBD86FF),
            Color(0xD87EF1),
            Color(0xE682BA),
            Color(0xE38F7F),
            Color(0xD0A24E),
            Color(0xB2B734),
            Color(0x90C739),
            Color(0x74CE5C),
            Color(0x66CB92),
            Color(0x69BECE),
            Color(0x4E4E4E),
            Color(0x000000),
            Color(0x000000),
            Color(0x000000),
            Color(0xFFFFFF),
            Color(0xC9DEFC),
            Color(0xD5D6FF),
            Color(0xE2CFFF),
            Color(0xEECCFC),
            Color(0xF5CCE7),
            Color(0xF5D1CF),
            Color(0xEED8BB),
            Color(0xE2E1AE),
            Color(0xD5E8AE),
            Color(0xC9EBBB),
            Color(0xC2EBCF),
            Color(0xC2E6E7),
            Color(0xB8B8B8),
            Color(0x000000),
            Color(0x000000)
        )

    }


}