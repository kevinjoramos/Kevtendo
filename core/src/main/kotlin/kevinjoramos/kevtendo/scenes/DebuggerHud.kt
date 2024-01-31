package kevinjoramos.kevtendo.scenes

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport

class DebuggerHud(val spriteBatch: SpriteBatch) {
    val viewport: Viewport = FitViewport(256f, 240f, OrthographicCamera())
    val stage: Stage = Stage(viewport, spriteBatch)

    val sprite0Label: Label = Label("Test Sprite String", Label.LabelStyle(BitmapFont(), Color.WHITE))

    init {
        val table = Table()
        table.top()
        table.setFillParent(true)

        table.add(sprite0Label).expandX()

        stage.addActor(table)
    }
}
