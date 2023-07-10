package ppu

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

    private var addressRegister: UInt = 0u
        set(value) { field = value and 0x3FFFu }

    /**
     * VRam Registers
     */
    private val vRegister = VRegister()
    private val tRegister = TRegister()
    private var vRamHasFirstWrite = false
    private var fineX: UInt = 0u
        set(value) { field = value and 0x07u }

    private val nameTableMirroring = NameTableMirroring.HORIZONTAL

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
    private var patternTileAddress = 0x0000u
    private var attributeBits = 0u
    private var tileLowBitPlane = 0x00u
    private var tileHighBitPlane = 0x00u

    /**
     * Shift Registers
     */
    private var highBackGroundShiftRegister: UInt = 0u
    private var lowBackgroundShiftRegister: UInt = 0u
    private var highPaletteShiftRegister: UInt = 0u
    private var lowPaletteShiftRegister: UInt = 0u

    private var testCounter = 5
    fun testPrintNameTable() {
        val nameTables = nameTable.chunked(0x400)
        for (row in nameTables[0].chunked(32)) {
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

    fun testPrintPaletteTable() {
        println("")
        println(paletteTable[0].toUInt().to2DigitHexString())
        for (color in paletteTable.slice(1..3)) {
            print("${ color.toUInt().to2DigitHexString() } ")
        }
        println("")
        for (color in paletteTable.slice(5..7)) {
            print("${color.toUInt().to2DigitHexString()} ")
        }
        println("")
        for (color in paletteTable.slice(9..11)) {
            print("${color.toUInt().to2DigitHexString()} ")
        }
        println("")
        for (color in paletteTable.slice(13..15)) {
            print("${color.toUInt().to2DigitHexString()} ")
        }
        println("")
    }

    fun run() {

        // Visible Scanlines + Pre-render
        if (scanline in 0..239 || scanline == 261) {

            // Output Pixels
            if (scanline != 261 && cycles in 0..256 ) {  /// TODO Unless rendering is turned off?
                if (maskRegister.isShowingBackground) {

                    val lowBackgroundBit = when (lowBackgroundShiftRegister and 0x8000u) {
                        0u -> 0u
                        else -> 1u
                    }

                    val highBackgroundBit = when (highBackGroundShiftRegister and 0x8000u) {
                        0u -> 0u
                        else -> 1u
                    }

                    val lowPaletteBit = when (lowPaletteShiftRegister and 0x8000u) {
                        0u -> 0u
                        else -> 1u
                    }

                    val highPaletteBit = when (highPaletteShiftRegister and 0x8000u) {
                        0u -> 0u
                        else -> 1u
                    }

                    drawPixel(
                        scanline,
                        cycles,
                        (highBackgroundBit shl 1) or lowBackgroundBit,
                        (highPaletteBit shl 1) or lowPaletteBit,
                    )
                }
            }

            lowBackgroundShiftRegister = lowPaletteShiftRegister shl 1
            highBackGroundShiftRegister = highBackGroundShiftRegister shl 1
            lowPaletteShiftRegister = lowPaletteShiftRegister shl 1
            highPaletteShiftRegister = highPaletteShiftRegister shl 1

            // 1 - 256 -> Visible Pixels.
            // 257 - 320 Garbage Tiles
            // 321 - 336 Next 2 Tiles
            if (cycles in 1..336) {
                when (cycles % 8) {
                    0 -> {
                        // Fetch the pattern tile at current name table address.
                        patternTileAddress = readNameTableMemory(vRegister.tileAddress)
                    }
                    2 -> {
                        // Fetch corresponding Attribute Byte
                        val attributeData = readNameTableMemory(vRegister.attributeDataAddress)
                        val quadrantAddress = (((vRegister.coarseY and 0x02u) shl 1) or (vRegister.coarseX and 0x02u))

                        attributeBits = when (quadrantAddress) {
                            0x00u -> attributeData and 0x03u
                            0x01u -> (attributeData shr 2) and 0x03u
                            0x10u -> (attributeData shr 4) and 0x03u
                            else -> (attributeData shr 6) and 0x03u
                        }
                    }
                    4 -> {
                        // Fetch low bit plane of pattern tile
                        tileLowBitPlane = readPatternTableMemory(
                            controllerRegister.backgroundPatternTableAddress +
                                    (patternTileAddress shl 4) +
                                    vRegister.fineY
                        )
                    }
                    6 -> {
                        // Fetch high bit plane of pattern tile
                        tileHighBitPlane = readPatternTableMemory(
                            controllerRegister.backgroundPatternTableAddress +
                                    (patternTileAddress shl 4) +
                                    (vRegister.fineY) + 8u
                        )
                    }
                    7 -> {

                        // Load shift registers.
                        lowBackgroundShiftRegister = (lowBackgroundShiftRegister and 0xFFu.inv()) or tileLowBitPlane
                        highBackGroundShiftRegister = (highBackGroundShiftRegister and 0xFFu.inv()) or tileHighBitPlane

                        // Load palette registers with attribute bits.
                        lowPaletteShiftRegister = when (attributeBits and 0x01u) {
                            0u -> 0u
                            else -> 0xFFFFu
                        }

                        highPaletteShiftRegister = when (attributeBits and 0x02u) {
                            0u -> 0u
                            else -> 0xFFFFu
                        }

                        // Increment X every 8 cycles when between 328 and 256 of next scanline.
                        if (cycles in 328..340 || cycles in 0..256) {
                            if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                                vRegister.incrementCoarseX()
                            }
                        }
                    }
                }
            }

            // Increment Y values in vRegister. Skips attribute tables.
            if (cycles == 256) {
                if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                    vRegister.incrementY()
                }
            }

            // Horizontal bits in V = Horizontal bits in T
            if (cycles == 257) {
                if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                    vRegister.value = vRegister.value and (0x041Fu).inv()
                    vRegister.value = vRegister.value or (tRegister.value and 0x041Fu)
                }
            }
        }

        // Post Render Scanline
        if (scanline == 240) {
            // DOES NOTHING

            //println()
            //testPrintPaletteTable()

            //testPrintNameTable()
            //println()

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
                if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                    vRegister.value = vRegister.value and (0x7BE0u).inv()
                    vRegister.value = (vRegister.value) or (tRegister.value and (0x7BE0u))
                }
            }
        }

        // Increment X And Y Over Entire Area.
        if (cycles < 340) {
            cycles++
        } else {
            if (scanline < 261) {
                scanline++
            } else {
                scanline = 0
            }
            cycles  = 0
        }
    }

    private fun drawPixel(scanline: Int, cycle: Int, colorSelect: UInt, paletteSelect: UInt) {

        frameBuffer[scanline][cycle] = readPaletteTableMemory(
            (paletteSelect shl 2) + colorSelect
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
        tRegister.parseNameTableSelect(data)
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
        statusRegister.isInVBlank = false

        vRamHasFirstWrite = false
        tRegister.value = 0u

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
            tRegister.parseUpperLatch(data)
            //addressRegister = (data shl 8)
            vRamHasFirstWrite = true
        } else {
            tRegister.parseLowerLatch(data)
            vRegister.value = tRegister.value
            //addressRegister = (addressRegister and 0xFF00u) or data
            vRamHasFirstWrite = false
        }
    }

    /**
     * Read And Writes To Data Register
     */
    fun readDataRegister(): UInt {
        var data = dataRegister

        // Palette read is not buffered.
        if (vRegister.value in 0x3F00u..0x3FFFu) {
            dataRegister = readPaletteTableMemory(vRegister.value)
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

        //println("WRITE TO DATA REGISTER: ${vRegister.value.to4DigitHexString()} : ${data.to2DigitHexString()}")

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
            tRegister.parseCoarseX(data)
            fineX = data
            vRamHasFirstWrite = true
        } else {
            tRegister.parseCoarseY(data)
            tRegister.parseFineY(data)
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
        return when (address) {

            // Pattern Table 0
            in 0x0000u..0x0FFFu -> readPatternTableMemory(address)

            // Pattern Table 1
            in 0x1000u..0x1FFFu -> readPatternTableMemory(address)

            // Name Tables 0-3 and Mirrors.
            in 0x2000u..0x3EFFu -> readNameTableMemory(address)

            // Pallet tables
            in 0x3F00u..0x3FFFu -> readPaletteTableMemory(address - 0x3F00u)

            // I'd have no idea how you got here if you did.
            else -> 0u
        }
    }

    private fun writeToPPUMemory(address: UInt, data: UInt) {
        when (address) {

            // Pattern Table 0
            in 0x0000u..0x0FFFu -> writeToPatternTableMemory(address, data)

            // Pattern Table 1
            in 0x1000u..0x1FFFu -> writeToPatternTableMemory(address, data)

            // Name Tables 0-3 and Mirrors.
            in 0x2000u..0x3EFFu -> writeToNameTableMemory(address, data)

            // Pallet tables
            in 0x3F00u..0x3FFFu -> writeToPaletteTableMemory(address - 0x3F00u, data)
        }
    }

    private fun readPatternTableMemory(address: UInt): UInt {
        return readAddress((address + 0x6000u).toUShort()).toUInt()
    }

    private fun writeToPatternTableMemory(address: UInt, data: UInt) {
        println("SOMEBODY TOUCH'A MY SPAGHET! = $${address.to4DigitHexString()} : ${data.to2DigitHexString()}")
        val number = 21
    }

    private fun readNameTableMemory(address: UInt): UInt {
        var nameTableAddress = address
        if (nameTableAddress in 0x3000u..0x3EFFu) {
            nameTableAddress -= 0x1000u
        }

        if (nameTableMirroring == NameTableMirroring.VERTICAL) {
            if (nameTableAddress in 0x2000u..0x23FFu) {
                return nameTable[(nameTableAddress - 0x2000u).toInt()].toUInt()
            }

            if (nameTableAddress in 0x2400u..0x27FFu) {
                return nameTable[(nameTableAddress - 0x2000u).toInt()].toUInt()
            }

            if (nameTableAddress in 0x2800u..0x2BFFu) {
                return nameTable[(nameTableAddress - 0x2800u).toInt()].toUInt()
            }

            if (nameTableAddress in 0x2C00u..0x2FFFu) {
                return nameTable[(nameTableAddress - 0x2800u).toInt()].toUInt()
            }
        }


        if (nameTableMirroring == NameTableMirroring.HORIZONTAL) {
            if (nameTableAddress in 0x2000u..0x23FFu) {
                return nameTable[(nameTableAddress - 0x2000u).toInt()].toUInt()
            }

            if (nameTableAddress in 0x2400u..0x27FFu) {
                return nameTable[(nameTableAddress - 0x2400u).toInt()].toUInt()
            }

            if (nameTableAddress in 0x2800u..0x2BFFu) {
                return nameTable[(nameTableAddress - 0x2400u).toInt()].toUInt()
            }

            if (nameTableAddress in 0x2C00u..0x2FFFu) {
                return nameTable[(nameTableAddress - 0x2800u).toInt()].toUInt()
            }
        }

        println("Someone tried to read a non existent address.")
        return 0u
    }

    private fun writeToNameTableMemory(address: UInt, data: UInt) {
        var nameTableAddress = address
        //println("Writing to name table memory. ${address.to4DigitHexString()} : ${data.to2DigitHexString()}")

        if (nameTableAddress in 0x3000u..0x3EFFu) {
            nameTableAddress -= 0x1000u
        }

        if (nameTableMirroring == NameTableMirroring.VERTICAL) {
            if (nameTableAddress in 0x2000u..0x23FFu) {
                nameTable[(nameTableAddress - 0x2000u).toInt()] = data.toUByte()
                return
            }

            if (nameTableAddress in 0x2400u..0x27FFu) {
                nameTable[(nameTableAddress - 0x2000u).toInt()] = data.toUByte()
                return
            }

            if (nameTableAddress in 0x2800u..0x2BFFu) {
                nameTable[(nameTableAddress - 0x2800u).toInt()] = data.toUByte()
                return
            }

            if (nameTableAddress in 0x2C00u..0x2FFFu) {
                nameTable[(nameTableAddress - 0x2800u).toInt()] = data.toUByte()
                return
            }
        }

        if (nameTableMirroring == NameTableMirroring.HORIZONTAL) {
            if (nameTableAddress in 0x2000u..0x23FFu) {
                nameTable[(nameTableAddress - 0x2000u).toInt()] = data.toUByte()
                return
            }

            if (nameTableAddress in 0x2400u..0x27FFu) {
                nameTable[(nameTableAddress - 0x2400u).toInt()] = data.toUByte()
                return
            }

            if (nameTableAddress in 0x2800u..0x2BFFu) {
                nameTable[(nameTableAddress - 0x2400u).toInt()] = data.toUByte()
                return
            }

            if (nameTableAddress in 0x2C00u..0x2FFFu) {
                nameTable[(nameTableAddress - 0x2800u).toInt()] = data.toUByte()
                return
            }
        }

        println("Someone tried to write to a non existent address.")
    }

    private fun readPaletteTableMemory(address: UInt): UInt {
        var paletteAddress = address.mod(0x20u)
        return paletteTable[paletteAddress.toInt()].toUInt()
    }

    private fun writeToPaletteTableMemory(address: UInt, data: UInt) {
        val paletteAddress = address.mod(0x20u)
        paletteTable[paletteAddress.toInt()] = data.toUByte()
    }


    companion object {
        const val NAMETABLE_MEMORY_SIZE = 0x800
        const val OAM_MEMORY_SIZE = 0x100
        const val PALETTE_TABLE_MEMORY_SIZE = 0x20

        enum class NameTableMirroring {
            HORIZONTAL,
            VERTICAL
        }
    }
}