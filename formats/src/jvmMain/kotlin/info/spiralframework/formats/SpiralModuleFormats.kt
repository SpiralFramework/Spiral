package info.spiralframework.formats

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.locale.addBundle

@ExperimentalUnsignedTypes
class SpiralModuleFormats: SpiralModuleProvider {
    override val moduleName: String = "spiral-formats"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override suspend fun register(context: SpiralContext) {
        context.addBundle<SpiralModuleFormats>("SpiralFormats")
    }
}