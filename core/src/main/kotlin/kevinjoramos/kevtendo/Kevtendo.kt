package kevinjoramos.kevtendo

import bus.Bus
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import controller.GameController
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ui.NesColors

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
@OptIn(ExperimentalUnsignedTypes::class)
class Kevtendo : KtxGame<KtxScreen>() {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var viewportGame: Viewport
    private lateinit var viewportDebug: Viewport
    private lateinit var cameraGame: Camera
    private lateinit var cameraDebug: Camera

    private var systemClock = 0
    private lateinit var bus: Bus
    private lateinit var controller1: GameController
    private lateinit var controller2: GameController

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
        cameraGame = OrthographicCamera()
        viewportGame = FitViewport(256f, 240f, cameraGame)
        viewportGame.apply()
        cameraGame.position.set(cameraGame.viewportWidth/2, cameraGame.viewportHeight/2, 0f)

        spriteBatch = SpriteBatch()

        // Debugging
        Gdx.app.logLevel = Application.LOG_DEBUG

        //addScreen(GameScreen())
        //setScreen<GameScreen>()

        // NES setup.
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/Donkey Kong.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/Super Mario Bros.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/smb.nes"
        //val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/PacMan.nes"
        val pathToGame = "/home/kevin/Documents/IntellijProjects/Kevtendo/core/src/main/kotlin/NesEmulator/games/Ice Climber.nes"


        bus = Bus(pathToGame)
        controller1 = bus.controller1
        controller2 = bus.controller2
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        viewportGame.update(width, height)
        cameraGame.position.set(cameraGame.viewportWidth/2, cameraGame.viewportHeight/2, 0f)
    }

    override fun render() {
        super.render()

        // Snapshot controller input.
        pollControllers()

        // Run emulation for one frame.
        executeFrameCycle()
        systemClock = 0

        cameraGame.update()
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        spriteBatch.projectionMatrix = cameraGame.combined
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
