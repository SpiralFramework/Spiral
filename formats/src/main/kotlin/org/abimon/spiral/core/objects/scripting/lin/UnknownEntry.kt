package org.abimon.spiral.core.objects.scripting.lin

import java.util.*

data class UnknownEntry(override val opCode: Int, override val rawArguments: IntArray): LinScript {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownEntry

        if (opCode != other.opCode) return false
        if (!Arrays.equals(rawArguments, other.rawArguments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = opCode
        result = 31 * result + Arrays.hashCode(rawArguments)
        return result
    }
}