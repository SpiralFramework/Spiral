package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.DefaultSpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger

@ExperimentalUnsignedTypes
expect class DefaultSpiralContext(
        locale: SpiralLocale,
        logger: SpiralLogger,
        config: SpiralConfig,
        environment: SpiralEnvironment,
        eventBus: SpiralEventBus,
        cacheProvider: SpiralCacheProvider
) : SpiralContext

@ExperimentalUnsignedTypes
fun defaultSpiralContext(): SpiralContext = DefaultSpiralContext(DefaultSpiralLocale(), DefaultSpiralLogger("DefaultSpiral"), DefaultSpiralConfig(), DefaultSpiralEnvironment(), DefaultSpiralEventBus(), DefaultSpiralCacheProvider())