package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1LoadMapEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x15

    val room: Int
        get() = rawArguments[0]

    val state: Int
        get() = rawArguments[1]

    val arg3: Int
        get() = rawArguments[2]

    override fun format(): String = "Load Map|$room, $state, $arg3"
}