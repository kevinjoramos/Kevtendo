package actors

import bus.Bus
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.scenes.scene2d.Actor
import controller.GameController
import kevinjoramos.kevtendo.Kevtendo
import ui.NesColors

@OptIn(ExperimentalUnsignedTypes::class)
class NesScreen : Actor() {
    private val sprite = Sprite(Texture(Pixmap(256, 240, Pixmap.Format.RGBA8888)))

    private var systemClock = 0
    private var bus: Bus
    private var controller1: GameController
    private var controller2: GameController

    private var isDMAReady = false
    private var highByteDMAPointer = 0u
    private var lowByteDMAPointer = 0u
    private var currentDMAData = 0u
    lateinit var spritesSnapshot: List<ppu.Sprite>

    init {
        setBounds(sprite.x, sprite.y, sprite.width, sprite.height)
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/Donkey Kong.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/Super Mario Bros.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/smb.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/nestest.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/PacMan.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/Ice Climber.nes"
        val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/palette_ram.nes"
        bus = Bus(pathToGame)
        bus.also {
            this.controller1 = it.controller1
            this.controller2 = it.controller2
        }
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        val pixels = Pixmap(256, 240, Pixmap.Format.RGBA8888)

        for ((i, row) in bus.ppu.frameBuffer.withIndex()) {
            for ((j, pixel) in row.withIndex()) {
                pixels.drawPixel(
                    j,
                    i,
                    NesColors.decodeNesColor(pixel.toInt())
                )
            }
        }

        sprite.texture = Texture(pixels)
        sprite.draw(batch)
    }

    override fun act(delta: Float) {
        pollControllers()

        systemClock = 0
        while (systemClock < Kevtendo.TOTAL_SYSTEM_CYCLES_PER_FRAME) {
            // execute one cycle of ppu (outputs one pixel)
            bus.ppu.run()

            // execute one cycle of cpu (1 cpu cycle for every 3 ppu cycles).
            // we also need to check for DMA suspends.
            if (systemClock % 3 == 0) {

                if (bus.cpu.isSuspendedForDMA) {

                    // When a dma suspend is pending we need to wait for a read cycle (for my emulation this is every even cycle).
                    if (systemClock.mod(2) == 0 && !isDMAReady) {

                        // Here we set up our values to intercept the cpu clock cycles.
                        isDMAReady = true
                        highByteDMAPointer = bus.ppu.dmaRegister
                        lowByteDMAPointer = 0u
                    }

                    // When we get the right cycle, we can start writing (odd) / reading (even).
                    if (isDMAReady) {

                        // Read
                        if (systemClock.mod(2) == 0) {
                            currentDMAData = bus.readAddressAsCpu(((highByteDMAPointer shl 8) or lowByteDMAPointer).toUShort()).toUInt()
                        }

                        // Write
                        else {
                            bus.ppu.writeToOamDataRegister(currentDMAData)

                            // When we are done, go back to normal cpu activities.
                            if (lowByteDMAPointer == 0xFFu) {
                                bus.cpu.isSuspendedForDMA = false
                                isDMAReady = false
                                highByteDMAPointer = 0u
                                lowByteDMAPointer = 0u
                                currentDMAData = 0u
                            } else {
                                lowByteDMAPointer++
                            }
                        }
                    }
                }

                // Run normally
                else {
                    bus.cpu.run()

                    // update debugger.
                    if (bus.cpu.cycleCount == 0) {

                    }
                }
            }

            /*
            spritesSnapshot = bus.ppu.objectAttributeMemory.primaryMemory
                .chunked(4)
                .map {
                    ppu.Sprite().apply {
                        yPosition = it[0].toUInt()
                        tileIndex = it[1].toUInt()
                        attributes = it[2].toUInt()
                        xPosition = it[3].toUInt()
                    }
                }

             */


            systemClock++
        }
    }

    private fun pollControllers() {
        controller1.buttonUp = Gdx.input.isKeyPressed(Input.Keys.W)
        controller1.buttonRight = Gdx.input.isKeyPressed(Input.Keys.D)
        controller1.buttonDown = Gdx.input.isKeyPressed(Input.Keys.S)
        controller1.buttonLeft = Gdx.input.isKeyPressed(Input.Keys.A)
        controller1.buttonA = Gdx.input.isKeyPressed(Input.Keys.J)
        controller1.buttonB = Gdx.input.isKeyPressed(Input.Keys.K)
        controller1.buttonStart = Gdx.input.isKeyPressed(Input.Keys.NUMPAD_2)
        controller1.buttonSelect = Gdx.input.isKeyPressed(Input.Keys.NUMPAD_1)
    }
}


