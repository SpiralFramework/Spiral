package info.spiralframework.core

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.core.serialisation.SpiralSerialisation

@ExperimentalUnsignedTypes
interface SpiralCoreContext: SpiralContext, SpiralSignatures, SpiralPluginRegistry, SpiralSerialisation {
    val updateConnectTimeout: Int
    val updateReadTimeout: Int
    val networkConnectTimeout: Int
    val networkReadTimeout: Int

    val apiBase: String
    val jenkinsBase: String

    val enabledPlugins: Map<String, SemanticVersion>

    suspend fun copy(newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newEventBus: SpiralEventBus? = null, newCacheProvider: SpiralCacheProvider? = null, newResourceLoader: SpiralResourceLoader? = null, newSignatures: SpiralSignatures? = null, newPluginRegistry: SpiralPluginRegistry? = null, newSerialisation: SpiralSerialisation? = null): SpiralContext
}

@ExperimentalUnsignedTypes
public inline fun <R> asCore(context: SpiralContext, block: SpiralCoreContext.() -> R): R? = (context as? SpiralCoreContext)?.let(block)

@ExperimentalUnsignedTypes
fun SpiralContext.core(): SpiralCoreContext? = this as? SpiralCoreContext