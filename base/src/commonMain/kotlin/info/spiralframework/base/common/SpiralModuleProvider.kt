package info.spiralframework.base.common

import dev.brella.kornea.toolkit.common.SemanticVersion

public interface SpiralModuleProvider {
    public val moduleName: String
    public val moduleVersion: SemanticVersion

    public suspend fun register(context: SpiralContext)
}