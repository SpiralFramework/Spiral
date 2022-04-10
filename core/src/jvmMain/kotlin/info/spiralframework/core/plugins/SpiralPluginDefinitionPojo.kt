package info.spiralframework.core.plugins

import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.core.common.serialisation.SemVerSerialiser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class SpiralPluginDefinitionPojo(
    val name: String,
    val uid: String,

    val description: String? = null,

    val authors: List<String>? = null,

    @SerialName("supported_modules")
    val supportedModules: List<String>? = null,

    @SerialName("required_modules")
    val requiredModules: List<String>? = null,

    @SerialName("content_warnings")
    val contentWarnings: List<String>? = null,

    val version: String? = null,

    @SerialName("semantic_version")
    @Serializable(SemVerSerialiser::class)
    val semanticVersion: SemanticVersion,

    @SerialName("plugin_class")
    val pluginClass: String,

    @SerialName("plugin_file_name")
    val pluginFileName: String? = null
) {
//    class Builder {
//        lateinit var name: String
//        lateinit var uid: String
//
//        var description: String? = null
//
//        var authors: Array<String>? = null
//        var supportedModules: Array<String>? = null
//        var requiredModules: Array<String>? = null
//        var contentWarnings: Array<String>? = null
//
//        var version: String? = null
////        @JsonProperty("semanticVersion")
//        @SerialName("semantic_version")
//        var semanticVersion: SemanticVersion? = null
//
//        lateinit var pluginClass: String
//
//        val pluginFileName: String? = null
//
//        fun build(): SpiralPluginDefinitionPojo =
//            SpiralPluginDefinitionPojo(name, uid, description, authors, supportedModules, requiredModules, contentWarnings, version, requireNotNull(semanticVersion), pluginClass, pluginFileName)
//    }
}