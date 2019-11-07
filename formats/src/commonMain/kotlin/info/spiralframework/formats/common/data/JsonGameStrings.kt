package info.spiralframework.formats.common.data

import kotlinx.serialization.Serializable

@Serializable
data class JsonGameStrings(
        val dr1: Array<String> = emptyArray(),
        val dr2: Array<String> = emptyArray(),
        val v3: Array<String> = emptyArray(),
        val udg: Array<String> = emptyArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonGameStrings) return false

        if (!dr1.contentEquals(other.dr1)) return false
        if (!dr2.contentEquals(other.dr2)) return false
        if (!v3.contentEquals(other.v3)) return false
        if (!udg.contentEquals(other.udg)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dr1.contentHashCode()
        result = 31 * result + dr2.contentHashCode()
        result = 31 * result + v3.contentHashCode()
        result = 31 * result + udg.contentHashCode()
        return result
    }
}