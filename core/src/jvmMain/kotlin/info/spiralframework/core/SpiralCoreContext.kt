package info.spiralframework.core

import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.base.common.serialisation.SpiralSerialisation
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import kotlin.coroutines.CoroutineContext

public interface SpiralCoreContext : SpiralContext, SpiralSignatures, SpiralPluginRegistry, SpiralHttp {
    public val socketTimeout: Int
    public val connectTimeout: Int
    public val requestTimeout: Int

    public val apiBase: String
    public val jenkinsBase: String

    public val enabledPlugins: Map<String, SemanticVersion>

    public suspend fun copy(
        newLocale: SpiralLocale? = null,
        newLogger: SpiralLogger? = null,
        newConfig: SpiralConfig? = null,
        newEnvironment: SpiralEnvironment? = null,
        newEventBus: SpiralEventBus? = null,
        newCacheProvider: SpiralCacheProvider? = null,
        newResourceLoader: SpiralResourceLoader? = null,
        newSerialisation: SpiralSerialisation? = null,
        newParentCoroutineContext: CoroutineContext? = null,
        newSignatures: SpiralSignatures? = null,
        newPluginRegistry: SpiralPluginRegistry? = null,
        newHttp: SpiralHttp? = null
    ): SpiralContext
}

public inline fun <R> asCore(context: SpiralContext, block: SpiralCoreContext.() -> R): R? =
    (context as? SpiralCoreContext)?.let(block)

public fun SpiralContext.core(): SpiralCoreContext? = this as? SpiralCoreContext