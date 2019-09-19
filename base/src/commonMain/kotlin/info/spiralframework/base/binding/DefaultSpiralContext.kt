package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger

@ExperimentalUnsignedTypes
expect class DefaultSpiralContext private constructor(
        locale: SpiralLocale,
        logger: SpiralLogger,
        config: SpiralConfig,
        environment: SpiralEnvironment,
        eventBus: SpiralEventBus,
        cacheProvider: SpiralCacheProvider
) : SpiralContext {
    companion object {
        suspend operator fun invoke(locale: SpiralLocale, logger: SpiralLogger, config: SpiralConfig, environment: SpiralEnvironment, eventBus: SpiralEventBus, cacheProvider: SpiralCacheProvider): DefaultSpiralContext
    }

    suspend fun init()
}

@ExperimentalUnsignedTypes
suspend fun defaultSpiralContext(): SpiralContext {
    val context = DefaultSpiralContext(DefaultSpiralLocale(), DefaultSpiralLogger("DefaultSpiral"), DefaultSpiralConfig(), DefaultSpiralEnvironment(), DefaultSpiralEventBus(), DefaultSpiralCacheProvider())
    context.init()
    return context
}