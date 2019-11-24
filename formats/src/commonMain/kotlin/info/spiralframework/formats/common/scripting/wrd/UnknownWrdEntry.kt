package info.spiralframework.formats.common.scripting.wrd

data class UnknownWrdEntry(override val opcode: Int, override val rawArguments: IntArray): WrdEntry {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownWrdEntry) return false

        if (opcode != other.opcode) return false
        if (!rawArguments.contentEquals(other.rawArguments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = opcode
        result = 31 * result + rawArguments.contentHashCode()
        return result
    }
}