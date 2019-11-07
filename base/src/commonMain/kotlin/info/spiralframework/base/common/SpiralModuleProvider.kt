package info.spiralframework.base.common

interface SpiralModuleProvider {
    val moduleName: String
    val moduleVersion: SemanticVersion

    suspend fun register(context: SpiralContext)
}