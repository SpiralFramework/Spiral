package info.spiralframework.core

import dev.brella.kornea.toolkit.common.SemanticVersion
import dev.brella.kornea.toolkit.common.SuspendInit0
import dev.brella.kornea.toolkit.common.init
import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleBase
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MD5_KEY
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MODULE_KEY
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.base.common.serialisation.SpiralSerialisation
import info.spiralframework.base.common.tryPrime
import info.spiralframework.core.common.SPIRAL_CORE_MODULE
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import info.spiralframework.core.common.SPIRAL_ENV_MODULES_KEY
import info.spiralframework.core.plugins.PluginEntry
import info.spiralframework.core.plugins.SpiralCorePlugin
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.formats.jvm.SpiralModuleFormats
import info.spiralframework.osl.jvm.SpiralModuleOSL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.jar.JarFile
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

public class DefaultSpiralCoreContext private constructor(
    public val core: SpiralCoreConfig,
    public val locale: SpiralLocale,
    public val logger: SpiralLogger,
    public val config: SpiralConfig,
    public val environment: SpiralEnvironment,
    public val eventBus: SpiralEventBus,
    public val cacheProvider: SpiralCacheProvider,
    public val resourceLoader: SpiralResourceLoader,
    public val serialisation: SpiralSerialisation,
    public val parentCoroutineContext: CoroutineContext?,
    public val signatures: SpiralSignatures,
    public val pluginRegistry: SpiralPluginRegistry,
    public val http: SpiralHttp,
) : SpiralCoreContext, SuspendInit0, SpiralCatalyst<SpiralCoreContext>,
    SpiralLocale by locale,
    SpiralLogger by logger,
    SpiralConfig by config,
    SpiralEnvironment by environment,
    SpiralEventBus by eventBus,
    SpiralCacheProvider by cacheProvider,
    SpiralResourceLoader by resourceLoader,
    SpiralSerialisation by serialisation,
    SpiralSignatures by signatures,
    SpiralPluginRegistry by pluginRegistry,
    SpiralHttp by http {
    public companion object {
        public const val DEFAULT_SOCKET_TIMEOUT: Int = 10_000
        public const val DEFAULT_CONNECT_TIMEOUT: Int = 10_000
        public const val DEFAULT_REQUEST_TIMEOUT: Int = 20_000

        public const val DEFAULT_API_BASE: String = "https://api.abimon.org/api"
        public const val DEFAULT_JENKINS_BASE: String = "https://jenkins.abimon.org"

        public val DEFAULT_ENABLED_PLUGINS: Map<String, SemanticVersion> = emptyMap()

        public suspend operator fun invoke(
            core: SpiralCoreConfig,
            locale: SpiralLocale,
            logger: SpiralLogger,
            config: SpiralConfig,
            environment: SpiralEnvironment,
            eventBus: SpiralEventBus,
            cacheProvider: SpiralCacheProvider,
            resourceLoader: SpiralResourceLoader,
            serialisation: SpiralSerialisation,
            parentCoroutineContext: CoroutineContext?,
            signatures: SpiralSignatures,
            pluginRegistry: SpiralPluginRegistry,
            http: SpiralHttp
        ): DefaultSpiralCoreContext =
            init(
                DefaultSpiralCoreContext(
                    core,
                    locale,
                    logger,
                    config,
                    environment,
                    eventBus,
                    cacheProvider,
                    resourceLoader,
                    serialisation,
                    parentCoroutineContext,
                    signatures,
                    pluginRegistry,
                    http
                )
            )

        public suspend operator fun invoke(
            core: SpiralCoreConfig,
            parent: SpiralContext,
            signatures: SpiralSignatures,
            pluginRegistry: SpiralPluginRegistry,
            http: SpiralHttp
        ): DefaultSpiralCoreContext =
            init(DefaultSpiralCoreContext(core, parent, signatures, pluginRegistry, http))
    }

    private constructor(
        core: SpiralCoreConfig,
        parent: SpiralContext,
        signatures: SpiralSignatures,
        pluginRegistry: SpiralPluginRegistry,
        http: SpiralHttp
    ) : this(
        core,
        parent,
        parent,
        parent,
        parent,
        parent,
        parent,
        parent,
        parent,
        parent.coroutineContext,
        signatures,
        pluginRegistry,
        http
    )

    override val coroutineContext: CoroutineContext = parentCoroutineContext?.plus(SupervisorJob()) ?: SupervisorJob()

    override val socketTimeout: Int = core.socketTimeout ?: DEFAULT_SOCKET_TIMEOUT
    override val connectTimeout: Int = core.connectTimeout ?: DEFAULT_CONNECT_TIMEOUT
    override val requestTimeout: Int = core.requestTimeout ?: DEFAULT_REQUEST_TIMEOUT

    override val apiBase: String = core.apiBase ?: DEFAULT_API_BASE
    override val jenkinsBase: String = core.jenkinsBase ?: DEFAULT_JENKINS_BASE

    override val enabledPlugins: Map<String, SemanticVersion> = core.enabledPlugins ?: DEFAULT_ENABLED_PLUGINS

    private val moduleLoader = ServiceLoader.load(SpiralModuleProvider::class.java)
    override val klass: KClass<SpiralCoreContext> = SpiralCoreContext::class
    private var primed: Boolean = false
    override val loadedModules: Map<String, SemanticVersion> = moduleLoader.iterator()
        .asSequence()
        .map { module -> Pair(module.moduleName, module.moduleVersion) }
        .toMap()

    override fun subcontext(module: String): SpiralCoreContext = this

    public override suspend fun copy(
        newLocale: SpiralLocale?,
        newLogger: SpiralLogger?,
        newConfig: SpiralConfig?,
        newEnvironment: SpiralEnvironment?,
        newEventBus: SpiralEventBus?,
        newCacheProvider: SpiralCacheProvider?,
        newResourceLoader: SpiralResourceLoader?,
        newSerialisation: SpiralSerialisation?,
        newParentCoroutineContext: CoroutineContext?,
    ): SpiralContext {
        val instance = DefaultSpiralCoreContext(
            core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newEventBus ?: eventBus,
            newCacheProvider ?: cacheProvider,
            newResourceLoader ?: resourceLoader,
            newSerialisation ?: serialisation,
            newParentCoroutineContext ?: parentCoroutineContext,
            signatures,
            pluginRegistry,
            http
        )
        instance.init()
        return instance
    }

    override suspend fun copy(
        newLocale: SpiralLocale?,
        newLogger: SpiralLogger?,
        newConfig: SpiralConfig?,
        newEnvironment: SpiralEnvironment?,
        newEventBus: SpiralEventBus?,
        newCacheProvider: SpiralCacheProvider?,
        newResourceLoader: SpiralResourceLoader?,
        newSerialisation: SpiralSerialisation?,
        newParentCoroutineContext: CoroutineContext?,
        newSignatures: SpiralSignatures?,
        newPluginRegistry: SpiralPluginRegistry?,
        newHttp: SpiralHttp?
    ): SpiralContext {
        val instance = DefaultSpiralCoreContext(
            core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newEventBus ?: eventBus,
            newCacheProvider ?: cacheProvider,
            newResourceLoader ?: resourceLoader,
            newSerialisation ?: serialisation,
            newParentCoroutineContext ?: parentCoroutineContext,
            newSignatures ?: signatures,
            newPluginRegistry ?: pluginRegistry,
            newHttp ?: http
        )
        instance.init()
        return instance
    }

    public suspend fun copy(
        newCore: SpiralCoreConfig? = null,
        newLocale: SpiralLocale? = null,
        newLogger: SpiralLogger? = null,
        newConfig: SpiralConfig? = null,
        newEnvironment: SpiralEnvironment? = null,
        newEventBus: SpiralEventBus? = null,
        newCacheProvider: SpiralCacheProvider? = null,
        newResourceLoader: SpiralResourceLoader? = null,
        newSerialisation: SpiralSerialisation? = null,
        newParentCoroutineContext: CoroutineContext? = null,
        newSignatures: SpiralSignatures? = null,
        newPluginRegistry: SpiralPluginRegistry? = null,
        newHttp: SpiralHttp? = null
    ): SpiralCoreContext {
        val instance = DefaultSpiralCoreContext(
            newCore ?: core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newEventBus ?: eventBus,
            newCacheProvider ?: cacheProvider,
            newResourceLoader ?: resourceLoader,
            newSerialisation ?: serialisation,
            newParentCoroutineContext ?: parentCoroutineContext,
            newSignatures ?: signatures,
            newPluginRegistry ?: pluginRegistry,
            newHttp ?: http
        )
        instance.init()
        return instance
    }

    override suspend fun prime(catalyst: SpiralCoreContext) {
        if (!primed) {
            tryPrime(locale)
            tryPrime(logger)
            tryPrime(config)
            tryPrime(environment)
            tryPrime(eventBus)
            tryPrime(cacheProvider)
            tryPrime(resourceLoader)
            tryPrime(serialisation)
            tryPrime(signatures)
            tryPrime(pluginRegistry)
            tryPrime(http)

            primed = true
        }
    }

    override suspend fun init() {
        addModuleProvider(SpiralModuleBase())
        addModuleProvider(SpiralModuleFormats())
        addModuleProvider(SpiralModuleOSL())
        addModuleProvider(SpiralModuleCore())

        moduleLoader.iterator().forEach { module -> addModuleProvider(module) }
        registerAllModules()

        prime(this)

        this.storeStaticValue(SPIRAL_MODULE_KEY, SPIRAL_CORE_MODULE)
        this.storeStaticValue(
            SPIRAL_ENV_MODULES_KEY,
            this.loadedModules.entries.joinToString { (moduleName, moduleVersion) -> "$moduleName v$moduleVersion" })

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
                        storeStaticValue("manifest.${key.toString().lowercase(Locale.getDefault())}", value.toString())
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
        if (serialisation != other.serialisation) return false
        if (eventBus != other.eventBus) return false
        if (signatures != other.signatures) return false
        if (pluginRegistry != other.pluginRegistry) return false
        if (http != other.http) return false

        if (socketTimeout != other.socketTimeout) return false
        if (connectTimeout != other.connectTimeout) return false
        if (requestTimeout != other.requestTimeout) return false

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

        result = 31 * result + socketTimeout.hashCode()
        result = 31 * result + connectTimeout.hashCode()
        result = 31 * result + requestTimeout.hashCode()

        result = 31 * result + apiBase.hashCode()
        result = 31 * result + jenkinsBase.hashCode()
        result = 31 * result + enabledPlugins.hashCode()
        result = 31 * result + (moduleLoader?.hashCode() ?: 0)
        result = 31 * result + loadedModules.hashCode()
        return result
    }
}