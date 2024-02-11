package ppu

import bus.Bus
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
    override val bus: Bus
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

    private val nameTableMirroring = MirroringMode.HORIZONTAL

    /**
     * Memories
     */
    private val nameTable: UByteArray = UByteArray(1024)
    private val objectAttributeMemory = ObjectAttributeMemory()
    private val paletteTable = UByteArray(32)

    /**
     * Scanline Rendering
     */
    val frameBuffer = Array(240) { UByteArray(256) { (0x0Fu).toUByte() } }
    private var scanline = 0
    private var cycles = 0
    private var patternTileId = 0x0000u
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

    fun run() {

        // Clear VBlank, Sprite 0, and Sprite Overflow.
        // @ 261:1
        if (scanline == 261 && cycles == 1) {
            statusRegister.apply {
                this.isInVBlank = false
                this.hasSpriteOverflow = false
                this.hasSpriteHit = false
            }
        }

        // Fetch Nametable bytes from current address.
        // @ 0-239 : 1 - 249 once every 8 cycles i.e 1, 9, 17... 249
        // @ 0-239 : 321 and 329
        // @ 261 : 1 - 249 once every 8 cycles i.e 1, 9, 17... 249 <- TODO These might not be necessary.
        // @ 261 : 321 and 329
        if (scanline in 0..239 || scanline == 261) {
            if ((cycles in 1..249 && cycles.mod(8) == 1)
                || cycles == 321
                || cycles == 329
            ) {
                patternTileId = readNameTableMemory(vRegister.patternTileAddress)
            }
        }

        // Fetch Attribute table byte from current address.
        // @ 0-239 : 3 - 251 once every 8 cycles i.e 3, 11, 19... 251
        // @ 0-239 : 323 and 331
        // @ 261 : 3 - 251 once every 8 cycles i.e 3, 11, 19... 251 <- TODO These might not be necessary.
        // @ 261 : 323 and 331
        if (scanline in 0..239 || scanline == 261) {
            if ((cycles in 3..251 && cycles.mod(8) == 3)
                || cycles == 323
                || cycles == 331
            ) {
                val attributeTileId = readNameTableMemory(vRegister.attributeTileAddress)

                val quadrantAddress = ((vRegister.coarseY and 0x02u) or ((vRegister.coarseX and 0x02u) shr 1))

                attributeBits = when (quadrantAddress) {
                    0u -> attributeTileId and 0x03u
                    1u -> (attributeTileId shr 2) and 0x03u
                    2u -> (attributeTileId shr 4) and 0x03u
                    else -> (attributeTileId shr 6) and 0x03u
                }
            }
        }

        // Fetch table tile low plane byte.
        // @ 0-239 : 5 - 253 once every 8 cycles i.e 5, 13, 21... 253
        // @ 0-239 : 325 and 333
        // @ 261 : 5 - 253 once every 8 cycles i.e 5, 13, 21... 253 <- TODO These might not be necessary.
        // @ 261 : 325 and 333
        if (scanline in 0..239 || scanline == 261) {
            if ((cycles in 5..253 && cycles.mod(8) == 5)
                || cycles == 325
                || cycles == 333
            ) {
                tileLowBitPlane = readPatternTableMemory(
                    controllerRegister.backgroundPatternTableAddress +
                        (patternTileId shl 4) +
                        vRegister.fineY
                )
            }
        }

        // Fetch table tile high plane byte.
        // @ 0-239 : 7 - 255 once every 8 cycles i.e 7, 15, 23... 255
        // @ 0-239 : 327 and 335
        // @ 261 :  7 - 255 once every 8 cycles i.e 7, 15, 23... 255 <- TODO These might not be necessary.
        // @ 261 : 327 and 335
        if (scanline in 0..239 || scanline == 261) {
            if ((cycles in 7..255 && cycles.mod(8) == 7)
                || cycles == 327
                || cycles == 335
            ) {
                tileHighBitPlane = readPatternTableMemory(
                    controllerRegister.backgroundPatternTableAddress +
                        (patternTileId shl 4) +
                        (vRegister.fineY) + 8u
                )
            }
        }

        // Emit NMI on second tick of vertical blank.
        // @ 241:1
        if (scanline == 241 && cycles == 1) {
            statusRegister.isInVBlank = true
            if (controllerRegister.generateNMIAtStartVBlank) {
                emitNMISignal()
            }
        }

        // Increment vertical position of VRegister.
        // @ 0 - 239, 261:256 (if rendering is enabled.)
        if ((scanline in 0..239 || scanline == 261) && cycles == 256) {
            if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                vRegister.incrementY()
            }
        }

        // Increment horizontal position of VRegister.
        // @ 0 - 239, 261:8 - 256 every 8 bytes, and 328 and 336 (if rendering is enabled.)
        if (scanline in 0..239 || scanline == 261) {
            if ((cycles in 8..256 && cycles.mod(8) == 0)
                || cycles == 328
                || cycles == 336
            ) {
                if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                    vRegister.incrementCoarseX()
                }
            }
        }

        // Copy horizontal position of TRegister to VRegister.
        // v: ... .A.. ...B CDEF <- t: ... .A.. ...B CDEF
        // @ 0 - 239, 261:257 (if rendering is enabled.)
        if ((scanline in 0..239 || scanline == 261) && cycles == 257) {
            if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                vRegister.value = (vRegister.value and (0x041Fu).inv()) or (tRegister.value and 0x041Fu)
            }
        }

        // Copy vertical position of TRegister to VRegister.
        // v: GHI A.BC DEF. .... <- t: GHI A.BC DEF. ....
        // @ 261 : 280 - 304 (if rendering is enabled.)
        if (scanline == 261 && cycles in 280..304) {
            if (maskRegister.isShowingBackground || maskRegister.isShowingSprites) {
                vRegister.value = (vRegister.value and (0x7BE0u).inv()) or (tRegister.value and 0x7BE0u)
            }
        }

        // Reload Background Shift Registers
        // @ 0 - 239, 261: 9 - 257, 329, 337 every 8 bytes. < - TODO I'm not sure if these are all necessary for the prerender scanline.
        if (scanline in 0..239 || scanline == 261) {
            if ((cycles in 9..257 && cycles.mod(8) == 1)
                || cycles == 329
                || cycles == 337
            ) {
                // new tile data is loaded into the end of the shift registers.
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
        }

        // Shift the shift registers.
        // @ 0 - 239, 261: 2 - 257, 322-337 < - TODO I'm not sure if these are all necessary for the prerender scanline.
        if (scanline in 0..239 || scanline == 261) {
            if (cycles in 2..257 || cycles in 322..337) {
                lowBackgroundShiftRegister = lowBackgroundShiftRegister shl 1
                highBackgroundShiftRegister = highBackgroundShiftRegister shl 1
                lowPaletteShiftRegister = lowPaletteShiftRegister shl 1
                highPaletteShiftRegister = highPaletteShiftRegister shl 1
            }
        }

        // Sprite Evaluation for next scanline.
        // @ 0 - 239 : 257
        if ((scanline in 0..239) && cycles == 257) {
            objectAttributeMemory.evaluateNextEightSprites(
                scanline.toUInt(),
                controllerRegister.isSpriteSize8x16
            )

            if (objectAttributeMemory.hasSpriteOverflow) {
                statusRegister.hasSpriteOverflow = true
            }
        }

        // Load sprite registers.
        // @ 0 - 239 : 340
        if ((scanline in 0..239 && cycles == 340)) {
            objectAttributeMemory.secondaryMemory.forEach { sprite ->

                // when the sprite is 8x8 and not flipped vertically
                if (!controllerRegister.isSpriteSize8x16 && !sprite.isFlippedVertically) {
                    sprite.lowSpriteShiftRegister = readPatternTableMemory(
                        controllerRegister.squareSpritePatternTableAddress +
                            (sprite.tileIndex shl 4) + (scanline.toUInt() - sprite.yPosition)
                    )

                    sprite.highSpriteShiftRegister = readPatternTableMemory(
                        controllerRegister.squareSpritePatternTableAddress +
                            (sprite.tileIndex shl 4) + (scanline.toUInt() - sprite.yPosition) + 8u
                    )
                }

                // when the sprite is 8x8 and flipped vertically
                if (!controllerRegister.isSpriteSize8x16 && sprite.isFlippedVertically) {
                    sprite.lowSpriteShiftRegister = readPatternTableMemory(
                        controllerRegister.squareSpritePatternTableAddress +
                            (sprite.tileIndex shl 4) + (7u - (scanline.toUInt() - sprite.yPosition))
                    )

                    sprite.highSpriteShiftRegister = readPatternTableMemory(
                        controllerRegister.squareSpritePatternTableAddress +
                            (sprite.tileIndex shl 4) + (7u - (scanline.toUInt() - sprite.yPosition)) + 8u
                    )
                }

                // when the sprite is 8x16 and not flipped vertically
                if (controllerRegister.isSpriteSize8x16 && !sprite.isFlippedVertically) {
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

                // when the sprite is 8x16 and flipped vertically
                if (controllerRegister.isSpriteSize8x16 && sprite.isFlippedVertically) {
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
                }

                // flip bits horizontally if needed.
                if (sprite.isFlippedHorizontally) {
                    sprite.lowSpriteShiftRegister = sprite.lowSpriteShiftRegister.toUByte().reverse().toUInt()
                    sprite.highSpriteShiftRegister = sprite.highSpriteShiftRegister.toUByte().reverse().toUInt()
                }
            }
        }

        // Pixel Multiplexer and render.
        if (scanline in 0..239 && cycles in 1..256) {

            val activeSprite = if (maskRegister.isShowingSprites) {
                objectAttributeMemory.getPrioritizedActiveSprite()
            } else {
                Sprite()
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

                // Background pixel 0 vs Sprite pixel 0 = BG
                if (backgroundColorSelect == 0u && spriteColorSelect == 0u) {
                    finalColorSelect = backgroundColorSelect
                    finalPaletteSelect = backgroundPaletteSelect
                }

                //  background pixel 0 vs sprite pixel 1-3 = SP
                if (backgroundColorSelect == 0u && spriteColorSelect != 0u) {
                    finalColorSelect = spriteColorSelect
                    finalPaletteSelect = spritePaletteSelect
                }

                //  background pixel 1-3 vs sprite pixel 0 = BG
                if (backgroundColorSelect != 0u && spriteColorSelect == 0u) {
                    finalColorSelect = backgroundColorSelect
                    finalPaletteSelect = backgroundPaletteSelect
                }

                // tie breaker, priority false = SP
                if (backgroundColorSelect != 0u && spriteColorSelect != 0u && !spritePriority) {
                    finalColorSelect = spriteColorSelect
                    finalPaletteSelect = spritePaletteSelect

                    if (objectAttributeMemory.isSpriteZeroPossible && objectAttributeMemory.isSpriteZeroBeingRendered) {
                        if (maskRegister.isShowingBackground && maskRegister.isShowingSprites) {
                            if (cycles != 255) {
                                if (maskRegister.isShowingBackgroundInLeftMost8Pixels && maskRegister.isShowingSpritesInLeftMost8Pixels) {
                                    statusRegister.hasSpriteHit = true
                                } else if (cycles !in 0..7) {
                                    statusRegister.hasSpriteHit = true
                                }
                            }
                        }
                    }
                }

                // tiebreaker, priority true = BG
                if (backgroundColorSelect != 0u && spriteColorSelect != 0u && spritePriority) {
                    finalColorSelect = backgroundColorSelect
                    finalPaletteSelect = backgroundPaletteSelect

                    if (objectAttributeMemory.isSpriteZeroPossible && objectAttributeMemory.isSpriteZeroBeingRendered) {
                        if (maskRegister.isShowingBackground && maskRegister.isShowingSprites) {
                            if (cycles != 255) {
                                if (maskRegister.isShowingBackgroundInLeftMost8Pixels && maskRegister.isShowingSpritesInLeftMost8Pixels) {
                                    statusRegister.hasSpriteHit = true
                                } else if (cycles !in 0..7) {
                                    statusRegister.hasSpriteHit = true
                                }
                            }
                        }
                    }
                }

                // Decrement X on all sprites
                if ((scanline in 0..239) && cycles in 1..256) {
                    if (maskRegister.isShowingSprites) {
                        objectAttributeMemory.decrementAllX()
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
            vRamHasFirstWrite = true
        } else {
            tRegister.parseLowerLatch(data)
            vRegister.value = tRegister.value
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
        // Left and right sides of pattern table
        if (address in 0x0000u..0x1FFFu) {
            readAddressFromBus(address.toUShort())
        }
        // nametables 0-3 (also the mirrors)
        if (address in 0x2000u..0x3EFFu) {

        }
        // palette ram and mirrors.
        if (address in 0x3F00u..0x3FFFu)





        return when (address) {
            // Left and right sides of pattern table
            in  -> readPatternTableMemory(address)
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

        // Vertical mirroring: $2000 equals $2800 and $2400 equals $2C00 (e.g. Super Mario Bros.)
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

        // Horizontal mirroring: $2000 equals $2400 and $2800 equals $2C00 (e.g. Kid Icarus)
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

        // Vertical mirroring: $2000 equals $2800 and $2400 equals $2C00 (e.g. Super Mario Bros.)
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

        // Horizontal mirroring: $2000 equals $2400 and $2800 equals $2C00 (e.g. Kid Icarus)
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

    override fun readAddressFromBus(address: UShort): UByte = bus.readAddressAsPpu(address)


    companion object {
        const val NAMETABLE_MEMORY_SIZE = 0x800
        const val OAM_MEMORY_SIZE = 0x100
        const val PALETTE_TABLE_MEMORY_SIZE = 0x20
    }
}
