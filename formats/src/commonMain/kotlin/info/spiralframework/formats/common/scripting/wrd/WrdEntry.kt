package info.spiralframework.formats.common.scripting.wrd

import info.spiralframework.formats.common.data.EnumWordScriptCommand

interface WrdEntry {
    val opcode: Int
    val rawArguments: IntArray
    val rawArgumentTypes: Array<EnumWordScriptCommand>

    /**
     * This should be an interpretable statement in something like OSL
     */
    fun format(): String = "0x${opcode.toString(16).padStart(2, '0').toUpperCase()}|${rawArguments.joinToString()}"
}