package info.spiralframework.osl

import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion

class SpiralModuleOSL: SpiralModuleProvider {
    override val moduleName: String = "spiral-osl"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION
}