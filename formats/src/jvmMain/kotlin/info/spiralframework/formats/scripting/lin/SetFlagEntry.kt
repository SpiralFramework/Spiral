package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class SetFlagEntry(val group: Int, val id: Int, val state: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opcode: Int = 0x26
    override val rawArguments: IntArray = intArrayOf(group, id, state)

    override fun format(): String = "Set Flag|$group, $id, $state"
}