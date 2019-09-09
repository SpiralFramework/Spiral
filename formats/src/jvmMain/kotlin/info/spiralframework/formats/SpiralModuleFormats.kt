package info.spiralframework.formats

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider

@ExperimentalUnsignedTypes
class SpiralModuleFormats: SpiralModuleProvider {
    override val moduleName: String = "spiral-formats"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override fun register(context: SpiralContext) {
        context.addBundle("SpiralFormats")
    }
}