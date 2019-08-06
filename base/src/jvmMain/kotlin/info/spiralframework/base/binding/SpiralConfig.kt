package info.spiralframework.base.binding

import info.spiralframework.base.util.arbitraryProgressBar
import info.spiralframework.base.util.ensureDirectoryExists
import info.spiralframework.base.util.ensureFileExists
import io.github.soc.directories.ProjectDirectories
import java.io.File

actual object SpiralConfig {
    val projectDirectories: ProjectDirectories by lazy {
        arbitraryProgressBar(loadingText = "config.loading_text.loading", loadedText = "config.loading_text.loaded") {
            ProjectDirectories.from("info", "Spiral Framework", "Spiral")
        }
    }

    actual fun getConfigFile(module: String): String {
        val configDir = File(projectDirectories.configDir)
                .ensureDirectoryExists()

        return File(configDir, "$module.yaml")
                .ensureFileExists()
                .absolutePath
    }

    actual fun getLocalDataDir(group: String): String {
        val localDataDir = File(projectDirectories.dataLocalDir)
                .ensureDirectoryExists()

        return File(localDataDir, group)
                .ensureDirectoryExists()
                .absolutePath
    }
}