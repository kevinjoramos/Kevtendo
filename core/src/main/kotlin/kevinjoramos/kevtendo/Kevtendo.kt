package kevinjoramos.kevtendo

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import ktx.app.KtxGame
import ktx.app.KtxScreen

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Kevtendo : KtxGame<KtxScreen>() {

    override fun create() {
        super.create()

        Gdx.app.logLevel = Application.LOG_DEBUG

        addScreen(GameScreen())
        setScreen<GameScreen>()
    }
}
