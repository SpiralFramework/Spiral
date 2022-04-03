package info.spiralframework.base.binding

import info.spiralframework.base.common.SPIRAL_BASE_MODULE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.environment.DynamicEnvironmentFunction
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_FILE_NAME_KEY
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MD5_KEY
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MODULE_KEY
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_SHA256_KEY
import info.spiralframework.base.jvm.crypto.md5Hash
import info.spiralframework.base.jvm.crypto.sha256Hash
import java.io.File
import java.io.InputStream

public actual class DefaultSpiralEnvironment : SpiralEnvironment {
    private val staticEnvironment: MutableMap<String, String> = HashMap()
    private val dynamicEnvironment: MutableMap<String, DynamicEnvironmentFunction> = HashMap()
    private val environmentalVariables: MutableSet<String> = hashSetOf(
            "os.name", "os.version", "os.arch",
            "java.vendor", "java.version", "java.vendor.url",
            "file.separator", "path.separator", "line.separator",
            "path"
    )
    private val moduleProviders: MutableMap<String, SpiralModuleProvider> = HashMap()
    private val enabledModules: MutableSet<String> = HashSet()

    override suspend fun SpiralContext.retrieveEnvironment(): Map<String, String> {
        val envMap = HashMap(staticEnvironment)
        dynamicEnvironment.forEach { (key, func) -> envMap[key] = func(key) ?: return@forEach }
        environmentalVariables.forEach { key -> envMap[key] = System.getenv(key) ?: System.getProperty(key) ?: "" }
        return envMap
    }

    override fun storeStaticValue(key: String, value: String) {
        staticEnvironment[key] = value
    }

    override fun storeDynamicValue(key: String, value: DynamicEnvironmentFunction) {
        dynamicEnvironment[key] = value
    }

    override fun storeEnvironmentalValue(key: String) {
        environmentalVariables.add(key)
    }

    override suspend fun retrieveStaticValue(key: String): String? = staticEnvironment[key]
    override suspend fun retrieveEnvironmentalValue(key: String): String? = System.getenv(key)
    override suspend fun SpiralContext.retrieveDynamicValue(key: String): String? = dynamicEnvironment[key]?.invoke(this, key)

    init {
        val jarFile = File(DefaultSpiralEnvironment::class.java.protectionDomain.codeSource.location.path).takeIf(File::isFile)

        if (jarFile != null) {
            staticEnvironment[SPIRAL_FILE_NAME_KEY] = jarFile.name
            staticEnvironment[SPIRAL_MD5_KEY] = jarFile.inputStream().use(InputStream::md5Hash)
            staticEnvironment[SPIRAL_SHA256_KEY] = jarFile.inputStream().use(InputStream::sha256Hash)
        }

        staticEnvironment[SPIRAL_MODULE_KEY] = SPIRAL_BASE_MODULE
    }

    override suspend fun addModuleProvider(moduleProvider: SpiralModuleProvider) {
        moduleProviders["${moduleProvider.moduleName} v${moduleProvider.moduleVersion}"] = moduleProvider
    }

    override suspend fun SpiralContext.registerAllModules() {
        moduleProviders.forEach { (name, provider) ->
            if (name !in enabledModules) {
                provider.register(this)
                enabledModules.add(name)
            }
        }
    }
}