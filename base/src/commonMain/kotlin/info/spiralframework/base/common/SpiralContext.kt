package info.spiralframework.base.common

import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger

interface SpiralContext : SpiralLocale, SpiralLogger, SpiralConfig, SpiralEnvironment, SpiralEventBus {
    object NoOp : SpiralContext,
            SpiralLocale by SpiralLocale.NoOp,
            SpiralLogger by SpiralLogger.NoOp,
            SpiralConfig by SpiralConfig.NoOp,
            SpiralEnvironment by SpiralEnvironment.NoOp,
            SpiralEventBus by SpiralEventBus.NoOp {
        override val loadedModules: Map<String, SemanticVersion>
            get() = throw IllegalStateException("NoOp context")

        override fun subcontext(module: String): SpiralContext = this
        override suspend fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?): SpiralContext = this
    }

    val loadedModules: Map<String, SemanticVersion>

    fun subcontext(module: String): SpiralContext
    suspend fun copy(newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newEventBus: SpiralEventBus? = null): SpiralContext
}

inline fun <T> SpiralContext.with(module: String, block: SpiralContext.() -> T): T = subcontext(module).block()