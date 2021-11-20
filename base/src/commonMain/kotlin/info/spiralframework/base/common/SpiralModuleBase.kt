package info.spiralframework.base.common

import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.base.common.locale.loadBundle

class SpiralModuleBase: SpiralModuleProvider {
    override val moduleName: String = "spiral-base"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override suspend fun register(context: SpiralContext) {
        //TODO: Fix SpiralContext
        context.loadBundle<SpiralModuleBase>(context, "SpiralBase")
    }
}