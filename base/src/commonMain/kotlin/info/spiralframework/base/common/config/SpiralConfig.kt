package info.spiralframework.base.common.config

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext

interface SpiralConfig: SpiralCatalyst<SpiralContext> {
    object NoOp: SpiralConfig {
        override fun SpiralContext.getConfigFile(module: String): String = module
        override fun SpiralContext.getLocalDataDir(group: String): String = group

        override fun prime(catalyst: SpiralContext) {}
    }

    fun SpiralContext.getConfigFile(module: String): String
    fun SpiralContext.getLocalDataDir(group: String): String
}

fun SpiralConfig.getConfigFile(module: String, context: SpiralContext): String = context.getConfigFile(module)
fun SpiralConfig.getLocalDataDir(group: String, context: SpiralContext): String = context.getLocalDataDir(group)