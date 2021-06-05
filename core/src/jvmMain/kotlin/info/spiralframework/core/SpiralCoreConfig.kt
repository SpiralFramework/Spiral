package info.spiralframework.core

import dev.brella.kornea.toolkit.common.SemanticVersion

data class SpiralCoreConfig(
        val socketTimeout: Int? = null,
        val connectTimeout: Int? = null,
        val requestTimeout: Int? = null,
        val apiBase: String? = null,
        val jenkinsBase: String? = null,

        val enabledPlugins: Map<String, SemanticVersion>? = null
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