package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class ChangeUIEntry(val element: Int, val state: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1])

    override val opcode: Int = 0x25
    override val rawArguments: IntArray = intArrayOf(element, state)

    override fun format(): String = "Change UI|$element, $state"
}