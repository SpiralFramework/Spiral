package info.spiralframework.base.config

import info.spiralframework.base.util.ensureExists
import io.github.soc.directories.ProjectDirectories
import java.io.File

object SpiralConfig {
    val projectDirectories = ProjectDirectories.from("info", "Spiral Framework", "Spiral")
    fun getConfigFile(module: String): File = File(File(projectDirectories.configDir).ensureExists(), "$module.yaml").ensureExists()
}