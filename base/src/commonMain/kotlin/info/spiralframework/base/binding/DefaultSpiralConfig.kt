package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig

expect class DefaultSpiralConfig: SpiralConfig {
    override fun SpiralContext.getConfigFile(module: String): String
    override fun SpiralContext.getLocalDataDir(group: String): String
}