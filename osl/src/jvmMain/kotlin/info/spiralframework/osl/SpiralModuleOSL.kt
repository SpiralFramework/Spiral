package info.spiralframework.osl

import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext

class SpiralModuleOSL: SpiralModuleProvider {
    override val moduleName: String = "spiral-osl"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override fun register(context: SpiralContext) {}
}