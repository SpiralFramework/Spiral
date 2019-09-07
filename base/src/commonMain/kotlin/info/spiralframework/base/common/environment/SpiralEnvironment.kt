package info.spiralframework.base.common.environment

import info.spiralframework.base.common.SpiralContext

typealias DynamicEnvironmentFunction = SpiralContext.(key: String) -> String?

interface SpiralEnvironment {
    object NoOp: SpiralEnvironment {
        override fun SpiralContext.retrieveEnvironment(): Map<String, String> = emptyMap()
        override fun storeStaticValue(key: String, value: String) {}
        override fun storeDynamicValue(key: String, value: DynamicEnvironmentFunction) {}
        override fun storeEnvironmentalValue(key: String) {}
        override fun retrieveStaticValue(key: String): String? = null
        override fun retrieveEnvironmentalValue(key: String): String? = null
        override fun SpiralContext.retrieveDynamicValue(key: String): String? = null
    }

    companion object {
        const val SPIRAL_MODULE_KEY = "spiral.module"
        const val SPIRAL_FILE_NAME_KEY = "spiral.name"
        const val SPIRAL_MD5_KEY = "spiral.md5"
        const val SPIRAL_SHA256_KEY = "spiral.sha256"
    }

    fun SpiralContext.retrieveEnvironment(): Map<String, String>
    fun storeStaticValue(key: String, value: String)
    fun storeDynamicValue(key: String, value: DynamicEnvironmentFunction)
    fun storeEnvironmentalValue(key: String)

    fun retrieveStaticValue(key: String): String?
    fun retrieveEnvironmentalValue(key: String): String?
    fun SpiralContext.retrieveDynamicValue(key: String): String?
}

fun SpiralEnvironment.retrieveEnvironment(context: SpiralContext) = context.retrieveEnvironment()
fun SpiralEnvironment.retrieveDynamicValue(context: SpiralContext, key: String) = context.retrieveDynamicValue(key)

operator fun SpiralEnvironment.set(key: String, value: String) = storeStaticValue(key, value)
operator fun SpiralEnvironment.set(key: String, value: DynamicEnvironmentFunction) = storeDynamicValue(key, value)