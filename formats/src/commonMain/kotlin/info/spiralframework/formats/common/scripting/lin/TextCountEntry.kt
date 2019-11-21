package info.spiralframework.formats.common.scripting.lin

inline class TextCountEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x00

    val linesOfText: Int
        get() = rawArguments[0] or (rawArguments[1] shl 8)

    override fun format(): String = "Text Count|$linesOfText"
}