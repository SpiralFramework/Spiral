package info.spiralframework.base

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext

interface SpiralModuleProvider {
    val moduleName: String
    val moduleVersion: SemanticVersion

    fun register(context: SpiralContext)
}