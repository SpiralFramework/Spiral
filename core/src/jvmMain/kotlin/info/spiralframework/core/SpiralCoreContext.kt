package info.spiralframework.core

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.core.serialisation.SpiralSerialisation

interface SpiralCoreContext: SpiralContext, SpiralSignatures, SpiralPluginRegistry, SpiralSerialisation {
    val updateConnectTimeout: Int
    val updateReadTimeout: Int
    val networkConnectTimeout: Int
    val networkReadTimeout: Int

    val apiBase: String
    val jenkinsBase: String

    val enabledPlugins: Map<String, SemanticVersion>

    fun copy(newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newSignatures: SpiralSignatures? = null, newPluginRegistry: SpiralPluginRegistry? = null, newSerialisation: SpiralSerialisation? = null): SpiralContext
}