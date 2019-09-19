package info.spiralframework.base.common

import info.spiralframework.base.common.locale.addBundle

class SpiralModuleBase: SpiralModuleProvider {
    override val moduleName: String = "spiral-base"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    @ExperimentalUnsignedTypes
    override suspend fun register(context: SpiralContext) {
        context.addBundle<SpiralModuleBase>("SpiralBase")
    }
}