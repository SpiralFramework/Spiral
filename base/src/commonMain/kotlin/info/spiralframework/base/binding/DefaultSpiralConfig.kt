package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig

public expect class DefaultSpiralConfig(): SpiralConfig {
    override fun SpiralContext.getConfigFile(module: String): String
    override fun SpiralContext.getLocalDataDir(group: String): String
}