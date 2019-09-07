package info.spiralframework.console

import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion

class SpiralModuleConsole: SpiralModuleProvider {
    override val moduleName: String = "spiral-console"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION
}