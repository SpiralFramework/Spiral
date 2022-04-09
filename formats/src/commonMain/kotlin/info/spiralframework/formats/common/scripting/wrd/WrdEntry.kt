package info.spiralframework.formats.common.scripting.wrd

public interface WrdEntry {
    public val opcode: Int
    public val arguments: Array<WordScriptValue>

    /**
     * This should be an interpretable statement in something like OSL
     */
    public fun format(): String = "0x${opcode.toString(16).padStart(2, '0').uppercase()}|${arguments.joinToString()}"
}