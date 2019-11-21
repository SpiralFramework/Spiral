package info.spiralframework.formats.common.scripting.lin

inline class EndFlagCheckEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x3C

    override fun format(): String = "End Flag Check|"
}