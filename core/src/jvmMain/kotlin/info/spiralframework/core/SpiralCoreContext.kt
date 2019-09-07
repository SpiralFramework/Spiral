package info.spiralframework.core

import com.fasterxml.jackson.databind.ObjectMapper
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.core.security.SpiralSignatures

interface SpiralCoreContext: SpiralContext, SpiralSignatures {
    val updateConnectTimeout: Int
    val updateReadTimeout: Int
    val networkConnectTimeout: Int
    val networkReadTimeout: Int

    val apiBase: String
    val jenkinsBase: String

    val enabledPlugins: Map<String, SemanticVersion>
    val loadedModules: Map<String, SemanticVersion>

    val jsonMapper: ObjectMapper
    val yamlMapper: ObjectMapper
    val xmlMapper: ObjectMapper

    fun copy(newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newSignatures: SpiralSignatures? = null): SpiralContext
}