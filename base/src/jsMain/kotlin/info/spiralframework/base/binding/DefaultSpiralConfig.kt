package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig

actual class DefaultSpiralConfig actual constructor() : SpiralConfig {
    actual override fun SpiralContext.getConfigFile(module: String): String = ""
    actual override fun SpiralContext.getLocalDataDir(group: String): String = ""

    override suspend fun prime(catalyst: SpiralContext) {}
}