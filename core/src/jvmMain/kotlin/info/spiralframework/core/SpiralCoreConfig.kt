package info.spiralframework.core

import com.fasterxml.jackson.annotation.JsonProperty
import dev.brella.kornea.toolkit.common.SemanticVersion

open class SpiralCoreConfig(
        @JsonProperty("update_connect_timeout") val updateConnectTimeout: Int? = null,
        @JsonProperty("update_read_timeout") val updateReadTimeout: Int? = null,
        @JsonProperty("network_connect_timeout") val networkConnectTimeout: Int? = null,
        @JsonProperty("network_read_Timeout") val networkReadTimeout: Int? = null,
        @JsonProperty("api_base") val apiBase: String? = null,
        @JsonProperty("jenkins_base") val jenkinsBase: String? = null,

        @JsonProperty("enabled_plugins") val enabledPlugins: Map<String, SemanticVersion>? = null
) {
    constructor(context: SpiralCoreContext) : this(
            context.updateConnectTimeout,
            context.updateReadTimeout,
            context.networkConnectTimeout,
            context.networkReadTimeout,
            context.apiBase,
            context.jenkinsBase,
            context.enabledPlugins
    )
}