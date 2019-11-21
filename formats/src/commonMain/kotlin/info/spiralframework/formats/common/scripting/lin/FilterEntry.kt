package info.spiralframework.formats.common.scripting.lin

inline class FilterEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x04

    val arg1: Int
        get() = rawArguments[0]

    val filter: Int
        get() = rawArguments[1]

    val arg3: Int
        get() = rawArguments[2]

    val arg4: Int
        get() = rawArguments[3]

    override fun format(): String = "Filter|$arg1, $filter, $arg3, $arg4"
}