package info.spiralframework.console

import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.locale.addBundle

class SpiralModuleConsole: SpiralModuleProvider {
    override val moduleName: String = "spiral-console"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override suspend fun register(context: SpiralContext) {
        context.addBundle<SpiralModuleConsole>("SpiralConsole")
        context.addBundle<SpiralModuleConsole>("SpiralConsole-Mechanic")
        context.addBundle<SpiralModuleConsole>("SpiralConsole-Pilot")
    }
}