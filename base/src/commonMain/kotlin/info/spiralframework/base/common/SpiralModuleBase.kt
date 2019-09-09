package info.spiralframework.base.common

class SpiralModuleBase: SpiralModuleProvider {
    override val moduleName: String = "spiral-base"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override fun register(context: SpiralContext) {
        context.addBundle("SpiralBase")
    }
}