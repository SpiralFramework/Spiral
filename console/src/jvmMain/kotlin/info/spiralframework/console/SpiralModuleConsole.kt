package info.spiralframework.console

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider

class SpiralModuleConsole: SpiralModuleProvider {
    override val moduleName: String = "spiral-console"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override fun register(context: SpiralContext) {
        context.addBundle("SpiralConsole")
        context.addBundle("SpiralConsole-Mechanic")
        context.addBundle("SpiralConsole-Pilot")
    }
}