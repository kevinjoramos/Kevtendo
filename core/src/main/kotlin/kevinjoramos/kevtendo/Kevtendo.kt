package kevinjoramos.kevtendo

import actors.NesScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxGame
import ktx.app.KtxScreen

class Kevtendo : KtxGame<KtxScreen>() {
    private lateinit var stage: Stage
    private lateinit var skin: Skin

    companion object {
        const val TOTAL_SYSTEM_CYCLES_PER_FRAME = 89_342
        const val FIRST_CYCLE_AFTER_RENDER = 81_841
        //const val FIRST_CYCLE_AFTER_RENDER = 82_080
    }

    override fun create() {
        skin = Skin(Gdx.files.internal("uiskin.json"))
        stage = Stage(FitViewport(256f, 240f))
        stage.apply {
            Gdx.input.inputProcessor = this
            val screen = NesScreen()
            addActor(screen)
            /*
            val container = Table()
            addActor(container)
            container.setFillParent(true)

            val Screen = NesScreen()
            val scrollPaneTable = Table()
            val scrollPane = ScrollPane(scrollPaneTable, skin)
            //addActor(actor)
            container.add(Screen).grow()
            container.add(scrollPane)

            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))
            scrollPaneTable.row()
            scrollPaneTable.add(Label("Hello World!", skin))

             */

            keyboardFocus = screen
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
