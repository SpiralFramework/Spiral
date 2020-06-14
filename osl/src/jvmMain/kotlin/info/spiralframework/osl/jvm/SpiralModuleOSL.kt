package info.spiralframework.osl.jvm

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.SPIRAL_VERSION
import org.kornea.toolkit.common.SemanticVersion

class SpiralModuleOSL: SpiralModuleProvider {
    override val moduleName: String = "spiral-osl"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override suspend fun register(context: SpiralContext) {}
}