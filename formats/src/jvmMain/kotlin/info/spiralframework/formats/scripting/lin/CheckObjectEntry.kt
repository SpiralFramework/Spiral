package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class CheckObjectEntry(val objectID: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opcode: Int = 0x29
    override val rawArguments: IntArray = intArrayOf(objectID)

    override fun format(): String = "Check Object|$objectID"
}