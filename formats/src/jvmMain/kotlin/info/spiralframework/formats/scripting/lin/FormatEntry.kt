package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class FormatEntry(val format: Int): LinEntry {
    constructor(op: Int, args: IntArray): this(args[0])

    override val opcode: Int = 0x03
    override val rawArguments: IntArray = intArrayOf(format)

    override fun format(): String = "Format|$format"
}