package ppu

import androidx.compose.ui.graphics.Color
import mediator.Component
import mediator.Mediator

@ExperimentalUnsignedTypes
class PPU2C02(override var bus: Mediator) : Component {

    private val controllerRegister = ControllerRegister()
    private val maskRegister = MaskRegister()
    private val statusRegister = StatusRegister()
    private val objectAttributeMemoryAddressRegister = ObjectAttributeMemoryAddressRegister()
    private val objectAttributeMemoryDataRegister = ObjectAttributeMemoryDataRegister()
    private val scrollRegister = ScrollRegister()
    private val addressRegister = AddressRegister()
    private val dataRegister: UInt = 0u
    private val objectAttributeMemoryDirectMemoryAccess = ObjectAttributeMemoryDirectMemoryAccess()

    fun writeToControllerRegister(data: UInt) {
        controllerRegister.value = data
    }

    fun writeToMaskRegister(data: UInt) {
        maskRegister.value = data
    }

    fun readStatusRegister(): UInt {
        val value = statusRegister.value
        statusRegister.clearBit7()
        addressRegister.clearAddressLatch()
        return value
    }

    fun writeToAddressRegister(data: UInt) {
        addressRegister.writeToAddressLatch(data)
    }

    fun readDataRegister(): UInt {
        return 0u
    }

    fun writeToDataRegister(data: UInt) {

    }

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