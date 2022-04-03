package info.spiralframework.base.common.environment

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider

public typealias DynamicEnvironmentFunction = suspend SpiralContext.(key: String) -> String?

public interface SpiralEnvironment {
    public object NoOp: SpiralEnvironment {
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

    public companion object {
        public const val SPIRAL_MODULE_KEY: String = "spiral.module"
        public const val SPIRAL_FILE_NAME_KEY: String = "spiral.name"
        public const val SPIRAL_MD5_KEY: String = "spiral.md5"
        public const val SPIRAL_SHA256_KEY: String = "spiral.sha256"
    }

    public suspend fun SpiralContext.retrieveEnvironment(): Map<String, String>
    public fun storeStaticValue(key: String, value: String)
    public fun storeDynamicValue(key: String, value: DynamicEnvironmentFunction)
    public fun storeEnvironmentalValue(key: String)

    public suspend fun retrieveStaticValue(key: String): String?
    public suspend fun retrieveEnvironmentalValue(key: String): String?
    public suspend fun SpiralContext.retrieveDynamicValue(key: String): String?

    public suspend fun addModuleProvider(moduleProvider: SpiralModuleProvider)
    public suspend fun SpiralContext.registerAllModules()
}

public suspend fun SpiralEnvironment.retrieveEnvironment(context: SpiralContext): Map<String, String> = context.retrieveEnvironment()
public suspend fun SpiralEnvironment.retrieveDynamicValue(context: SpiralContext, key: String): String? = context.retrieveDynamicValue(key)
public suspend fun SpiralEnvironment.registerAllModules(context: SpiralContext): Unit = context.registerAllModules()

public operator fun SpiralEnvironment.set(key: String, value: String): Unit = storeStaticValue(key, value)
public operator fun SpiralEnvironment.set(key: String, value: DynamicEnvironmentFunction): Unit = storeDynamicValue(key, value)