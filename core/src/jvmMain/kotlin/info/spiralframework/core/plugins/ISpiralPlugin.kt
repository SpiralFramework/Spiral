package info.spiralframework.core.plugins

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.formats.utils.DataSource

interface ISpiralPlugin {
    val name: String
    val uid: String
    val version: SemanticVersion
    val dataSource: DataSource

    fun load()
    fun unload()
}