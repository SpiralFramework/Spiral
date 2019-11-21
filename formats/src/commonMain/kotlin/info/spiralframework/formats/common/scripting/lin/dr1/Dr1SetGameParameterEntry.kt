package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1SetGameParameterEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x33

    val variable: Int
        get() = rawArguments[0]

    val operation: Int
        get() = rawArguments[1]

    val value: Int
        get() = (rawArguments[2] shl 8) or rawArguments[3]

    override fun format(): String = "Set Game Parameter|$variable, $operation, ${rawArguments[2]}, ${rawArguments[3]}"
}