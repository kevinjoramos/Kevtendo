package ppu

import common.MirroringMode
import mediator.Component
import mediator.Event
import mediator.Mediator
import mediator.Sender
import util.reverse
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

    private var oamAddressRegister: UInt = 0u
        set(value) { field = value and 0xFFu }

    private var oamDataRegister: UInt = 0u
        set(value) { field = value and 0xFFu }

    private var dataRegister: UInt = 0u
    var dmaRegister: UInt = 0u
        set(value) {field = value and 0xFFu}

    private var staleBus: UInt = 0u

    var overridingPalette: UInt? = null

    /**
     * VRam Registers
     */
    private val vRegister = VRegister()
    private val tRegister = TRegister()
    private var vRamHasFirstWrite = false
    private var fineX: UInt = 0u
        set(value) { field = value and 0x07u }

    private val nameTableMirroring = MirroringMode.VERTICAL

    /**
     * Memories
     */
    private val nameTable: UByteArray = UByteArray(NAMETABLE_MEMORY_SIZE)
    private val objectAttributeMemory = ObjectAttributeMemory()
    val paletteTable = UByteArray(PALETTE_TABLE_MEMORY_SIZE)

    /**
     * Scanline Rendering
     */
    val frameBuffer = Array(240) { UByteArray(256) { (0x0Fu).toUByte() } }
    private var scanline = 0
    private var cycles = 0
    private var patternTileAddress = 0x0000u
    private var attributeBits = 0u
    private var tileLowBitPlane = 0x00u
    private var tileHighBitPlane = 0x00u

    /**
     * Shift Registers
     */
    private var highBackgroundShiftRegister: UInt = 0u
    private var lowBackgroundShiftRegister: UInt = 0u
    private var highPaletteShiftRegister: UInt = 0u
    private var lowPaletteShiftRegister: UInt = 0u

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

    private var testCounter = 31

    private var testCounterY = 29

    fun run() {

        /**
         * Pre-Render Scanline
         * - Fill the shift registers with data for first 2 tiles of next scanline.
         * - No pixels rendered, but make same memory access.
         * - For odd frames, last cycle is skipped.
         * - Reload vertical bits v = t.
         */
        if (scanline == 261) {
            if (cycles == 1) {
                statusRegister.isInVBlank = false
                statusRegister.hasSpriteOverflow = false
                statusRegister.hasSpriteHit = false
            }

            // v: 0GHI A.BC DEF. .... <- t: 0GHI A.BC DEF. ....
            if (cycles in 280..304) {
                if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                    vRegister.value = vRegister.value and (0x7BE0u).inv()
                    vRegister.value = vRegister.value or (tRegister.value and 0x7BE0u)
                }
            }
        }

        /**
         * Visible Scanlines
         * cycle 0 -> idle cycle.
         * cycles 1 - 256 -> Memory access loops.
         * every 8 cycles from 9..257 reload registers.
         */
        if (scanline in 0..239 || scanline == 261) {

            //Reload shift Registers
            if ((cycles in 9..257 || cycles in 321..337 ) && (cycles.mod(8)) == 1) {
                // Load shift registers.
                lowBackgroundShiftRegister = (lowBackgroundShiftRegister and 0xFF00u) or tileLowBitPlane
                highBackgroundShiftRegister = (highBackgroundShiftRegister and 0xFF00u) or tileHighBitPlane

                // Load palette registers with attribute bits.
                lowPaletteShiftRegister = when (attributeBits and 0x01u) {
                    0u -> (lowPaletteShiftRegister and 0xFF00u)
                    else -> (lowPaletteShiftRegister and 0xFF00u) or 0xFFu
                }

                highPaletteShiftRegister = when ((attributeBits shr 1) and 0x01u) {
                    0u -> (highPaletteShiftRegister and 0xFF00u)
                    else -> (highPaletteShiftRegister and 0xFF00u) or 0xFFu
                }
            }

            // 1..256 Visible cycles
            // 257..320 Junk
            // 321..336 next 2 tiles on next scanline.
            if (cycles in 1..256 || cycles in 321..336) {
                when ((cycles - 1).mod(8)) {
                    0 -> {
                        // Fetch the pattern tile at current name table address.
                        patternTileAddress = readNameTableMemory(vRegister.tileAddress)
                    }

                    2 -> {
                        // Fetch corresponding Attribute Byte

                        val attributeData = readNameTableMemory(vRegister.attributeDataAddress)

                        val quadrantAddress = ((vRegister.coarseY and 0x02u) or ((vRegister.coarseX and 0x02u) shr 1))

                        attributeBits = when (quadrantAddress) {
                            0u -> attributeData and 0x03u
                            1u -> (attributeData shr 2) and 0x03u
                            2u -> (attributeData shr 4) and 0x03u
                            else -> (attributeData shr 6) and 0x03u
                        }

                        /*println("ATTRIB ADD: ${vRegister.attributeDataAddress.to4DigitHexString()}")
                        println("ATTRIB: ${attributeData.to4DigitHexString()}")
                        println("QUADRANT: ${quadrantAddress.to4DigitHexString()}")
                        println("AttributeBits: ${attributeBits.to4DigitHexString()}")
                        testPrintPaletteTable()
                        println("")*/
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
                }
            }

            if (cycles in 328..340 || cycles in 1..256) {
                if (cycles.mod(8) == 0) {
                    if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                        vRegister.incrementCoarseX()
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
            // v: 0... .A.. ...B CDEF <- t: 0... .A.. ...B CDEF
            if (cycles == 257) {
                if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                    vRegister.value = vRegister.value and (0x041Fu).inv()
                    vRegister.value = vRegister.value or (tRegister.value and 0x041Fu)
                }
            }

            if (scanline in 0..239) {

                if (cycles == 257) {
                    // Sprite Evaluation.
                    statusRegister.hasSpriteOverflow = objectAttributeMemory.evaluateNextEightSprites(
                        scanline.toUInt(),
                        controllerRegister.isSpriteSize8x16
                    )

                    /*println("OAM Eight Sprites:")
                    objectAttributeMemory.secondaryMemory.forEach { println(it) }
                    println()*/
                }

                if (cycles == 340) {

                    objectAttributeMemory.secondaryMemory.forEach { sprite ->
                        if (!controllerRegister.isSpriteSize8x16) {
                            if (sprite.isFlippedVertically) {
                                sprite.lowSpriteShiftRegister = readPatternTableMemory(
                                    controllerRegister.squareSpritePatternTableAddress +
                                            (sprite.tileIndex shl 4) + (7u - (scanline.toUInt() - sprite.yPosition))
                                )

                                sprite.highSpriteShiftRegister = readPatternTableMemory(
                                    controllerRegister.squareSpritePatternTableAddress +
                                            (sprite.tileIndex shl 4) + (7u - (scanline.toUInt() - sprite.yPosition)) + 8u
                                )
                            } else {
                                sprite.lowSpriteShiftRegister = readPatternTableMemory(
                                    controllerRegister.squareSpritePatternTableAddress +
                                            (sprite.tileIndex shl 4) + (scanline.toUInt() - sprite.yPosition)
                                )

                                sprite.highSpriteShiftRegister = readPatternTableMemory(
                                    controllerRegister.squareSpritePatternTableAddress +
                                            (sprite.tileIndex shl 4) + (scanline.toUInt() - sprite.yPosition) + 8u
                                )
                            }

                        } else {
                            if (sprite.isFlippedVertically) {
                                if (sprite.yPosition - scanline.toUInt() < 8u) {

                                    // Top Half
                                    sprite.lowSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                (((sprite.tileIndex and 0xFEu) + 1u) shl 4) +
                                                (7u - (scanline.toUInt() - sprite.yPosition) and 0x07u)
                                    )

                                    sprite.highSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                (((sprite.tileIndex and 0xFEu) + 1u) shl 4) +
                                                (7u - (scanline.toUInt() - sprite.yPosition) and 0x07u) + 8u
                                    )

                                } else {

                                    // Bottom Half


                                    sprite.lowSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                ((sprite.tileIndex and 0xFEu) shl 4) +
                                                ((scanline.toUInt() - sprite.yPosition) and 0x07u)
                                    )

                                    sprite.highSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                ((sprite.tileIndex and 0xFEu) shl 4) +
                                                ((scanline.toUInt() - sprite.yPosition) and 0x07u) + 8u
                                    )

                                }
                            } else { // not flipped vertically.
                                if (sprite.yPosition - scanline.toUInt() < 8u) {

                                    // Top Half
                                    sprite.lowSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                ((sprite.tileIndex and 0xFEu) shl 4) +
                                                ((scanline.toUInt() - sprite.yPosition) and 0x07u)
                                    )

                                    sprite.highSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                ((sprite.tileIndex and 0xFEu) shl 4) +
                                                ((scanline.toUInt() - sprite.yPosition) and 0x07u) + 8u
                                    )
                                } else {

                                    // Bottom Half
                                    sprite.lowSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                (((sprite.tileIndex and 0xFEu) + 1u) shl 4) +
                                                ((scanline.toUInt() - sprite.yPosition) and 0x07u)
                                    )

                                    sprite.highSpriteShiftRegister = readPatternTableMemory(
                                        ((sprite.tileIndex and 0x01u) shl 12) +
                                                (((sprite.tileIndex and 0xFEu) + 1u) shl 4) +
                                                ((scanline.toUInt() - sprite.yPosition) and 0x07u) + 8u
                                    )

                                }
                            }
                        }

                        // Flip horizontally if necessary.
                        if (sprite.isFlippedHorizontally) {
                            sprite.lowSpriteShiftRegister = sprite.lowSpriteShiftRegister.toUByte().reverse().toUInt()
                            sprite.highSpriteShiftRegister = sprite.highSpriteShiftRegister.toUByte().reverse().toUInt()
                        }

                    }
                }
            }


            if (cycles in 257..320) {
                // GARBAGE FETCHES
            }


        }

        // Post-Render scanline
        if (scanline == 240) {
            // IDLE SCANLINE
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

        // Output Pixels
        if (scanline in 0..239 && cycles in 1..256) {

            val activeSprite = if (maskRegister.isShowingSprites) {
                objectAttributeMemory.getPrioritizedActiveSprite()
            } else {
                ObjectAttributeMemory.Sprite()
            }
            val spriteColorSelect = (((activeSprite.highSpriteShiftRegister and 0x80u) shr 6) or ((activeSprite.lowSpriteShiftRegister and 0x80u) shr 7))
            val spritePaletteSelect = activeSprite.palette
            val spritePriority = activeSprite.hasPriority

            if (maskRegister.isShowingSprites) objectAttributeMemory.shiftAllActiveSprites()

            var backgroundColorSelect = 0u
            var backgroundPaletteSelect = 0u
            if (maskRegister.isShowingBackground) {

                val multiplexer = 0x8000u shr fineX.toInt()

                val lowBackgroundBit = when (lowBackgroundShiftRegister and multiplexer) {
                    0u -> 0u
                    else -> 1u
                }

                val highBackgroundBit = when (highBackgroundShiftRegister and multiplexer) {
                    0u -> 0u
                    else -> 1u
                }

                val lowPaletteBit = when (lowPaletteShiftRegister and multiplexer) {
                    0u -> 0u
                    else -> 1u
                }

                val highPaletteBit = when (highPaletteShiftRegister and multiplexer) {
                    0u -> 0u
                    else -> 1u
                }

                backgroundColorSelect = (highBackgroundBit shl 1) or lowBackgroundBit
                backgroundPaletteSelect = (highPaletteBit shl 1) or lowPaletteBit
            }

            if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {

                var finalColorSelect = 0u
                var finalPaletteSelect = 0u

                if (backgroundColorSelect == 0u && spriteColorSelect == 0u) {
                    finalColorSelect = 0u
                    finalPaletteSelect = 0u
                }

                if (backgroundColorSelect == 0u && spriteColorSelect != 0u) {
                    finalColorSelect = spriteColorSelect
                    finalPaletteSelect = spritePaletteSelect
                }

                if (backgroundColorSelect != 0u && spriteColorSelect == 0u) {
                    finalColorSelect = backgroundColorSelect
                    finalPaletteSelect = backgroundPaletteSelect
                }

                if (backgroundColorSelect != 0u && spriteColorSelect != 0u && !spritePriority) {
                    finalColorSelect = spriteColorSelect
                    finalPaletteSelect = spritePaletteSelect

                    if (objectAttributeMemory.isSpriteZeroPossible && objectAttributeMemory.isSpriteZeroBeingRendered) {
                        if (maskRegister.isShowingBackground  || maskRegister.isShowingSprites) {
                            if (cycles != 255) {
                                if (!(maskRegister.isShowingBackgroundInLeftMost8Pixels || maskRegister.isShowingSpritesInLeftMost8Pixels)) {
                                    if (cycles in 8..256) {
                                        statusRegister.hasSpriteHit = true
                                    }
                                } else {
                                    statusRegister.hasSpriteHit = true
                                }
                            }
                        }
                    }
                }

                if (backgroundColorSelect != 0u && spriteColorSelect != 0u && spritePriority) {
                    finalColorSelect = backgroundColorSelect
                    finalPaletteSelect = backgroundPaletteSelect

                    if (objectAttributeMemory.isSpriteZeroPossible && objectAttributeMemory.isSpriteZeroBeingRendered) {
                        if (maskRegister.isShowingBackground  || maskRegister.isShowingSprites) {
                            if (cycles != 255) {
                                if (!(maskRegister.isShowingBackgroundInLeftMost8Pixels || maskRegister.isShowingSpritesInLeftMost8Pixels)) {
                                    if (cycles in 8..256) {
                                        statusRegister.hasSpriteHit = true
                                    }
                                } else {
                                    statusRegister.hasSpriteHit = true
                                }
                            }
                        }
                    }
                }

                drawPixel(
                    scanline,
                    cycles,
                    finalColorSelect,
                    finalPaletteSelect
                )
            }
        }

        if ((scanline in 0..239 || scanline == 261) && (cycles in 1..256 || cycles in 321..336)) {
            if (maskRegister.isShowingBackground) {
                lowBackgroundShiftRegister = lowBackgroundShiftRegister shl 1
                highBackgroundShiftRegister = highBackgroundShiftRegister shl 1
                lowPaletteShiftRegister = lowPaletteShiftRegister shl 1
                highPaletteShiftRegister = highPaletteShiftRegister shl 1
            }
        }

        if ((scanline in 0..239) && cycles in 1..256) {
            if (maskRegister.isShowingSprites) {
                objectAttributeMemory.decrementAllX()
            }
        }

        // Increment X And Y Over Entire Area.
        if (cycles < 340) {
            cycles++
        } else {
            cycles  = 0

            if (scanline < 261) {
                scanline++
            } else {
                scanline = 0
            }
        }
    }

    private fun drawPixel(scanline: Int, cycle: Int, colorSelect: UInt, paletteSelect: UInt) {
        frameBuffer[scanline][cycle - 1] = readPaletteTableMemoryWhileRendering(
            ((overridingPalette?.shl(2)) ?: (paletteSelect shl 2)) + colorSelect
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
        //tRegister.value = 0u

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
        oamAddressRegister = data
    }

    /**
     * Read And Writes To OAM Data Register
     */
    fun readOamDataRegister(): UInt {
        return objectAttributeMemory.primaryMemory[oamAddressRegister.toInt()].toUInt()
    }

    fun writeToOamDataRegister(data: UInt) {
        staleBus = data

        oamDataRegister = data

        // In case programmer does not initialize oam address to 0.
        if (oamAddressRegister <= 0xFFu) {
            objectAttributeMemory.primaryMemory[oamAddressRegister.toInt()] = data.toUByte()
        }

        oamAddressRegister++
    }

    private var testOamCounter = 0

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

        dmaRegister = data

        notify(Sender.PPU, Event.DMA)
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

        if (nameTableMirroring == MirroringMode.VERTICAL) {
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


        if (nameTableMirroring == MirroringMode.HORIZONTAL) {
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

        if (nameTableMirroring == MirroringMode.VERTICAL) {
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

        if (nameTableMirroring == MirroringMode.HORIZONTAL) {
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

        when (paletteAddress) {
            0x10u -> paletteAddress = 0x00u
            0x14u -> paletteAddress = 0x04u
            0x18u -> paletteAddress = 0x08u
            0x1Cu -> paletteAddress = 0x0Cu
        }

        return paletteTable[paletteAddress.toInt()].toUInt()
    }

    private fun writeToPaletteTableMemory(address: UInt, data: UInt) {
        var paletteAddress = address.mod(0x20u)

        when (paletteAddress) {
            0x10u -> paletteAddress = 0x00u
            0x14u -> paletteAddress = 0x04u
            0x18u -> paletteAddress = 0x08u
            0x1Cu -> paletteAddress = 0x0Cu
        }

        paletteTable[paletteAddress.toInt()] = data.toUByte()
    }

    private fun readPaletteTableMemoryWhileRendering(address: UInt): UInt {
        var paletteAddress = address.mod(0x20u)

        when (paletteAddress) {
            0x04u -> paletteAddress = 0x00u
            0x08u -> paletteAddress = 0x00u
            0x0Cu -> paletteAddress = 0x00u
            0x10u -> paletteAddress = 0x00u
            0x14u -> paletteAddress = 0x00u
            0x18u -> paletteAddress = 0x00u
            0x1Cu -> paletteAddress = 0x00u
        }

        return paletteTable[paletteAddress.toInt()].toUInt()
    }

    companion object {
        const val NAMETABLE_MEMORY_SIZE = 0x800
        const val OAM_MEMORY_SIZE = 0x100
        const val PALETTE_TABLE_MEMORY_SIZE = 0x20
    }
}
