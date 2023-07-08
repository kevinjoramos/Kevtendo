package ppu

import androidx.compose.ui.graphics.Color
import mediator.Component
import mediator.Event
import mediator.Mediator
import mediator.Sender
import util.to2DigitHexString
import util.to4DigitHexString

@ExperimentalUnsignedTypes
class PPU2C02(
    override var bus: Mediator
) : Component {

    /**
     * Basic Registers
     */
    private val controllerRegister = ControllerRegister()
    private val maskRegister = MaskRegister()
    private val statusRegister = StatusRegister()
    private var oamAddressRegister = OamAddressRegister()
    private var oamDataRegister: UInt = 0u
    private var dataRegister: UInt = 0u
    private val dmaRegister = OamDma()
    private var staleBus: UInt = 0u

    /**
     * VRam Registers
     */
    private val vRegister = VRegister()
    private val tRegister = TRegister()
    private var vRamHasFirstWrite = false
    private var fineX: UInt = 0u
        set(value) { field = value and THREE_BITMASK }

    private val nameTableMirroringState = NameTableMirroring.HORIZONTAL

    /**
     * Memories
     */
    private val nameTable: UByteArray = UByteArray(NAMETABLE_MEMORY_SIZE)
    private val objectAttributeMemory: UByteArray = UByteArray(OAM_MEMORY_SIZE)
    private val paletteTable = UByteArray(PALETTE_TABLE_MEMORY_SIZE)

    /**
     * Scanline Rendering
     */
    val frameBuffer = Array(240) { Array(257) { (0x01u).toUByte() } }
    private var scanline = 0
    private var cycles = 0
    private var nameTableAddress = 0x0000u
    private val xPosition get() = (nameTableAddress and 0x02u) shr 1
    private val yPosition get() = nameTableAddress and 0x40u shr 6
    private var patternTileAddress = 0x0000u
    private var attributeData = 0x0000u
    private val topLeft get() = attributeData and 0x3u
    private val topRight get() = (attributeData shr 2) and 0x3u
    private val bottomLeft get() = (attributeData shr 4) and 0x3u
    private val bottomRight get() = (attributeData shr 6) and 0x3u

    private var tileLowerBitPlane = 0x00u
    private var tileHigherBitPlane = 0x00u

    /**
     * Shift Registers
     */
    private var upperBackGroundShiftRegister: UInt = 0u
    private var lowerBackgroundShiftRegister: UInt = 0u
    private var upperPaletteShiftRegister: UInt = 0u
    private var lowerPaletteShiftRegister: UInt = 0u

    private var testCounter = 5
    fun testPrintNameTable() {
        val nameTableState = nameTable.slice(0..(0x400u - 1u).toInt()).chunked(32)
        for (row in nameTableState) {
            println("")
            for (tile in row) {
                print("${tile.toUInt().to2DigitHexString()} ")
            }
        }
    }

    fun testPrintFrameBuffer() {
        for (row in frameBuffer) {
            println("")
            for (color in row) {
                print("${color.toUInt().to2DigitHexString()} ")
            }
        }
    }

    fun run() {

        if (testCounter == 0) {
            //testPrintNameTable()
            //testPrintFrameBuffer()
            //println("")
            //println("")
            testCounter = 50
        } else testCounter--

        // Visible Scanlines + Pre-render
        if (scanline in 0..239 || scanline == 261) {

            // Output Pixels
            if (scanline != 261 && cycles in 1..256 ) {
                drawPixel(
                    scanline,
                    cycles,
                    (lowerBackgroundShiftRegister and 0x1u) or ((upperBackGroundShiftRegister and 0x1u) shl 1),
                    (lowerPaletteShiftRegister and 0x1u) or ((upperPaletteShiftRegister and 0x1u) shl 1),
                )

                lowerBackgroundShiftRegister = lowerPaletteShiftRegister shr 1
                upperBackGroundShiftRegister = upperBackGroundShiftRegister shr 1
                lowerPaletteShiftRegister = lowerPaletteShiftRegister shr 1
                upperPaletteShiftRegister = upperPaletteShiftRegister shr 1
            }


            // 1 - 256 -> Visible Pixels.
            // 257 - 320 Garbage Tiles
            // 321 - 336 Next 2 Tiles
            if (cycles in 1..336) {
                when (cycles % 8) {
                    0 -> {
                        println("")
                        println("")
                        nameTableAddress = vRegister.tileAddress
                        patternTileAddress = nameTable[computeNameTableAddress(nameTableAddress).toInt()].toUInt()
                        println("NameTableAddress: ${nameTableAddress.to4DigitHexString()}")
                        println("TileAddress: ${patternTileAddress.to4DigitHexString()}")
                    }
                    2 -> {
                        // Attribute Byte
                        var attributeData = nameTable[computeNameTableAddress(vRegister.attributeDataAddress).toInt()].toUInt()
                        println("Attribute Address ${vRegister.attributeDataAddress.to4DigitHexString()}")
                        println("Attribute Data ${attributeData.to4DigitHexString()}")
                    }
                    4 -> {
                        // Pattern Tile Low
                        tileLowerBitPlane = readPatternTableMemoryAddress(patternTileAddress + vRegister.fineY)
                        println("Lower Pattern Plane ${tileLowerBitPlane.to2DigitHexString()}")

                    }
                    6 -> {
                        // Pattern Tile High
                        tileHigherBitPlane = readPatternTableMemoryAddress(patternTileAddress + vRegister.fineY + 8u)
                        println("Higher Pattern Plane ${tileHigherBitPlane.to2DigitHexString()}")

                    }
                    7 -> {
                        // Load shift registers.
                        upperBackGroundShiftRegister = (upperBackGroundShiftRegister and 0xFF00u.inv()) or (tileHigherBitPlane shl 8)
                        lowerBackgroundShiftRegister = (lowerBackgroundShiftRegister and 0xFF00u.inv()) or (tileLowerBitPlane shl 8)
                        println("Upper BG SHIFT ${upperBackGroundShiftRegister.to4DigitHexString()}")
                        println("Lower BG SHIFT ${lowerBackgroundShiftRegister.to4DigitHexString()}")

                        var highBit = 0u
                        var lowBit = 0u

                        if (xPosition == 0u && yPosition == 0u) {
                            highBit = topLeft and 0x2u
                            lowBit = topLeft and 0x1u
                        }

                        if (xPosition == 1u && yPosition == 0u) {
                            highBit = topRight and 0x2u
                            lowBit = topRight and 0x1u
                        }

                        if (xPosition == 0u && yPosition == 1u) {
                            highBit = bottomLeft and 0x2u
                            lowBit = bottomLeft and 0x1u
                        }

                        if (xPosition == 1u && yPosition == 1u) {
                            highBit = bottomRight and 0x2u
                            lowBit = bottomRight and 0x1u
                        }

                        upperPaletteShiftRegister = if (highBit == 1u) 0xFFu else 0u
                        lowerPaletteShiftRegister = if (lowBit == 1u) 0xFFu else 0u

                        println("Upper BG SHIFT ${upperPaletteShiftRegister.to4DigitHexString()}")
                        println("Lower BG SHIFT ${lowerPaletteShiftRegister.to4DigitHexString()}")

                        // Increment X every 8 cycles when between 328 and 256 of next scanline.
                        if (328 >= cycles && cycles <= 256) {
                            vRegister.incrementCoarseX()
                        }
                    }
                }
            }

            // Increment Y values in vRegister. Skips attribute tables.
            if (cycles == 256) {
                vRegister.incrementY()
            }

            // Horizontal bits in V = Horizontal bits in T
            if (cycles == 257) {
                vRegister.value = vRegister.value and (0x41Fu).inv()
                vRegister.value = vRegister.value or (tRegister.value and 0x41Fu)
            }
        }

        // Post Render Scanline
        if (scanline == 240) {
            // DOES NOTHING
        }

        // Vertical Blanking Lines
        if (scanline in 241..260) {

            // Emit NMI on 2nd tick of scanline 241
            if (scanline == 241 && cycles == 1) {
                statusRegister.isInVBlank = true
                if (controllerRegister.generateNMIAtStartVBlank) {
                    emitNMISignal()
                }
            }

        }

        // Pre-Render Scanline
        if (scanline == 261) {
            if (cycles == 1) {
                statusRegister.isInVBlank = false
            }

            // Repeatedly copy vertical bits v = vertical bits t.
            if (cycles in 280..304) {
                vRegister.value = vRegister.value and (0x7BE0u).inv()
                vRegister.value = vRegister.value or (tRegister.value and (0x7BE0u))
            }
        }

        // Increment X And Y Over Entire Area.
        if (cycles < 341) {
            cycles++
        } else {
            if (scanline < 262) {
                scanline++
            } else {
                scanline = 0
            }
            cycles  = 0
        }


        /*
        if (scanline in 0..239 || scanline == 261) {

            if (scanline in 0..239 && cycles in 0..256) {
                drawPixel(
                    scanline,
                    cycles,
                    (lowerBackgroundShiftRegister and 0x1u) or ((upperBackGroundShiftRegister and 0x1u) shl 1),
                    (lowerPaletteShiftRegister and 0x1u) or ((upperPaletteShiftRegister and 0x1u) shl 1),
                )

                lowerBackgroundShiftRegister = lowerPaletteShiftRegister shr 1
                upperBackGroundShiftRegister = upperBackGroundShiftRegister shr 1
                lowerPaletteShiftRegister = lowerPaletteShiftRegister shr 1
                upperPaletteShiftRegister = upperPaletteShiftRegister shr 1
            }

            if (cycles in 1..336) {
                when (cycles % 8) {
                    0 -> {
                        println(vRegister.tileAddress.to2DigitHexString())
                        nameTableAddress = vRegister.tileAddress
                        patternTileAddress = nameTable[computeNameTableAddress(nameTableAddress).toInt()].toUInt()
                    }
                    2 -> {
                        // Attribute Byte
                        var attributeData = nameTable[computeNameTableAddress(vRegister.attributeDataAddress).toInt()].toUInt()
                    }
                    4 -> {
                        // Pattern Tile Low
                        tileLowerBitPlane = readPatternTableMemoryAddress(
                            patternTileAddress + vRegister.fineY
                        )
                    }
                    6 -> {
                        // Pattern Tile High
                        tileHigherBitPlane = readPatternTableMemoryAddress(
                            patternTileAddress + vRegister.fineY + 8u
                        )
                    }
                    7 -> {
                        // Load shift registers.
                        upperBackGroundShiftRegister = upperBackGroundShiftRegister or (tileHigherBitPlane shl 8)
                        lowerBackgroundShiftRegister = lowerBackgroundShiftRegister or (tileLowerBitPlane shl 8)

                        var highBit = 0u
                        var lowBit = 0u

                        if (xPosition == 0u && yPosition == 0u) {
                            highBit = topLeft and 0x2u
                            lowBit = topLeft and 0x1u
                        }

                        if (xPosition == 1u && yPosition == 0u) {
                            highBit = topRight and 0x2u
                            lowBit = topRight and 0x1u
                        }

                        if (xPosition == 0u && yPosition == 1u) {
                            highBit = bottomLeft and 0x2u
                            lowBit = bottomLeft and 0x1u
                        }

                        if (xPosition == 1u && yPosition == 1u) {
                            highBit = bottomRight and 0x2u
                            lowBit = bottomRight and 0x1u
                        }

                        upperPaletteShiftRegister = if (highBit == 1u) 0xFFu else 0u
                        lowerPaletteShiftRegister = if (lowBit == 1u) 0xFFu else 0u

                        // Increment Coarse X.
                        if (cycles <= 256 || cycles >= 328) {
                            vRegister.incrementCoarseX()
                        }
                    }
                }
                if (cycles == 256) {
                    vRegister.incrementY()
                }
            }

            if (cycles == 257) {
                vRegister.value = vRegister.value and (0x41Fu).inv()
                vRegister.value = vRegister.value or (tRegister.value and 0x41Fu)
            }

            if (scanline == 261) {

                if (cycles == 1) statusRegister.isInVBlank = false

                if (cycles in 280..304) {
                    vRegister.value = vRegister.value and (0x7BE0u).inv()
                    vRegister.value = vRegister.value or (tRegister.value and (0x7BE0u))
                }
            }
        }

        if (scanline in 241..260) {
            if (scanline == 241 && cycles == 1) {
                //testPrintFrameBuffer()
                    //println("")
                    //println("")

                statusRegister.isInVBlank = true

                if (controllerRegister.generateNMIAtStartVBlank) {
                    emitNMISignal()
                }
            }
        }

        // Multidimensional array index wrapping.
        if (cycles < 340) {
            cycles++
        } else {
            if (scanline < 261) scanline++ else scanline = 0
            cycles = 0
        }


         */
    }

    private fun drawPixel(scanline: Int, cycle: Int, colorSelect: UInt, paletteSelect: UInt) {

        /*println(readPaletteTableAddress(
            (1u + (4u * paletteSelect) + colorSelect)
        ).toUByte())*/

        frameBuffer[scanline][cycle] = readPaletteTableAddress(
            (1u + (4u * paletteSelect) + colorSelect)
        ).toUByte()
    }

    private fun emitNMISignal() {
        notify(Sender.PPU, Event.NMI)
    }

    /**
     * Read And Writes To Controller Register $2000
     */
    fun readControllerRegister(): UInt {
        return staleBus
    }

    fun writeToControllerRegister(data: UInt) {
        staleBus = data
        controllerRegister.value = data
        tRegister.nameTableSelect = data
    }

    /**
    * Read And Writes To Mask Register $2001
    */
    fun readMaskRegister(): UInt {
        return staleBus
    }

    fun writeToMaskRegister(data: UInt) {
        staleBus = data
        maskRegister.value = data
    }

    /**
     * Read And Writes To Status Register $2002
     */
    fun readStatusRegister(): UInt {
        val value = statusRegister.value
        statusRegister.clearBit7()
        vRamHasFirstWrite = false
        return value
    }

    fun writeToStatusRegister(data: UInt) {
        staleBus = data
    }

    /**
     * Read And Writes To Address Register $2006
     */
    fun readAddressRegister(): UInt {
        return staleBus
    }

    fun writeToAddressRegister(data: UInt) {
        staleBus = data

        if (!vRamHasFirstWrite) {
            tRegister.upperLatch = data
            vRamHasFirstWrite = true
        } else {
            tRegister.lowerLatch = data
            vRegister.value = tRegister.value
            vRamHasFirstWrite = false
        }
    }

    /**
     * Read And Writes To Data Register
     */
    fun readDataRegister(): UInt {
        var data = dataRegister

        if (vRegister.value in PALETTE_TABLE_ADDRESS_RANGE) {
            dataRegister = readPaletteTableAddress(vRegister.value)
            data = dataRegister
        } else {
            dataRegister = readPPUMemory(vRegister.value)
        }

        vRegister.value += controllerRegister.vRamAddressIncrement

        return data
    }

    fun writeToDataRegister(data: UInt) {
        staleBus = data

        dataRegister = data

        writeToPPUMemory(vRegister.value, data)

        vRegister.value += controllerRegister.vRamAddressIncrement
    }

    /**
     * Read And Writes To OAM Address Register
     */
    fun readOamAddressRegister(): UInt {
        return staleBus
    }

    fun writeToOamAddressRegister(data: UInt) {
        staleBus = data
        oamAddressRegister.value = data
    }

    /**
     * Read And Writes To OAM Data Register
     */
    fun readOamDataRegister(): UInt {
        return objectAttributeMemory[oamAddressRegister.value.toInt()].toUInt()
    }

    fun writeToOamDataRegister(data: UInt) {
        staleBus = data

        oamDataRegister = data
        objectAttributeMemory[oamAddressRegister.value.toInt()] = data.toUByte()
        oamAddressRegister.increment()
    }

    /**
     * Read And Writes To Scroll Register $2005
     */
    fun readScrollRegister(): UInt {
        return staleBus
    }

    fun writeToScrollRegister(data: UInt) {
        staleBus = data

        if (!vRamHasFirstWrite) {
            tRegister.coarseX = data
            fineX = data
            vRamHasFirstWrite = true
        } else {
            tRegister.coarseY = data
            tRegister.fineY = data
            vRamHasFirstWrite = false
        }
    }

    /**
     * Read And Writes To OAM DMA Register
     */
    fun readDMARegister(): UInt {
        return staleBus
    }

    fun writeToDMARegister(data: UInt) {
        staleBus = data

        dmaRegister.value = data
        //TODO copy over page from cpu memory into oam.
    }

    /**
     * PPU Memory Access
     */
    private fun readPPUMemory(address: UInt): UInt {
        var data = 0u
        when (address) {
            in PATTERN_TABLE_ADDRESS_RANGE -> {
                data = readPatternTableMemoryAddress(address)
            }
            in NAME_TABLE_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address)
                data = nameTable[nameTableAddress.toInt()].toUInt()
            }
            in NAME_TABLE_MIRROR_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address - MIRROR_OFFSET_FROM_NAMETABLE)
                data = nameTable[nameTableAddress.toInt()].toUInt()
            }
            in PALETTE_TABLE_ADDRESS_RANGE -> {
               data = readPaletteTableAddress(address)
            }
        }

        return data
    }

    private fun writeToPPUMemory(address: UInt, data: UInt) {

        when (address) {
            in PATTERN_TABLE_ADDRESS_RANGE -> {
                writeToPatternTableMemoryAddress(address, data)
            }
            in NAME_TABLE_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address)
                println("NAMETABLE ADDESS = ${nameTableAddress.to4DigitHexString()}")
                nameTable[nameTableAddress.toInt()] = data.toUByte()
            }
            in NAME_TABLE_MIRROR_ADDRESS_RANGE -> {
                val nameTableAddress = computeNameTableAddress(address - MIRROR_OFFSET_FROM_NAMETABLE)
                nameTable[nameTableAddress.toInt()] = data.toUByte()
            }
            in PALETTE_TABLE_ADDRESS_RANGE -> {
               writeToPaletteTableAddress(address, data)
            }
        }
    }

    private fun readPatternTableMemoryAddress(address: UInt): UInt {
        return readAddress((address + 0x6000u).toUShort()).toUInt()
    }

    private fun writeToPatternTableMemoryAddress(address: UInt, data: UInt) {
        writeToAddress((address + 0x6000u).toUShort(), data.toUByte())
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

    private fun readPaletteTableAddress(address: UInt): UInt {
        val paletteAddress = (address - PALETTE_TABLE_ADDRESS_OFFSET)
            .mod(PALETTE_TABLE_MEMORY_SIZE.toUInt()).toInt()

        return paletteTable[paletteAddress].toUInt()
    }

    private fun writeToPaletteTableAddress(address: UInt, data: UInt) {
        val paletteAddress = (address - PALETTE_TABLE_ADDRESS_OFFSET)
            .mod(PALETTE_TABLE_MEMORY_SIZE.toUInt()).toInt()

        paletteTable[paletteAddress] = data.toUByte()
    }


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
        private const val THREE_BITMASK = 0x7u

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