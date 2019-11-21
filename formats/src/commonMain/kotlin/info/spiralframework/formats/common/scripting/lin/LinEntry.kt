package info.spiralframework.formats.common.scripting.lin

interface LinEntry {
    val opcode: Int
    val rawArguments: IntArray

    /**
     * This should be an interpretable statement in something like OSL
     */
    fun format(): String = "0x${opcode.toString(16).padStart(2, '0').toUpperCase()}|${rawArguments.joinToString()}"
}