package info.spiralframework.base.binding

import dev.brella.kornea.io.jvm.files.ensureDirectoryExists
import dev.brella.kornea.io.jvm.files.ensureFileExists
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import io.github.soc.directories.ProjectDirectories
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import info.spiralframework.base.common.SpiralCatalyst
import java.io.File
import kotlin.reflect.KClass

actual class DefaultSpiralConfig : SpiralConfig, SpiralCatalyst<SpiralContext> {
    private var primed: Boolean = false
    var projectDirectories: ProjectDirectories by oneTimeMutableInline()
    override val klass: KClass<SpiralContext> = SpiralContext::class

    actual override fun SpiralContext.getConfigFile(module: String): String {
        val configDir = File(projectDirectories.configDir)
            .ensureDirectoryExists()

        return File(configDir, "$module.yaml")
            .ensureFileExists()
            .absolutePath
    }

    actual override fun SpiralContext.getLocalDataDir(group: String): String {
        val localDataDir = File(projectDirectories.dataLocalDir)
            .ensureDirectoryExists()

        return File(localDataDir, group)
            .ensureDirectoryExists()
            .absolutePath
    }

    override suspend fun prime(catalyst: SpiralContext) {
        if (!primed) {
            arbitraryProgressBar(loadingText = catalyst.localise("config.loading_text.loading"), loadedText = catalyst.localise("config.loading_text.loaded")) {
                projectDirectories = ProjectDirectories.from("info", "Spiral Framework", "Spiral")
            }

            primed = true
        }
    }
}