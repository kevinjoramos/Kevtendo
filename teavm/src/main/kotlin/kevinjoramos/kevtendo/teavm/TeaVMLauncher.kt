@file:JvmName("TeaVMLauncher")

package kevinjoramos.kevtendo.teavm

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration
import com.github.xpenatan.gdx.backends.teavm.TeaApplication
import kevinjoramos.kevtendo.Kevtendo

/** Launches the TeaVM/HTML application. */
fun main() {
    val config = TeaApplicationConfiguration("canvas").apply {
        width = 640
        height = 480
    }
    TeaApplication(Kevtendo(), config)
}
