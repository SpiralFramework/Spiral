package info.spiralframework.console.jvm.commands

import dev.brella.knolus.context.KnolusContext
import info.spiralframework.base.common.SpiralContext

interface CommandRegistrar {
    suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext)
}