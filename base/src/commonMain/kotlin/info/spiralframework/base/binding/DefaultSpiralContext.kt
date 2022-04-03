package info.spiralframework.base.binding

import dev.brella.kornea.toolkit.common.SuspendInit0
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
import kotlin.coroutines.CoroutineContext

public expect class DefaultSpiralContext private constructor(
    locale: SpiralLocale,
    logger: SpiralLogger,
    config: SpiralConfig,
    environment: SpiralEnvironment,
    eventBus: SpiralEventBus,
    cacheProvider: SpiralCacheProvider,
    resourceLoader: SpiralResourceLoader,
    serialisation: SpiralSerialisation,
    parentCoroutineContext: CoroutineContext? = null,
) : SpiralContext, SuspendInit0 {
    public companion object {
        public suspend operator fun invoke(
            locale: SpiralLocale,
            logger: SpiralLogger,
            config: SpiralConfig,
            environment: SpiralEnvironment,
            eventBus: SpiralEventBus,
            cacheProvider: SpiralCacheProvider,
            resourceLoader: SpiralResourceLoader,
            serialisation: SpiralSerialisation,
            parentCoroutineContext: CoroutineContext? = null,
        ): DefaultSpiralContext
    }

    override suspend fun init()
}

public suspend fun defaultSpiralContext(parentCoroutineContext: CoroutineContext? = null): SpiralContext =
    DefaultSpiralContext.invoke(
        DefaultSpiralLocale(),
        DefaultSpiralLogger("DefaultSpiral"),
        DefaultSpiralConfig(),
        DefaultSpiralEnvironment(),
        DefaultSpiralEventBus(),
        DefaultSpiralCacheProvider(),
        DefaultSpiralResourceLoader(),
        DefaultSpiralSerialisation(),
        parentCoroutineContext
    )