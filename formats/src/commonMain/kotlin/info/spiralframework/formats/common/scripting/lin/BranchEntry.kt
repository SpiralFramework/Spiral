package info.spiralframework.formats.common.scripting.lin

inline class BranchEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x2B

    val branchValue: Int
        get() = rawArguments[0]

    override fun format(): String = "Branch|$branchValue"
}