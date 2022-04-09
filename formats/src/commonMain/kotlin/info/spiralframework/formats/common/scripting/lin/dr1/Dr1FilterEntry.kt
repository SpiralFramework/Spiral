package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1FilterEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(arg1: Int, filter: Int, arg3: Int, arg4: Int): this(intArrayOf(arg1, filter, arg3, arg4))

    override val opcode: Int
        get() = 0x04

    public var arg1: Int
        get() = get(0)
        set(value) = set(0, value)

    public var filter: Int
        get() = get(1)
        set(value) = set(1, value)

    public var arg3: Int
        get() = get(2)
        set(value) = set(2, value)

    public var arg4: Int
        get() = get(3)
        set(value) = set(3, value)
}