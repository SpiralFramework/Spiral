package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class ChoiceEntry(val arg1: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opcode: Int = 0x2B
    override val rawArguments: IntArray = intArrayOf(arg1)

    override fun format(): String = "Choice|$arg1"
}