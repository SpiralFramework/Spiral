package info.spiralframework.base.binding

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger

@ExperimentalUnsignedTypes
actual data class DefaultSpiralContext actual constructor(
        val locale: SpiralLocale,
        val logger: SpiralLogger,
        val config: SpiralConfig,
        val environment: SpiralEnvironment,
        val eventBus: SpiralEventBus,
        val cacheProvider: SpiralCacheProvider
) : SpiralContext,
        SpiralLocale by locale,
        SpiralLogger by logger,
        SpiralConfig by config,
        SpiralEnvironment by environment,
        SpiralEventBus by eventBus,
        SpiralCacheProvider by cacheProvider {
    actual companion object {
        actual suspend operator fun invoke(locale: SpiralLocale, logger: SpiralLogger, config: SpiralConfig, environment: SpiralEnvironment, eventBus: SpiralEventBus, cacheProvider: SpiralCacheProvider): DefaultSpiralContext {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override val loadedModules: Map<String, SemanticVersion>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun subcontext(module: String): SpiralContext {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?, newCacheProvider: SpiralCacheProvider?): SpiralContext {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prime(catalyst: SpiralContext) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun init() {
    }
}