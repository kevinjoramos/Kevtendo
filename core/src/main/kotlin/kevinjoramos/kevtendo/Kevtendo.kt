package kevinjoramos.kevtendo

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxGame
import ktx.app.KtxScreen

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Kevtendo : KtxGame<KtxScreen>() {

    private lateinit var spriteBatch: SpriteBatch

    override fun create() {
        super.create()

        Gdx.app.logLevel = Application.LOG_DEBUG

        //addScreen(GameScreen())
        //setScreen<GameScreen>()

        spriteBatch = SpriteBatch()


    }

    override fun render() {
        super.render()

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val noise = Array(240) { IntArray(256) { if ((0..1).random() == 0) 0 else 0xFFFFFFF } }

        val pixels = Pixmap(256, 240, Pixmap.Format.RGBA8888)

        for ((i, row) in noise.withIndex()) {
            for ((j, pixel) in row.withIndex()) {
                pixels.drawPixel(j, i, pixel)
            }
        }


        val frameBuffer = Texture(pixels)

        spriteBatch.begin()

        spriteBatch.draw(frameBuffer, 0f, 0f, 256f, 240f)

        spriteBatch.end()

        frameBuffer.dispose()
    }
}
