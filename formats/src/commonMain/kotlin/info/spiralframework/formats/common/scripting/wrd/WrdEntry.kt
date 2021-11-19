package info.spiralframework.formats.common.scripting.wrd

interface WrdEntry {
    val opcode: Int
    val arguments: Array<WordScriptValue>

    /**
     * This should be an interpretable statement in something like OSL
     */
    fun format(): String = "0x${opcode.toString(16).padStart(2, '0').uppercase()}|${arguments.joinToString()}"
}