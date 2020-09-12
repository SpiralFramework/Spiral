package info.spiralframework.base.common.environment

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider

typealias DynamicEnvironmentFunction = suspend SpiralContext.(key: String) -> String?

interface SpiralEnvironment {
    object NoOp: SpiralEnvironment {
        override suspend fun SpiralContext.retrieveEnvironment(): Map<String, String> = emptyMap()
        override fun storeStaticValue(key: String, value: String) {}
        override fun storeDynamicValue(key: String, value: DynamicEnvironmentFunction) {}
        override fun storeEnvironmentalValue(key: String) {}
        override suspend fun retrieveStaticValue(key: String): String? = null
        override suspend fun retrieveEnvironmentalValue(key: String): String? = null
        override suspend fun SpiralContext.retrieveDynamicValue(key: String): String? = null
        override suspend fun addModuleProvider(moduleProvider: SpiralModuleProvider) {}
        override suspend fun SpiralContext.registerAllModules() {}
    }

    companion object {
        const val SPIRAL_MODULE_KEY = "spiral.module"
        const val SPIRAL_FILE_NAME_KEY = "spiral.name"
        const val SPIRAL_MD5_KEY = "spiral.md5"
        const val SPIRAL_SHA256_KEY = "spiral.sha256"
    }

    suspend fun SpiralContext.retrieveEnvironment(): Map<String, String>
    fun storeStaticValue(key: String, value: String)
    fun storeDynamicValue(key: String, value: DynamicEnvironmentFunction)
    fun storeEnvironmentalValue(key: String)

    suspend fun retrieveStaticValue(key: String): String?
    suspend fun retrieveEnvironmentalValue(key: String): String?
    suspend fun SpiralContext.retrieveDynamicValue(key: String): String?

    suspend fun addModuleProvider(moduleProvider: SpiralModuleProvider)
    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.registerAllModules()
}

suspend fun SpiralEnvironment.retrieveEnvironment(context: SpiralContext) = context.retrieveEnvironment()
suspend fun SpiralEnvironment.retrieveDynamicValue(context: SpiralContext, key: String) = context.retrieveDynamicValue(key)
suspend fun SpiralEnvironment.registerAllModules(context: SpiralContext) = context.registerAllModules()

operator fun SpiralEnvironment.set(key: String, value: String) = storeStaticValue(key, value)
operator fun SpiralEnvironment.set(key: String, value: DynamicEnvironmentFunction) = storeDynamicValue(key, value)