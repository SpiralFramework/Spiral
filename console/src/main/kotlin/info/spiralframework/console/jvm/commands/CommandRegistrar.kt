package info.spiralframework.console.jvm.commands

import dev.brella.zshk.common.ShellEnvironment
import info.spiralframework.base.common.SpiralContext

public interface CommandRegistrar {
    public suspend fun register(spiralContext: SpiralContext, env: ShellEnvironment)
}