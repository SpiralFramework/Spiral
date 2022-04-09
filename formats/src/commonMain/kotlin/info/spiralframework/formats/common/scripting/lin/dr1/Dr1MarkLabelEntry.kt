package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1MarkLabelEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(id: Int) : this(intArrayOf((id shr 8) and 0xFF, (id shr 0) and 0xFF))

    override val opcode: Int
        get() = 0x2A

    public var id: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)
}