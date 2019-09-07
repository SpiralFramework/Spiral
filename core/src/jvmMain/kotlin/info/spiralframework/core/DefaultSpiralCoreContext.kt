package info.spiralframework.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MD5_KEY
import info.spiralframework.base.common.environment.SpiralEnvironment.Companion.SPIRAL_MODULE_KEY
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.core.common.SPIRAL_CORE_MODULE
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import info.spiralframework.core.common.SPIRAL_ENV_MODULES_KEY
import info.spiralframework.core.plugins.PluginEntry
import info.spiralframework.core.plugins.SpiralCorePlugin
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.core.serialisation.InstantSerialisation
import info.spiralframework.core.serialisation.SemanticVersionSerialisation
import java.io.File
import java.util.*
import java.util.jar.JarFile

open class DefaultSpiralCoreContext(val core: SpiralCoreConfig, val locale: SpiralLocale, val logger: SpiralLogger, val config: SpiralConfig, val environment: SpiralEnvironment, val signatures: SpiralSignatures, val pluginRegistry: SpiralPluginRegistry)
    : SpiralCoreContext,
        SpiralLocale by locale,
        SpiralLogger by logger,
        SpiralConfig by config,
        SpiralEnvironment by environment,
        SpiralSignatures by signatures,
        SpiralPluginRegistry by pluginRegistry {
    companion object {
        const val DEFAULT_UPDATE_CONNECT_TIMEOUT = 5000
        const val DEFAULT_UPDATE_READ_TIMEOUT = 5000

        const val DEFAULT_NETWORK_CONNECT_TIMEOUT = 5000
        const val DEFAULT_NETWORK_READ_TIMEOUT = 5000

        const val DEFAULT_API_BASE = "https://api.abimon.org/api"
        const val DEFAULT_JENKINS_BASE = "https://jenkins.abimon.org"

        val DEFAULT_ENABLED_PLUGINS = emptyMap<String, SemanticVersion>()
    }

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

    /** Jackson mapper for JSON data */
    override val jsonMapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), SemanticVersionSerialisation.MODULE())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    /** Jackson mapper for YAML data */
    override val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), SemanticVersionSerialisation.MODULE())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    /** Jackson mapper for XML data */
    override val xmlMapper: ObjectMapper = XmlMapper(JacksonXmlModule().apply { setDefaultUseWrapper(false) })
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), InstantSerialisation.MODULE())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY))

    override fun subcontext(module: String): SpiralCoreContext = this

    override fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?): SpiralCoreContext = DefaultSpiralCoreContext(
            core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            signatures,
            pluginRegistry
    )

    override fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newSignatures: SpiralSignatures?, newPluginRegistry: SpiralPluginRegistry?): SpiralContext = DefaultSpiralCoreContext(
            core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newSignatures ?: signatures,
            newPluginRegistry ?: pluginRegistry
    )

    fun copy(newCore: SpiralCoreConfig? = null, newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newSignatures: SpiralSignatures? = null, newPluginRegistry: SpiralPluginRegistry? = null): SpiralCoreContext = DefaultSpiralCoreContext(
            newCore ?: core,
            newLocale ?: locale,
            newLogger ?: logger,
            newConfig ?: config,
            newEnvironment ?: environment,
            newSignatures ?: signatures,
            newPluginRegistry ?: pluginRegistry
    )

    init {
        storeStaticValue(SPIRAL_MODULE_KEY, SPIRAL_CORE_MODULE)
        storeStaticValue(SPIRAL_ENV_MODULES_KEY, loadedModules.entries.joinToString { (moduleName, moduleVersion) -> "$moduleName v$moduleVersion" })

        storeDynamicValue(SPIRAL_ENV_BUILD_KEY) {
            if (this !is SpiralCoreContext) {
                return@storeDynamicValue null
            }
            val version = retrieveStaticValue(SPIRAL_MD5_KEY) ?: return@storeDynamicValue null
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

        loadPlugin(PluginEntry(SpiralCorePlugin(this).pojo, null))
    }
}