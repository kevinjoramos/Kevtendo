package kevinjoramos.kevtendo

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

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Kevtendo : KtxGame<KtxScreen>() {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var viewport: Viewport
    private lateinit var camera: Camera

    override fun create() {
        super.create()
        camera = OrthographicCamera()
        viewport = FitViewport(256f, 240f, camera)
        viewport.apply()
        camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0f)

        Gdx.app.logLevel = Application.LOG_DEBUG

        //addScreen(GameScreen())
        //setScreen<GameScreen>()

        spriteBatch = SpriteBatch()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        viewport.update(width, height)
        camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0f)
    }

    override fun render() {
        super.render()

        camera.update()

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        spriteBatch.projectionMatrix = camera.combined

        val noise = Array(240) { UIntArray(256) { if ((0..1).random() == 0) 0u else 0xFFFFFFFFu} }

        val pixels = Pixmap(256, 240, Pixmap.Format.RGBA8888)

        for ((i, row) in noise.withIndex()) {
            for ((j, pixel) in row.withIndex()) {
                pixels.drawPixel(j, i, pixel.toInt())
            }
        }


        val frameBuffer = Texture(pixels)

        spriteBatch.begin()

        spriteBatch.draw(frameBuffer, 0f, 0f, 256f, 240f)

        spriteBatch.end()

        frameBuffer.dispose()
    }


}
