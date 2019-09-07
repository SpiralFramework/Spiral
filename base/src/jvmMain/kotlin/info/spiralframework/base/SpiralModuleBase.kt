package info.spiralframework.base

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion

class SpiralModuleBase: SpiralModuleProvider {
    override val moduleName: String = "spiral-base"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION
}