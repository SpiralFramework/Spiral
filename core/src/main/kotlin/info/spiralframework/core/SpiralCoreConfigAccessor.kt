package info.spiralframework.core

import info.spiralframework.base.config.SpiralConfig
import info.spiralframework.base.util.SemVer

interface SpiralCoreConfigAccessor {
    companion object {
        val DEFAULT_CONFIG: SpiralCoreConfig? by cacheNullableYaml(SpiralConfig.getConfigFile("core"))

        const val DEFAULT_UPDATE_CONNECT_TIMEOUT = 5000
        const val DEFAULT_UPDATE_READ_TIMEOUT = 5000

        const val DEFAULT_NETWORK_CONNECT_TIMEOUT = 5000
        const val DEFAULT_NETWORK_READ_TIMEOUT = 5000

        const val DEFAULT_API_BASE = "https://api.abimon.org/api"
        const val DEFAULT_JENKINS_BASE = "https://jenkins.abimon.org"

        val DEFAULT_ENABLED_PLUGINS = emptyMap<String, SemVer>()
    }

    val config: SpiralCoreConfig?
        get() = DEFAULT_CONFIG

    val updateConnectTimeout: Int
        get() = config?.updateConnectTimeout ?: DEFAULT_UPDATE_CONNECT_TIMEOUT

    val updateReadTimeout: Int
        get() = config?.updateReadTimeout ?: DEFAULT_UPDATE_READ_TIMEOUT

    val networkConnectTimeout: Int
        get() = config?.networkConnectTimeout ?: DEFAULT_NETWORK_CONNECT_TIMEOUT

    val networkReadTimeout: Int
        get() = config?.networkReadTimeout ?: DEFAULT_NETWORK_READ_TIMEOUT

    val apiBase: String
        get() = config?.apiBase ?: DEFAULT_API_BASE

    val jenkinsBase: String
        get() = config?.jenkinsBase ?: DEFAULT_JENKINS_BASE

    val enabledPlugins: Map<String, SemVer>
        get() = config?.enabledPlugins ?: DEFAULT_ENABLED_PLUGINS
}