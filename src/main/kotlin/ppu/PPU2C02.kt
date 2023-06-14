package ppu

import androidx.compose.ui.graphics.Color
import mediator.Component
import mediator.Mediator

@ExperimentalUnsignedTypes
class PPU2C02(override var bus: Mediator) : Component {

    var ioBusLatch = 0u

    private val controllerRegister = ControllerRegister()
    private val maskRegister = MaskRegister()
    private val statusRegister = StatusRegister()
    private var objectAttributeMemoryAddressRegister = ObjectAttributeMemoryAddressRegister()
    private var objectAttributeMemoryDataRegister: UInt = 0u
    private val scrollRegister = ScrollRegister()
    private val addressRegister = AddressRegister()
    private var dataRegister: UInt = 0u
    private val directMemoryAccessRegister = ObjectAttributeMemoryDirectMemoryAccess()

    private val nameTableMirroringState = NameTableMirroring.HORIZONTAL

    fun run() {
        TODO("RENDER A PIXEL!!!!")
    }

    fun readControllerRegister(): UInt {
        return 0u
    }

    fun writeToControllerRegister(data: UInt) {
        controllerRegister.value = data
    }

    fun readMaskRegister(): UInt {
        return 0u
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

    fun writeToStatusRegister(data: UInt){

    }

    fun readAddressRegister(): UInt {
        return 0u
    }

    fun writeToAddressRegister(data: UInt) {
        addressRegister.writeToAddressLatch(data)
    }

    fun readDataRegister(): UInt {

        var data = dataRegister

        when (val address = addressRegister.readAddressFromLatch()) {
            in PATTERN_TABLE_ADDRESS_RANGE -> {
                dataRegister = readAddress((address + 0x6000u).toUShort()).toUInt()
            }
            in NAME_TABLE_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address)
                dataRegister = nameTable[nameTableAddress.toInt()].toUInt()
            }
            in NAME_TABLE_MIRROR_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address - MIRROR_OFFSET_FROM_NAMETABLE)
                dataRegister = nameTable[nameTableAddress.toInt()].toUInt()
            }
            in PALETTE_TABLE_ADDRESS_RANGE -> {
                val paletteAddress = (address - PALETTE_TABLE_ADDRESS_OFFSET)
                    .mod(PALETTE_TABLE_MEMORY_SIZE.toUInt()).toInt()

                dataRegister = paletteTable[paletteAddress].toUInt()
                data = dataRegister
            }
        }

        addressRegister.incrementAddressLatch(controllerRegister.vRamAddressIncrement)
        return data
    }

    fun writeToDataRegister(data: UInt) {
        dataRegister = data

        when (val address = addressRegister.readAddressFromLatch()) {
            in PATTERN_TABLE_ADDRESS_RANGE -> {
                writeToAddress((address + 0x6000u).toUShort(), data.toUByte())
            }
            in NAME_TABLE_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address)
                nameTable[nameTableAddress.toInt()] = data.toUByte()
            }
            in NAME_TABLE_MIRROR_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address - MIRROR_OFFSET_FROM_NAMETABLE)
                nameTable[nameTableAddress.toInt()] = data.toUByte()
            }
            in PALETTE_TABLE_ADDRESS_RANGE -> {
                val paletteAddress = (address - PALETTE_TABLE_ADDRESS_OFFSET)
                    .mod(PALETTE_TABLE_MEMORY_SIZE.toUInt()).toInt()

                paletteTable[paletteAddress] = data.toUByte()
            }
        }
        addressRegister.incrementAddressLatch(controllerRegister.vRamAddressIncrement)
    }

    private fun computeNameTableAddress(unmappedAddress: UInt): UInt {

        return when (nameTableMirroringState) {
            NameTableMirroring.HORIZONTAL -> {
                when (unmappedAddress) {
                    in NAME_TABLE_0_ADDRESS_RANGE -> {
                        unmappedAddress - 0x2000u
                    }
                    in NAME_TABLE_1_ADDRESS_RANGE -> {
                        unmappedAddress - 0x2400u
                    }
                    in NAME_TABLE_2_ADDRESS_RANGE -> {
                        (unmappedAddress - 0x2800u) + 0x400u
                    }
                    else -> {
                        (unmappedAddress - 0x2C00u) + 0x400u
                    }
                }
            }
            NameTableMirroring.VERTICAL -> {
                when (unmappedAddress) {
                    in NAME_TABLE_0_ADDRESS_RANGE -> {
                        unmappedAddress - NAME_TABLE_1_ADDRESS_OFFSET
                    }
                    in NAME_TABLE_1_ADDRESS_RANGE -> {
                        (unmappedAddress - 0x2400u) + 0x400u
                    }
                    in NAME_TABLE_2_ADDRESS_RANGE -> {
                        unmappedAddress - 0x2800u
                    }
                    else -> {
                        (unmappedAddress - 0x2C00u) + 0x400u
                    }
                }
            }
        }
    }

    fun readObjectAttributeMemoryAddressRegister(): UInt {
        return 0u
    }

    fun writeToObjectAttributeMemoryAddressRegister(data: UInt) {
        objectAttributeMemoryAddressRegister.value = data
    }

    fun readObjectAttributeMemoryDataRegister(): UInt {
        return objectAttributeMemory[objectAttributeMemoryAddressRegister.value.toInt()].toUInt()
    }

    fun writeToObjectAttributeMemoryDataRegister(data: UInt) {
        objectAttributeMemoryDataRegister = data
        objectAttributeMemory[objectAttributeMemoryAddressRegister.value.toInt()] = data.toUByte()
        objectAttributeMemoryAddressRegister.increment()
    }

    fun readScrollRegister(): UInt {
        return 0u
    }

    fun writeToScrollRegister(data: UInt) {

    }

    fun readDirectMemoryAccessRegister(): UInt {
        return 0u
    }

    fun writeToDirectMemoryAccessRegister(data: UInt) {
        directMemoryAccessRegister.value = data
        //TODO copy over page from cpu memory into oam.
    }

    private val nameTable: UByteArray = UByteArray(NAMETABLE_MEMORY_SIZE)
    private val objectAttributeMemory: UByteArray = UByteArray(OAM_MEMORY_SIZE)
    private val paletteTable = UByteArray(PALETTE_TABLE_MEMORY_SIZE)

    companion object {
        const val NAMETABLE_MEMORY_SIZE = 0x800
        const val OAM_MEMORY_SIZE = 0x100
        const val PALETTE_TABLE_MEMORY_SIZE = 0x20

        private val PATTERN_TABLE_ADDRESS_RANGE = 0x0000u..0x1FFFu
        private val NAME_TABLE_ADDRESS_RANGE = 0x2000u..0x2FFFu
        private val NAME_TABLE_MIRROR_ADDRESS_RANGE = 0x3000u..0x3EFFu
        private val PALETTE_TABLE_ADDRESS_RANGE =  0x3F00u..0x3FFFu
        private val NAME_TABLE_0_ADDRESS_RANGE = 0x2000u..0x23FFu
        private val NAME_TABLE_1_ADDRESS_RANGE = 0x2400u..0x27FFu
        private val NAME_TABLE_2_ADDRESS_RANGE = 0x2800u..0x2BFFu
        private val NAME_TABLE_3_ADDRESS_RANGE = 0x2C00u..0x2FFFu

        private val NAME_TABLE_1_ADDRESS_OFFSET = 0x2000u
        private val MIRROR_OFFSET_FROM_NAMETABLE = 0x1000u
        private val EXTRA_NAME_TABLE_1_ADDRESS_OFFSET = 0x3000
        private val PALETTE_TABLE_ADDRESS_OFFSET = 0x3F00u

        enum class NameTableMirroring {
            HORIZONTAL,
            VERTICAL
        }


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