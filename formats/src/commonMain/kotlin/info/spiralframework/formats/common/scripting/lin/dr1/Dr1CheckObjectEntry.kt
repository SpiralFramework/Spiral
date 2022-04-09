package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1CheckObjectEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(objectID: Int): this(intArrayOf(objectID))

    override val opcode: Int
        get() = 0x29

    public var objectID: Int
        get() = get(0)
        set(value) = set(0, value)
}