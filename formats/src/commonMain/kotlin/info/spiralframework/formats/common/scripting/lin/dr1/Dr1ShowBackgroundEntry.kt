package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1ShowBackgroundEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x30

    val backgroundID: Int
        get() = rawArguments[0]

    val state: Int
        get() = rawArguments[1]

    override fun format(): String = "Show Background|$backgroundID, $state"
}