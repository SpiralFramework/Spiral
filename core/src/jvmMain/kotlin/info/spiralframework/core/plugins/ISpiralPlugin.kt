package info.spiralframework.core.plugins

import info.spiralframework.base.util.SemVer
import info.spiralframework.formats.utils.DataSource

interface ISpiralPlugin {
    val name: String
    val uid: String
    val version: SemVer
    val dataSource: DataSource

    fun load()
    fun unload()
}