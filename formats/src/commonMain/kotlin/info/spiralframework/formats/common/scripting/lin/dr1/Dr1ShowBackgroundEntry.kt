package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1ShowBackgroundEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(backgroundID: Int, state: Int) : this(intArrayOf(backgroundID, state))

    override val opcode: Int
        get() = 0x30

    public var backgroundID: Int
        get() = get(0)
        set(value) = set(0, value)

    public var state: Int
        get() = get(1)
        set(value) = set(1, value)
}