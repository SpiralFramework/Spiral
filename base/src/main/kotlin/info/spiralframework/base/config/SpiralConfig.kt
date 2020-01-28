package info.spiralframework.base.config

import info.spiralframework.base.util.arbitraryProgressBar
import info.spiralframework.base.util.ensureDirectoryExists
import info.spiralframework.base.util.ensureFileExists
import io.github.soc.directories.ProjectDirectories
import java.io.File

object SpiralConfig {
    //SLOW
    val projectDirectories = arbitraryProgressBar(loadingText = "config.loading_text.loading", loadedText = "config.loading_text.loaded") {
        ProjectDirectories.from("info", "Spiral Framework", "Spiral")
    }
    fun getConfigFile(module: String): File = File(File(projectDirectories.configDir).ensureDirectoryExists(), "$module.yaml").ensureFileExists()
    fun getPluginFile(name: String): File = File(File(projectDirectories.dataLocalDir, "plugins").ensureDirectoryExists(), name).ensureFileExists()
}