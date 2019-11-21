package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1MovieEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)

    override val opcode: Int
        get() = 0x05

    val movieID: Int
        get() = rawArguments[0] shl 8 or rawArguments[1]

    override fun format(): String = "Movie|${rawArguments[0]}, ${rawArguments[1]}"
}