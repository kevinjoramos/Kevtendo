package kevinjoramos.kevtendo

import ktx.app.KtxScreen
import ktx.log.logger

class GameScreen : KtxScreen {

    override fun show() {
        super.show()
        log.debug { "GameScreen got shown!" }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
