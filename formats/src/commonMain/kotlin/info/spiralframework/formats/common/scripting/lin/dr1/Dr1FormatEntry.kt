package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1FormatEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(formatValue: Int): this(intArrayOf(formatValue))

    override val opcode: Int
        get() = 0x03

    var formatValue: Int
        get() = rawArguments[0]
        set(value) = set(0, value)

    override fun format(): String = "Format|$formatValue"
}