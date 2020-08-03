package info.spiralframework.base.common

import info.spiralframework.base.common.locale.loadBundle
import dev.brella.kornea.toolkit.common.SemanticVersion

class SpiralModuleBase: SpiralModuleProvider {
    override val moduleName: String = "spiral-base"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    @ExperimentalUnsignedTypes
    override suspend fun register(context: SpiralContext) {
        //TODO: Fix SpiralContext
        context.loadBundle<SpiralModuleBase>(context, "SpiralBase")
    }
}