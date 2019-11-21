package info.spiralframework.formats.common.scripting.lin

inline class ShowBackgroundEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x30

    val backgroundID: Int
        get() = rawArguments[0]

    val state: Int
        get() = rawArguments[1]

    override fun format(): String = "Show Background|$backgroundID, $state"
}