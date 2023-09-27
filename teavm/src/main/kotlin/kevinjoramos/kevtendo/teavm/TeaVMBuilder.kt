package kevinjoramos.kevtendo.teavm

import java.io.File
import com.github.xpenatan.gdx.backends.teavm.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.plugins.TeaReflectionSupplier
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass

/** Builds the TeaVM/HTML application. */
@SkipClass
object TeaVMBuilder {
    @JvmStatic fun main(arguments: Array<String>) {
        val teaBuildConfiguration = TeaBuildConfiguration().apply {
            assetsPath.add(File("../assets"))
            webappPath = File("build/dist").canonicalPath
            // Register any extra classpath assets here:
            // additionalAssetsClasspathFiles += "kevinjoramos/kevtendo/asset.extension"
        }

        // Register any classes or packages that require reflection here:
        // TeaReflectionSupplier.addReflectionClass("kevinjoramos.kevtendo.reflect")

        val tool = TeaBuilder.config(teaBuildConfiguration)
        tool.mainClass = "kevinjoramos.kevtendo.teavm.TeaVMLauncher"
        TeaBuilder.build(tool)
    }
}
