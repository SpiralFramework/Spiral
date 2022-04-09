package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1BranchEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(branchValue: Int): this(intArrayOf(branchValue))
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x2B

    public var branchValue: Int
        get() = get(0)
        set(value) = set(0, value)
}