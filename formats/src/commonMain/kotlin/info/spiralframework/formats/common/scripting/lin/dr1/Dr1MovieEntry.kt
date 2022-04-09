package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1MovieEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    public constructor(id: Int): this(intArrayOf(id))

    override val opcode: Int
        get() = 0x05

    public var movieID: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)
}