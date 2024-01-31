package kevinjoramos.kevtendo

import actors.NesScreen
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
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import controller.GameController
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ui.NesColors

class Kevtendo : KtxGame<KtxScreen>() {
    private lateinit var stage: Stage

    companion object {
        const val TOTAL_SYSTEM_CYCLES_PER_FRAME = 89_342
        const val FIRST_CYCLE_AFTER_RENDER = 81_841
        //const val FIRST_CYCLE_AFTER_RENDER = 82_080
    }

    override fun create() {
        stage = Stage(FitViewport(245f, 240f))
        stage.apply {
            Gdx.input.inputProcessor = this

            val actor = NesScreen()
            addActor(actor)
            keyboardFocus = actor
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height)
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.run {
            act(Gdx.graphics.deltaTime)
            draw()
        }
    }

    override fun dispose() {
        super.dispose()
        stage.dispose()
    }
}
