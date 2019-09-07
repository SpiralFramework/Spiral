package info.spiralframework.console.data

import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.core.DefaultSpiralCoreContext
import info.spiralframework.core.SpiralCoreConfig
import info.spiralframework.core.SpiralCoreContext
import info.spiralframework.core.buildForVersion
import info.spiralframework.core.common.SPIRAL_CORE_MODULE
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import info.spiralframework.core.common.SPIRAL_ENV_MODULES_KEY
import info.spiralframework.core.plugins.PluginEntry
import info.spiralframework.core.plugins.SpiralCorePlugin
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.core.serialisation.SpiralSerialisation
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*
import java.util.jar.JarFile

data class DefaultSpiralCockpitContext(override val args: GurrenArgs, val core: SpiralCoreConfig, val locale: SpiralLocale, val logger: SpiralLogger, val config: SpiralConfig, val environment: SpiralEnvironment, val eventBus: SpiralEventBus, val signatures: SpiralSignatures, val pluginRegistry: SpiralPluginRegistry, val serialisation: SpiralSerialisation)
    : SpiralCockpitContext,
        SpiralLocale by locale,
        SpiralLogger by logger,
        SpiralConfig by config,
        SpiralEnvironment by environment,
        SpiralEventBus by eventBus,
        SpiralSignatures by signatures,
        SpiralPluginRegistry by pluginRegistry,
        SpiralSerialisation by serialisation {

    constructor(args: GurrenArgs, core: SpiralCoreConfig, parent: SpiralContext, signatures: SpiralSignatures, pluginRegistry: SpiralPluginRegistry, serialisation: SpiralSerialisation): this(args, core, parent, parent, parent, parent, parent, signatures, pluginRegistry, serialisation)
    constructor(args: GurrenArgs, parent: SpiralCoreContext): this(args, if (parent is DefaultSpiralCoreContext) parent.core else SpiralCoreConfig(parent), parent, parent, parent, parent, parent, parent, parent, parent)

    override val updateConnectTimeout: Int = core.updateConnectTimeout ?: DefaultSpiralCoreContext.DEFAULT_UPDATE_CONNECT_TIMEOUT
    override val updateReadTimeout: Int = core.updateReadTimeout ?: DefaultSpiralCoreContext.DEFAULT_UPDATE_READ_TIMEOUT
    override val networkConnectTimeout: Int = core.networkConnectTimeout ?: DefaultSpiralCoreContext.DEFAULT_NETWORK_CONNECT_TIMEOUT
    override val networkReadTimeout: Int = core.networkReadTimeout ?: DefaultSpiralCoreContext.DEFAULT_NETWORK_READ_TIMEOUT

    override val apiBase: String = core.apiBase ?: DefaultSpiralCoreContext.DEFAULT_API_BASE
    override val jenkinsBase: String = core.jenkinsBase ?: DefaultSpiralCoreContext.DEFAULT_JENKINS_BASE

    override val enabledPlugins: Map<String, SemanticVersion> = core.enabledPlugins ?: DefaultSpiralCoreContext.DEFAULT_ENABLED_PLUGINS

    val moduleLoader: ServiceLoader<SpiralModuleProvider> = ServiceLoader.load(SpiralModuleProvider::class.java)
    override val loadedModules: Map<String, SemanticVersion> = moduleLoader.iterator()
            .asSequence()
            .map { module -> Pair(module.moduleName, module.moduleVersion) }
            .toMap()

    override fun subcontext(module: String): SpiralCoreContext = this

    override fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?): SpiralCockpitContext = DefaultSpiralCockpitContext(
            args,
            core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newEventBus ?: eventBus,
            signatures,
            pluginRegistry,
            serialisation
    )

    override fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?, newSignatures: SpiralSignatures?, newPluginRegistry: SpiralPluginRegistry?, newSerialisation: SpiralSerialisation?): SpiralCockpitContext = DefaultSpiralCockpitContext(
            args,
            core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newEventBus ?: eventBus,
            newSignatures ?: signatures,
            newPluginRegistry ?: pluginRegistry,
            newSerialisation ?: serialisation
    )

    override fun copy(newArgs: GurrenArgs?, newCore: SpiralCoreConfig?, newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?, newSignatures: SpiralSignatures?, newPluginRegistry: SpiralPluginRegistry?, newSerialisation: SpiralSerialisation?): SpiralCockpitContext = DefaultSpiralCockpitContext(
            newArgs ?: args,
            newCore ?: core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newEventBus ?: eventBus,
            newSignatures ?: signatures,
            newPluginRegistry ?: pluginRegistry,
            newSerialisation ?: serialisation
    )

    init {
        moduleLoader.iterator().forEach { module -> module.register(this) }
        config.prime(this)

        this.storeStaticValue(SpiralEnvironment.SPIRAL_MODULE_KEY, SPIRAL_CORE_MODULE)
        this.storeStaticValue(SPIRAL_ENV_MODULES_KEY, this.loadedModules.entries.joinToString { (moduleName, moduleVersion) -> "$moduleName v$moduleVersion" })

        this.storeDynamicValue(SPIRAL_ENV_BUILD_KEY) {
            if (this !is SpiralCoreContext) {
                return@storeDynamicValue null
            }
            val version = retrieveStaticValue(SpiralEnvironment.SPIRAL_MD5_KEY) ?: return@storeDynamicValue null
            return@storeDynamicValue buildForVersion(this, version).toString()
        }

        val file = File(DefaultSpiralCoreContext::class.java.protectionDomain.codeSource.location.path)
        if (file.isFile) {
            JarFile(file).use { jar ->
                jar.manifest.mainAttributes.forEach { key, value ->
                    storeStaticValue("manifest.${key.toString().toLowerCase()}", value.toString())
                }
            }
        }

        this.loadPlugin(PluginEntry(SpiralCorePlugin(this).pojo, null))
    }
}