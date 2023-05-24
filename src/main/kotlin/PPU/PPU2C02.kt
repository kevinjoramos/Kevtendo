package PPU

import Bus.Bus

@ExperimentalUnsignedTypes
class PPU2C02 {
    lateinit var bus: Bus

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

    var maskRegister: UByte = 0x00u

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




    var statusRegister: UByte = 0x00u
    var oamAddressRegister: UByte = 0x00u
    var oamDataRegister: UByte = 0x00u
    var scrollRegister: UByte = 0x00u

    var addressRegister: UByte = 0x00u
    private var addressLatch: UShort = 0x0000u

    fun writeToAddressRegister(data: UByte) {
        addressRegister = data
        addressLatch = (addressLatch.toUInt() shl 8).toUShort() or data.toUShort()
    }


    var dataRegister: UByte = 0x00u
    fun readDataRegister(): UByte {



    }


    var oamDMARegister: UByte = 0x00u

    val leftPatternTable: UByteArray = UByteArray((0x1000u).toInt())
    val rightPatternTable: UByteArray = UByteArray((0x1000u).toInt())
    val nameTable: UByteArray = UByteArray((0x2000u).toInt())
    val paletteTable: UByteArray = UByteArray((0x0100u).toInt())
    val objectAtrributeMemory: UByteArray = UByteArray(256)

}