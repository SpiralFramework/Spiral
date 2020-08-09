package info.spiralframework.base.common.config

import dev.brella.kornea.toolkit.common.SuspendInit1
import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext

interface SpiralConfig: SpiralCatalyst<SpiralContext> {
    object NoOp: SpiralConfig {
        override fun SpiralContext.getConfigFile(module: String): String = module
        override fun SpiralContext.getLocalDataDir(group: String): String = group

        override suspend fun prime(catalyst: SpiralContext) {}
    }

    fun SpiralContext.getConfigFile(module: String): String
    fun SpiralContext.getLocalDataDir(group: String): String
}

fun SpiralConfig.getConfigFile(context: SpiralContext, module: String): String = context.getConfigFile(module)
fun SpiralConfig.getLocalDataDir(context: SpiralContext, group: String): String = context.getLocalDataDir(group)