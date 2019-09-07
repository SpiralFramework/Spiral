package info.spiralframework.core.plugins

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.core.SpiralCoreContext
import info.spiralframework.formats.utils.DataSource

interface ISpiralPlugin {
    val name: String
    val uid: String
    val version: SemanticVersion
    val dataSource: DataSource

    fun SpiralCoreContext.load()
    fun SpiralCoreContext.unload()
}

fun ISpiralPlugin.load(context: SpiralCoreContext) = context.load()
fun ISpiralPlugin.unload(context: SpiralCoreContext) = context.unload()