package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1TextCountEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(linesOfText: Int) : this(intArrayOf(linesOfText))

    override val opcode: Int
        get() = 0x00

    public var linesOfText: Int
        get() = getInt16LE(0)
        set(value) = setInt16LE(0, value)
}