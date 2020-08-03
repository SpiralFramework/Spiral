package info.spiralframework.core

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.locale.loadBundle
import dev.brella.kornea.toolkit.common.SemanticVersion

class SpiralModuleCore: SpiralModuleProvider {
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION
    override val moduleName: String = "spiral-core"

    override suspend fun register(context: SpiralContext) {
        context.loadBundle<SpiralModuleCore>(context, "SpiralCore")
    }
}