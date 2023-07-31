package kevinjoramos.kevtendo

import bus.Bus
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ui.NesColors

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
@OptIn(ExperimentalUnsignedTypes::class)
class Kevtendo : KtxGame<KtxScreen>() {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var viewport: Viewport
    private lateinit var camera: Camera

    private var systemClock = 0
    private lateinit var bus: Bus

    private var isDMAReady = false
    private var highByteDMAPointer = 0u
    private var lowByteDMAPointer = 0u
    private var currentDMAData = 0u

    companion object {
        const val TOTAL_SYSTEM_CYCLES_PER_FRAME = 89_342
        const val FIRST_CYCLE_AFTER_RENDER = 81_841
        //const val FIRST_CYCLE_AFTER_RENDER = 82_080

        const val MILLISECONDS_PER_FRAME = 16.5
    }

    override fun create() {
        super.create()

        // LibGDX setup
        camera = OrthographicCamera()
        viewport = FitViewport(256f, 240f, camera)
        viewport.apply()
        camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0f)
        spriteBatch = SpriteBatch()

        // Debugging
        Gdx.app.logLevel = Application.LOG_DEBUG

        //addScreen(GameScreen())
        //setScreen<GameScreen>()

        // NES setup.
        val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/Donkey Kong.nes"

        bus = Bus(pathToGame)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        viewport.update(width, height)
        camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0f)
    }

    override fun render() {
        super.render()

        executeFrameCycle()

        systemClock = 0

        camera.update()

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        spriteBatch.projectionMatrix = camera.combined

        val pixels = Pixmap(256, 240, Pixmap.Format.RGBA8888)

        for ((i, row) in bus.ppu.frameBuffer.withIndex()) {
            for ((j, pixel) in row.withIndex()) {
                pixels.drawPixel(
                    j,
                    i,
                    when (pixel.toUInt()) {
                        0x00u -> NesColors.COLOR_00
                        0x01u -> NesColors.COLOR_01
                        0x02u -> NesColors.COLOR_02
                        0x03u -> NesColors.COLOR_03
                        0x04u -> NesColors.COLOR_04
                        0x05u -> NesColors.COLOR_05
                        0x06u -> NesColors.COLOR_06
                        0x07u -> NesColors.COLOR_07
                        0x08u -> NesColors.COLOR_08
                        0x09u -> NesColors.COLOR_09
                        0x0Au -> NesColors.COLOR_0A
                        0x0Bu -> NesColors.COLOR_0B
                        0x0Cu -> NesColors.COLOR_0C
                        0x0Du -> NesColors.COLOR_0D
                        0x0Eu -> NesColors.COLOR_0E
                        0x0Fu -> NesColors.COLOR_0F
                        0x10u -> NesColors.COLOR_10
                        0x11u -> NesColors.COLOR_11
                        0x12u -> NesColors.COLOR_12
                        0x13u -> NesColors.COLOR_13
                        0x14u -> NesColors.COLOR_14
                        0x15u -> NesColors.COLOR_15
                        0x16u -> NesColors.COLOR_16
                        0x17u -> NesColors.COLOR_17
                        0x18u -> NesColors.COLOR_18
                        0x19u -> NesColors.COLOR_19
                        0x1Au -> NesColors.COLOR_1A
                        0x1Bu -> NesColors.COLOR_1B
                        0x1Cu -> NesColors.COLOR_1C
                        0x1Du -> NesColors.COLOR_1D
                        0x1Eu -> NesColors.COLOR_1E
                        0x1Fu -> NesColors.COLOR_1F
                        0x20u -> NesColors.COLOR_20
                        0x21u -> NesColors.COLOR_21
                        0x22u -> NesColors.COLOR_22
                        0x23u -> NesColors.COLOR_23
                        0x24u -> NesColors.COLOR_24
                        0x25u -> NesColors.COLOR_25
                        0x26u -> NesColors.COLOR_26
                        0x27u -> NesColors.COLOR_27
                        0x28u -> NesColors.COLOR_28
                        0x29u -> NesColors.COLOR_29
                        0x2Au -> NesColors.COLOR_2A
                        0x2Bu -> NesColors.COLOR_2B
                        0x2Cu -> NesColors.COLOR_2C
                        0x2Du -> NesColors.COLOR_2D
                        0x2Eu -> NesColors.COLOR_2E
                        0x2Fu -> NesColors.COLOR_1F
                        0x30u -> NesColors.COLOR_30
                        0x31u -> NesColors.COLOR_31
                        0x32u -> NesColors.COLOR_32
                        0x33u -> NesColors.COLOR_33
                        0x34u -> NesColors.COLOR_34
                        0x35u -> NesColors.COLOR_35
                        0x36u -> NesColors.COLOR_36
                        0x37u -> NesColors.COLOR_37
                        0x38u -> NesColors.COLOR_38
                        0x39u -> NesColors.COLOR_39
                        0x3Au -> NesColors.COLOR_3A
                        0x3Bu -> NesColors.COLOR_3B
                        0x3Cu -> NesColors.COLOR_3C
                        0x3Du -> NesColors.COLOR_3D
                        0x3Eu -> NesColors.COLOR_3E
                        else -> NesColors.COLOR_3F
                    }.toInt()
                )
            }
        }

        val frameBuffer = Texture(pixels)

        spriteBatch.begin()

        spriteBatch.draw(frameBuffer, 0f, 0f, 256f, 240f)

        spriteBatch.end()

        frameBuffer.dispose()
    }

    private fun executeFrameCycle() {

        while (systemClock < TOTAL_SYSTEM_CYCLES_PER_FRAME) {
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
                            currentDMAData = bus.readAddress(((highByteDMAPointer shl 8) or lowByteDMAPointer).toUShort()).toUInt()
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

            systemClock++
        }
    }


}
