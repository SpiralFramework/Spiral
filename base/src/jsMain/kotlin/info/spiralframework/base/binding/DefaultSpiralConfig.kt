package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig

public actual class DefaultSpiralConfig actual constructor() : SpiralConfig {
    actual override fun SpiralContext.getConfigFile(module: String): String = ""
    actual override fun SpiralContext.getLocalDataDir(group: String): String = ""
}