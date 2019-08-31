package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.text.arbitraryProgressBar
import info.spiralframework.base.util.ensureDirectoryExists
import info.spiralframework.base.util.ensureFileExists
import io.github.soc.directories.ProjectDirectories
import kotlinx.coroutines.runBlocking
import java.io.File

actual class DefaultSpiralConfig: SpiralConfig {
    private val _projectDirectories = primedLazy { context: SpiralContext ->
        runBlocking {
            context.arbitraryProgressBar(loadingText = "config.loading_text.loading", loadedText = "config.loading_text.loaded") {
                ProjectDirectories.from("info", "Spiral Framework", "Spiral")
            }
        }
    }
    val projectDirectories: ProjectDirectories by _projectDirectories

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

    override fun prime(catalyst: SpiralContext) {
        _projectDirectories.prime(catalyst)
    }
}