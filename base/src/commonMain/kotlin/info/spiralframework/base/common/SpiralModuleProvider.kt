package info.spiralframework.base.common

interface SpiralModuleProvider {
    val moduleName: String
    val moduleVersion: SemanticVersion

    fun register(context: SpiralContext)
}