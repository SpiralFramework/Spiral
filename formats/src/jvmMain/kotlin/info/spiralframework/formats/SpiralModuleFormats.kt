package info.spiralframework.formats

import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion

class SpiralModuleFormats: SpiralModuleProvider {
    override val moduleName: String = "spiral-formats"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION
}