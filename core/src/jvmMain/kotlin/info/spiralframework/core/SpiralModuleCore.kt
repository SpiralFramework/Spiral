package info.spiralframework.core

import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext

class SpiralModuleCore: SpiralModuleProvider {
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION
    override val moduleName: String = "spiral-core"

    override fun register(context: SpiralContext) {
        context.addBundle("SpiralCore")
    }
}