package info.spiralframework.formats.common.scripting.wrd

import info.spiralframework.formats.common.games.DrGame

public data class UnknownWrdEntry(
    override val opcode: Int,
    override val arguments: Array<WordScriptValue>,
    val wrdGame: DrGame.WordScriptable?
) : WrdEntry {
    public constructor(opcode: Int, rawArguments: Array<WordScriptValue>) : this(
        opcode,
        rawArguments,
        DrGame.WordScriptable.Unknown
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownWrdEntry) return false

        if (opcode != other.opcode) return false
        if (!arguments.contentEquals(other.arguments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = opcode
        result = 31 * result + arguments.contentHashCode()
        return result
    }

    override fun format(): String =
        "${
            wrdGame?.wrdOpcodeMap?.get(opcode)?.names?.firstOrNull()
                ?: "0x${opcode.toString(16).padStart(2, '0').uppercase()}"
        }|${arguments.joinToString()}"
}