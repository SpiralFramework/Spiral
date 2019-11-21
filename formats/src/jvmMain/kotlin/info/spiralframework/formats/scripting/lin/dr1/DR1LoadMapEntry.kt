package info.spiralframework.formats.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class DR1LoadMapEntry(val room: Int, val state: Int, val padding: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opcode: Int = 0x15
    override val rawArguments: IntArray = intArrayOf(room, state, padding)

    override fun format(): String = "Load Map|$room, $state, $padding"
}