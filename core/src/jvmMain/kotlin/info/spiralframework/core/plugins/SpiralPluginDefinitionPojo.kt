package info.spiralframework.core.plugins

import com.fasterxml.jackson.annotation.JsonProperty
import dev.brella.kornea.toolkit.common.SemanticVersion
import dev.brella.kornea.toolkit.common.oneTimeMutable

data class SpiralPluginDefinitionPojo(
    val name: String,
    val uid: String,

    val description: String?,

    val authors: Array<String>?,
    val supportedModules: Array<String>?,
    val requiredModules: Array<String>?,
    val contentWarnings: Array<String>?,

    val version: String?,
    val semanticVersion: SemanticVersion,

    val pluginClass: String,

    val pluginFileName: String?
) {
    class Builder {
        lateinit var name: String
        lateinit var uid: String

        var description: String? = null

        var authors: Array<String>? = null
        var supportedModules: Array<String>? = null
        var requiredModules: Array<String>? = null
        var contentWarnings: Array<String>? = null

        var version: String? = null
        @JsonProperty("semanticVersion")
        var semanticVersion: SemanticVersion? = null

        lateinit var pluginClass: String

        val pluginFileName: String? = null

        fun build(): SpiralPluginDefinitionPojo =
            SpiralPluginDefinitionPojo(name, uid, description, authors, supportedModules, requiredModules, contentWarnings, version, requireNotNull(semanticVersion), pluginClass, pluginFileName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpiralPluginDefinitionPojo) return false

        if (name != other.name) return false
        if (uid != other.uid) return false
        if (description != other.description) return false
        if (authors != null) {
            if (other.authors == null) return false
            if (!authors.contentEquals(other.authors)) return false
        } else if (other.authors != null) return false
        if (supportedModules != null) {
            if (other.supportedModules == null) return false
            if (!supportedModules.contentEquals(other.supportedModules)) return false
        } else if (other.supportedModules != null) return false
        if (requiredModules != null) {
            if (other.requiredModules == null) return false
            if (!requiredModules.contentEquals(other.requiredModules)) return false
        } else if (other.requiredModules != null) return false
        if (contentWarnings != null) {
            if (other.contentWarnings == null) return false
            if (!contentWarnings.contentEquals(other.contentWarnings)) return false
        } else if (other.contentWarnings != null) return false
        if (version != other.version) return false
        if (semanticVersion != other.semanticVersion) return false
        if (pluginClass != other.pluginClass) return false
        if (pluginFileName != other.pluginFileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (authors?.contentHashCode() ?: 0)
        result = 31 * result + (supportedModules?.contentHashCode() ?: 0)
        result = 31 * result + (requiredModules?.contentHashCode() ?: 0)
        result = 31 * result + (contentWarnings?.contentHashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + semanticVersion.hashCode()
        result = 31 * result + pluginClass.hashCode()
        result = 31 * result + (pluginFileName?.hashCode() ?: 0)
        return result
    }
}