package PPU

import Bus.Bus
import mediator.Component
import mediator.Event
import mediator.Mediator

@ExperimentalUnsignedTypes
class PPU2C02(override var bus: Mediator) : Component {

    /**
     * Control Register
     */

    private var controlRegister: UByte = 0x00u
    val baseNameTableAddress: UShort
        get() {
            return when (controlRegister and 0x03u) {
                (0u).toUByte() -> 0x2000u
                (1u).toUByte() -> 0x2400u
                (2u).toUByte() -> 0x2800u
                else -> 0x2C00u
            }
        }

    val vramIncrement: Int
        get() = if (controlRegister and (0x04u).toUByte() == (0x04u).toUByte()) 32 else 1

    val spritePatternTableAddress8x8: UShort
        get() = if (controlRegister and (0x08).toUByte() == (0x80u).toUByte()) 0x1000u else 0x0000u

    val backgroundPatternTableAddress: UShort
        get() = if (controlRegister and (0x10).toUByte() == (0x10u).toUByte()) 0x1000u else 0x0000u

    val spriteSize: SpriteSize
        get() = if (controlRegister and (0x20).toUByte() == (0x10u).toUByte())
            SpriteSize.EIGHT_X_SIXTEEN else SpriteSize.EIGHT_X_EIGHT
    enum class SpriteSize {
        EIGHT_X_EIGHT, EIGHT_X_SIXTEEN
    }

    val masterSlaveSelect: Boolean
        get() = controlRegister and (0x40).toUByte() == (0x40u).toUByte()

    val generateNMIAtStartVBlank: Boolean
        get() = controlRegister and (0x80).toUByte() == (0x80u).toUByte()

    fun writeToControlRegister(data: UByte) {
        controlRegister = data
    }

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

    val patternTable: UByteArray = UByteArray(8192)
    val nameTable: UByteArray = UByteArray(2048)
    val paletteTable: UByteArray = UByteArray(32)
    val objectAttributeMemory: UByteArray = UByteArray(256)


}