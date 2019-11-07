package info.spiralframework.core

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.locale.addBundle

class SpiralModuleCore: SpiralModuleProvider {
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION
    override val moduleName: String = "spiral-core"

    override suspend fun register(context: SpiralContext) {
        context.addBundle<SpiralModuleCore>("SpiralCore")
    }
}