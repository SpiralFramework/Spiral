package info.spiralframework.base.common

import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import dev.brella.kornea.toolkit.common.SemanticVersion

@ExperimentalUnsignedTypes
interface SpiralContext : SpiralLocale, SpiralLogger, SpiralConfig, SpiralEnvironment, SpiralEventBus, SpiralCacheProvider, SpiralResourceLoader {
    object NoOp : SpiralContext,
            SpiralLocale by SpiralLocale.NoOp,
            SpiralLogger by SpiralLogger.NoOp,
            SpiralConfig by SpiralConfig.NoOp,
            SpiralEnvironment by SpiralEnvironment.NoOp,
            SpiralEventBus by SpiralEventBus.NoOp,
            SpiralCacheProvider by SpiralCacheProvider.Memory(),
            SpiralResourceLoader by SpiralResourceLoader.NoOp {
        override val loadedModules: Map<String, SemanticVersion>
            get() = throw IllegalStateException("NoOp context")

        override fun subcontext(module: String): SpiralContext = this
        override suspend fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?, newCacheProvider: SpiralCacheProvider?, newResourceLoader: SpiralResourceLoader?): SpiralContext = this
    }

    val loadedModules: Map<String, SemanticVersion>

    fun subcontext(module: String): SpiralContext
    suspend fun copy(newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newEventBus: SpiralEventBus? = null, newCacheProvider: SpiralCacheProvider? = null, newResourceLoader: SpiralResourceLoader? = null): SpiralContext
}

@ExperimentalUnsignedTypes
inline fun <T> SpiralContext.with(module: String, block: SpiralContext.() -> T): T = subcontext(module).block()