package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1BranchEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(branchValue: Int): this(intArrayOf(branchValue))
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x2B

    var branchValue: Int
        get() = rawArguments[0]
        set(value) = set(0, value)
}