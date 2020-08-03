package info.spiralframework.base.common

import dev.brella.kornea.toolkit.common.SemanticVersion

interface SpiralModuleProvider {
    val moduleName: String
    val moduleVersion: SemanticVersion

    suspend fun register(context: SpiralContext)
}