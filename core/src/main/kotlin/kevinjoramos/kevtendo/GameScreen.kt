package kevinjoramos.kevtendo

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.log.logger

class GameScreen : KtxScreen {

    companion object {
        private val log = logger<GameScreen>()
    }

    private val stage: Stage = Stage(FitViewport(256f, 240f))


    override fun render(delta: Float) {
        with (stage) {
            act(delta)
            draw()
        }
    }

    override fun show() {
        log.debug { "GameScreen got shown!" }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.disposeSafely()
    }
}
