package info.spiralframework.formats.common.scripting.lin

data class UnknownLinEntry(override val opcode: Int, override val rawArguments: IntArray): LinEntry {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownLinEntry) return false

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