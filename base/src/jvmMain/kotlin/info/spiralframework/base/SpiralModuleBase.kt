package info.spiralframework.base

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext

class SpiralModuleBase: SpiralModuleProvider {
    override val moduleName: String = "spiral-base"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override fun register(context: SpiralContext) {
        context.addBundle("SpiralBase")
    }
}