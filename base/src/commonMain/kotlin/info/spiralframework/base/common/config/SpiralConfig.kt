package info.spiralframework.base.common.config

import info.spiralframework.base.common.SpiralContext

public interface SpiralConfig {
    public object NoOp: SpiralConfig {
        override fun SpiralContext.getConfigFile(module: String): String = module
        override fun SpiralContext.getLocalDataDir(group: String): String = group
    }

    public fun SpiralContext.getConfigFile(module: String): String
    public fun SpiralContext.getLocalDataDir(group: String): String
}

public fun SpiralConfig.getConfigFile(context: SpiralContext, module: String): String = context.getConfigFile(module)
public fun SpiralConfig.getLocalDataDir(context: SpiralContext, group: String): String = context.getLocalDataDir(group)