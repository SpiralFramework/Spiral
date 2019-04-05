package info.spiralframework.core.plugins

import info.spiralframework.base.util.SemVer
import info.spiralframework.formats.utils.DataSource

interface ISpiralPlugin {
    val name: String
    val uid: String
    val description: String
        get() = ""
    val author: String
        get() = ""
    val website: String
        get() = ""
    val semanticVersion: SemVer
    val displayVersion: String
        get() = "v${semanticVersion.major}.${semanticVersion.minor}.${semanticVersion.patch}"
    val dataSource: DataSource

    fun load()
    fun unload()
}