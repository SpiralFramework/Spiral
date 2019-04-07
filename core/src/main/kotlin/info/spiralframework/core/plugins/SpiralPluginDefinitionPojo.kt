package info.spiralframework.core.plugins

import info.spiralframework.base.util.SemVer

data class SpiralPluginDefinitionPojo(
        val name: String,
        val uid: String,

        val description: String?,

        val version: String?,
        val semanticVersion: SemVer,

        val pluginClass: String
)