package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1MovieEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    constructor(id: Int): this(intArrayOf(id))

    override val opcode: Int
        get() = 0x05

    var movieID: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)

    override fun format(): String = "Movie|${rawArguments[0]}, ${rawArguments[1]}"
}