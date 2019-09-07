package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.DefaultSpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger

expect class DefaultSpiralContext(
        locale: SpiralLocale,
        logger: SpiralLogger,
        config: SpiralConfig,
        environment: SpiralEnvironment,
        eventBus: SpiralEventBus
) : SpiralContext

fun defaultSpiralContext(): SpiralContext = DefaultSpiralContext(DefaultSpiralLocale(), DefaultSpiralLogger("DefaultSpiral"), DefaultSpiralConfig(), DefaultSpiralEnvironment(), DefaultSpiralEventBus())