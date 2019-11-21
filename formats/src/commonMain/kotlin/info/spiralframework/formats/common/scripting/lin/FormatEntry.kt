package info.spiralframework.formats.common.scripting.lin

inline class FormatEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x03

    val formatValue: Int
        get() = rawArguments[0]

    override fun format(): String = "Format|$formatValue"
}