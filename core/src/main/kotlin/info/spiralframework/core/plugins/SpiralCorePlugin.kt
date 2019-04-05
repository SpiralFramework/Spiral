package info.spiralframework.core.plugins

import info.spiralframework.base.util.SemVer
import info.spiralframework.core.SpiralCoreData
import info.spiralframework.formats.utils.DataSource
import java.io.File

object SpiralCorePlugin: ISpiralPlugin {
    override val name: String = "Spiral Framework"
    override val uid: String = "SPIRAL_FRAMEWORK"
    override val semanticVersion: SemVer = SemVer(0, SpiralCoreData.jenkinsBuild ?: 0)
    override val displayVersion: String = SpiralCoreData.version ?: "Developer"
    private val jarFile = File(SpiralCorePlugin::class.java.protectionDomain.codeSource.location.path).takeIf(File::isFile)
    override val dataSource: DataSource = jarFile?.let { jar -> jar::inputStream } ?: ByteArray(0)::inputStream

    override fun load() {}
    override fun unload() {}
}