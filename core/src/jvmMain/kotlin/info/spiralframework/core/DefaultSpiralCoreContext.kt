package info.spiralframework.core

import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MD5_KEY
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MODULE_KEY
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.core.common.SPIRAL_CORE_MODULE
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import info.spiralframework.core.common.SPIRAL_ENV_MODULES_KEY
import info.spiralframework.core.plugins.PluginEntry
import info.spiralframework.core.plugins.SpiralCorePlugin
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.core.serialisation.SpiralSerialisation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.jar.JarFile

@ExperimentalUnsignedTypes
class DefaultSpiralCoreContext private constructor(val core: SpiralCoreConfig, val locale: SpiralLocale, val logger: SpiralLogger, val config: SpiralConfig, val environment: SpiralEnvironment, val eventBus: SpiralEventBus, val cacheProvider: SpiralCacheProvider, val signatures: SpiralSignatures, val pluginRegistry: SpiralPluginRegistry, val serialisation: SpiralSerialisation)
    : SpiralCoreContext,
        SpiralLocale by locale,
        SpiralLogger by logger,
        SpiralConfig by config,
        SpiralEnvironment by environment,
        SpiralEventBus by eventBus,
        SpiralCacheProvider by cacheProvider,
        SpiralSignatures by signatures,
        SpiralPluginRegistry by pluginRegistry,
        SpiralSerialisation by serialisation {
    companion object {
        const val DEFAULT_UPDATE_CONNECT_TIMEOUT = 5000
        const val DEFAULT_UPDATE_READ_TIMEOUT = 5000

        const val DEFAULT_NETWORK_CONNECT_TIMEOUT = 5000
        const val DEFAULT_NETWORK_READ_TIMEOUT = 5000

        const val DEFAULT_API_BASE = "https://api.abimon.org/api"
        const val DEFAULT_JENKINS_BASE = "https://jenkins.abimon.org"

        val DEFAULT_ENABLED_PLUGINS = emptyMap<String, SemanticVersion>()

        suspend operator fun invoke(core: SpiralCoreConfig, locale: SpiralLocale, logger: SpiralLogger, config: SpiralConfig, environment: SpiralEnvironment, eventBus: SpiralEventBus, cacheProvider: SpiralCacheProvider, signatures: SpiralSignatures, pluginRegistry: SpiralPluginRegistry, serialisation: SpiralSerialisation): DefaultSpiralCoreContext {
            val instance = DefaultSpiralCoreContext(core, locale, logger, config, environment, eventBus, cacheProvider, signatures, pluginRegistry, serialisation)
            instance.init()
            return instance
        }

        suspend operator fun invoke(core: SpiralCoreConfig, parent: SpiralContext, signatures: SpiralSignatures, pluginRegistry: SpiralPluginRegistry, serialisation: SpiralSerialisation): DefaultSpiralCoreContext {
            val instance = DefaultSpiralCoreContext(core, parent, signatures, pluginRegistry, serialisation)
            instance.init()
            return instance
        }
    }

    private constructor(core: SpiralCoreConfig, parent: SpiralContext, signatures: SpiralSignatures, pluginRegistry: SpiralPluginRegistry, serialisation: SpiralSerialisation) : this(core, parent, parent, parent, parent, parent, parent, signatures, pluginRegistry, serialisation)

    override val updateConnectTimeout: Int = core.updateConnectTimeout ?: DEFAULT_UPDATE_CONNECT_TIMEOUT
    override val updateReadTimeout: Int = core.updateReadTimeout ?: DEFAULT_UPDATE_READ_TIMEOUT
    override val networkConnectTimeout: Int = core.networkConnectTimeout ?: DEFAULT_NETWORK_CONNECT_TIMEOUT
    override val networkReadTimeout: Int = core.networkReadTimeout ?: DEFAULT_NETWORK_READ_TIMEOUT

    override val apiBase: String = core.apiBase ?: DEFAULT_API_BASE
    override val jenkinsBase: String = core.jenkinsBase ?: DEFAULT_JENKINS_BASE

    override val enabledPlugins: Map<String, SemanticVersion> = core.enabledPlugins ?: DEFAULT_ENABLED_PLUGINS

    val moduleLoader = ServiceLoader.load(SpiralModuleProvider::class.java)
    override val loadedModules: Map<String, SemanticVersion> = moduleLoader.iterator()
            .asSequence()
            .map { module -> Pair(module.moduleName, module.moduleVersion) }
            .toMap()

    override fun subcontext(module: String): SpiralCoreContext = this

    override suspend fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?, newCacheProvider: SpiralCacheProvider?): SpiralContext {
        val instance = DefaultSpiralCoreContext(
                core,
                newLocale ?: locale,
                newLogger ?: logger,
                newConfig ?: config,
                newEnvironment ?: environment,
                newEventBus ?: eventBus,
                newCacheProvider ?: cacheProvider,
                signatures,
                pluginRegistry,
                serialisation
        )
        instance.init()
        return instance
    }

    override suspend fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?, newCacheProvider: SpiralCacheProvider?, newSignatures: SpiralSignatures?, newPluginRegistry: SpiralPluginRegistry?, newSerialisation: SpiralSerialisation?): SpiralContext {
        val instance = DefaultSpiralCoreContext(
                core,
                newLocale ?: locale,
                newLogger ?: logger,
                newConfig ?: config,
                newEnvironment ?: environment,
                newEventBus ?: eventBus,
                newCacheProvider ?: cacheProvider,
                newSignatures ?: signatures,
                newPluginRegistry ?: pluginRegistry,
                newSerialisation ?: serialisation
        )
        instance.init()
        return instance
    }

    suspend fun copy(newCore: SpiralCoreConfig? = null, newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newEventBus: SpiralEventBus? = null, newCacheProvider: SpiralCacheProvider? = null, newSignatures: SpiralSignatures? = null, newPluginRegistry: SpiralPluginRegistry? = null, newSerialisation: SpiralSerialisation? = null): SpiralCoreContext {
        val instance = DefaultSpiralCoreContext(
                newCore ?: core,
                newLocale ?: locale,
                newLogger ?: logger,
                newConfig ?: config,
                newEnvironment ?: environment,
                newEventBus ?: eventBus,
                newCacheProvider ?: cacheProvider,
                newSignatures ?: signatures,
                newPluginRegistry ?: pluginRegistry,
                newSerialisation ?: serialisation
        )
        instance.init()
        return instance
    }

    override fun prime(catalyst: SpiralContext) {
        config.prime(catalyst)
        cacheProvider.prime(this)
    }

    suspend fun init() {
        moduleLoader.iterator().forEach { module -> module.register(this) }
        prime(this)

        this.storeStaticValue(SPIRAL_MODULE_KEY, SPIRAL_CORE_MODULE)
        this.storeStaticValue(SPIRAL_ENV_MODULES_KEY, this.loadedModules.entries.joinToString { (moduleName, moduleVersion) -> "$moduleName v$moduleVersion" })

        this.storeDynamicValue(SPIRAL_ENV_BUILD_KEY) {
            if (this !is SpiralCoreContext) {
                return@storeDynamicValue null
            }
            val version = retrieveStaticValue(SPIRAL_MD5_KEY) ?: return@storeDynamicValue null
            return@storeDynamicValue buildForVersion(this, version).toString()
        }

        val file = File(DefaultSpiralCoreContext::class.java.protectionDomain.codeSource.location.path)
        if (file.isFile) {
            withContext(Dispatchers.IO) {
                JarFile(file).use { jar ->
                    jar.manifest.mainAttributes.forEach { key, value ->
                        storeStaticValue("manifest.${key.toString().toLowerCase()}", value.toString())
                    }
                }
            }
        }

        this.loadPlugin(PluginEntry(SpiralCorePlugin(this).pojo, null))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultSpiralCoreContext) return false

        if (core != other.core) return false
        if (locale != other.locale) return false
        if (logger != other.logger) return false
        if (config != other.config) return false
        if (environment != other.environment) return false
        if (eventBus != other.eventBus) return false
        if (signatures != other.signatures) return false
        if (pluginRegistry != other.pluginRegistry) return false
        if (serialisation != other.serialisation) return false
        if (updateConnectTimeout != other.updateConnectTimeout) return false
        if (updateReadTimeout != other.updateReadTimeout) return false
        if (networkConnectTimeout != other.networkConnectTimeout) return false
        if (networkReadTimeout != other.networkReadTimeout) return false
        if (apiBase != other.apiBase) return false
        if (jenkinsBase != other.jenkinsBase) return false
        if (enabledPlugins != other.enabledPlugins) return false
        if (moduleLoader != other.moduleLoader) return false
        if (loadedModules != other.loadedModules) return false

        return true
    }

    override fun hashCode(): Int {
        var result = core.hashCode()
        result = 31 * result + locale.hashCode()
        result = 31 * result + logger.hashCode()
        result = 31 * result + config.hashCode()
        result = 31 * result + environment.hashCode()
        result = 31 * result + eventBus.hashCode()
        result = 31 * result + signatures.hashCode()
        result = 31 * result + pluginRegistry.hashCode()
        result = 31 * result + serialisation.hashCode()
        result = 31 * result + updateConnectTimeout
        result = 31 * result + updateReadTimeout
        result = 31 * result + networkConnectTimeout
        result = 31 * result + networkReadTimeout
        result = 31 * result + apiBase.hashCode()
        result = 31 * result + jenkinsBase.hashCode()
        result = 31 * result + enabledPlugins.hashCode()
        result = 31 * result + (moduleLoader?.hashCode() ?: 0)
        result = 31 * result + loadedModules.hashCode()
        return result
    }
}