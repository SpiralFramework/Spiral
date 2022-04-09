package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1FormatEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(formatValue: Int): this(intArrayOf(formatValue))

    override val opcode: Int
        get() = 0x03

    public var formatValue: Int
        get() = get(0)
        set(value) = set(0, value)
}