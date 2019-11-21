package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry
import java.util.*

data class UnknownEntry(override val opcode: Int, override val rawArguments: IntArray): LinEntry {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownEntry

        if (opcode != other.opcode) return false
        if (!Arrays.equals(rawArguments, other.rawArguments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = opcode
        result = 31 * result + Arrays.hashCode(rawArguments)
        return result
    }
}