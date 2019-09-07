package info.spiralframework.base.common

import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger

interface SpiralContext : SpiralLocale, SpiralLogger, SpiralConfig, SpiralEnvironment {
    object NoOp : SpiralContext,
            SpiralLocale by SpiralLocale.NoOp,
            SpiralLogger by SpiralLogger.NoOp,
            SpiralConfig by SpiralConfig.NoOp,
            SpiralEnvironment by SpiralEnvironment.NoOp {
        override fun subcontext(module: String): SpiralContext = this
        override fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?): SpiralContext = this
    }

    fun subcontext(module: String): SpiralContext
    fun copy(newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null): SpiralContext
}

inline fun <T> SpiralContext.with(module: String, block: SpiralContext.() -> T): T = subcontext(module).block()