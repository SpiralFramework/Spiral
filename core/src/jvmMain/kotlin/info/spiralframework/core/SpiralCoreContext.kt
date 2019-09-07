package info.spiralframework.core

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext

interface SpiralCoreContext: SpiralContext {
    val updateConnectTimeout: Int
    val updateReadTimeout: Int
    val networkConnectTimeout: Int
    val networkReadTimeout: Int

    val apiBase: String
    val jenkinsBase: String

    val enabledPlugins: Map<String, SemanticVersion>
    val loadedModules: Map<String, SemanticVersion>
}