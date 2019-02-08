package info.spiralframework.base.config

import info.spiralframework.base.util.arbitraryProgressBar
import info.spiralframework.base.util.ensureDirectoryExists
import info.spiralframework.base.util.ensureFileExists
import io.github.soc.directories.ProjectDirectories
import java.io.File

object SpiralConfig {
    //SLOW
    val projectDirectories = arbitraryProgressBar(loadingText = "Loading config...", loadedText = "Config loaded, thank you for your patience!") {
        ProjectDirectories.from("info", "Spiral Framework", "Spiral")
    }
    fun getConfigFile(module: String): File = File(File(projectDirectories.configDir).ensureDirectoryExists(), "$module.yaml").ensureFileExists()
}