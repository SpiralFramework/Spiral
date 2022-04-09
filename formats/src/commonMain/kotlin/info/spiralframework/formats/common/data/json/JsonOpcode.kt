package info.spiralframework.formats.common.data.json

import kotlinx.serialization.Serializable

@Serializable
public data class JsonOpcode(
    val opcode: String,
    val argCount: Int,
    val name: String? = null,
    val names: Array<out String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JsonOpcode

        if (opcode != other.opcode) return false
        if (argCount != other.argCount) return false
        if (name != other.name) return false
        if (names != null) {
            if (other.names == null) return false
            if (!names.contentEquals(other.names)) return false
        } else if (other.names != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = opcode.hashCode()
        result = 31 * result + argCount
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (names?.contentHashCode() ?: 0)
        return result
    }
}