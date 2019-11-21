package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1SetStudentReportInfoEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x10

    val characterID: Int
        get() = rawArguments[0]

    val arg2: Int
        get() = rawArguments[1]

    val state: Int
        get() = rawArguments[2]

    override fun format(): String = "Set Report Info|$characterID, $arg2, $state"
}