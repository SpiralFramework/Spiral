package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class FilterEntry(val arg1: Int, val filter: Int, val arg3: Int, val arg4: Int): LinEntry {
    constructor(filter: Int) : this(1, filter, 0, 0)
    constructor(op: Int, args: IntArray): this(args[0], args[1], args[2], args[3])

    override val opcode: Int = 0x04
    override val rawArguments: IntArray = intArrayOf(arg1, filter, arg3, arg4)

    override fun format(): String = "Filter|$arg1, $filter, $arg3, $arg4"
}