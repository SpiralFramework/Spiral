package info.spiralframework.core.plugins

import info.spiralframework.core.SpiralCoreContext
import org.abimon.kornea.io.common.DataSource
import org.kornea.toolkit.common.SemanticVersion

interface ISpiralPlugin {
    val name: String
    val uid: String
    val version: SemanticVersion
    val dataSource: DataSource<*>

    fun SpiralCoreContext.load()
    fun SpiralCoreContext.unload()
}

fun ISpiralPlugin.load(context: SpiralCoreContext) = context.load()
fun ISpiralPlugin.unload(context: SpiralCoreContext) = context.unload()