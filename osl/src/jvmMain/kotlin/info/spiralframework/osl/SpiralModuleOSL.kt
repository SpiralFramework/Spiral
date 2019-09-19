package info.spiralframework.osl

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider

class SpiralModuleOSL: SpiralModuleProvider {
    override val moduleName: String = "spiral-osl"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override suspend fun register(context: SpiralContext) {}
}