package info.spiralframework.core.plugins

import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.core.SpiralCoreContext

public interface ISpiralPlugin {
    public val name: String
    public val uid: String
    public val version: SemanticVersion
    public val dataSource: DataSource<*>

    public fun SpiralCoreContext.load()
    public fun SpiralCoreContext.unload()
}

public fun ISpiralPlugin.load(context: SpiralCoreContext): Unit = context.load()
public fun ISpiralPlugin.unload(context: SpiralCoreContext): Unit = context.unload()