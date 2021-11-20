package info.spiralframework.base.binding

import dev.brella.kornea.toolkit.common.SuspendInit0
import dev.brella.kornea.toolkit.common.init
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.base.common.serialisation.DefaultSpiralSerialisation
import info.spiralframework.base.common.serialisation.SpiralSerialisation

@ExperimentalUnsignedTypes
expect class DefaultSpiralContext private constructor(
        locale: SpiralLocale,
        logger: SpiralLogger,
        config: SpiralConfig,
        environment: SpiralEnvironment,
        eventBus: SpiralEventBus,
        cacheProvider: SpiralCacheProvider,
        resourceLoader: SpiralResourceLoader,
        serialisation: SpiralSerialisation,
) : SpiralContext, SuspendInit0 {
    companion object {
        suspend operator fun invoke(locale: SpiralLocale, logger: SpiralLogger, config: SpiralConfig, environment: SpiralEnvironment, eventBus: SpiralEventBus, cacheProvider: SpiralCacheProvider, resourceLoader: SpiralResourceLoader, serialisation: SpiralSerialisation): DefaultSpiralContext
    }

    override suspend fun init()
}

@ExperimentalUnsignedTypes
suspend fun defaultSpiralContext(): SpiralContext =
    init(DefaultSpiralContext(DefaultSpiralLocale(), DefaultSpiralLogger("DefaultSpiral"), DefaultSpiralConfig(), DefaultSpiralEnvironment(), DefaultSpiralEventBus(), DefaultSpiralCacheProvider(), DefaultSpiralResourceLoader(), DefaultSpiralSerialisation()))