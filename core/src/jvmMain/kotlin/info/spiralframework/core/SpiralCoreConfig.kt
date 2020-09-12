package info.spiralframework.core

import com.fasterxml.jackson.annotation.JsonProperty
import dev.brella.kornea.toolkit.common.SemanticVersion

open class SpiralCoreConfig(
        @JsonProperty("socket_timeout") val socketTimeout: Int? = null,
        @JsonProperty("connect_timeout") val connectTimeout: Int? = null,
        @JsonProperty("request_timeout") val requestTimeout: Int? = null,
        @JsonProperty("api_base") val apiBase: String? = null,
        @JsonProperty("jenkins_base") val jenkinsBase: String? = null,

        @JsonProperty("enabled_plugins") val enabledPlugins: Map<String, SemanticVersion>? = null
) {
    constructor(context: SpiralCoreContext) : this(
            context.socketTimeout,
            context.connectTimeout,
            context.requestTimeout,
            context.apiBase,
            context.jenkinsBase,
            context.enabledPlugins
    )
}