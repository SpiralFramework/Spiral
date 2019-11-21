package info.spiralframework.formats.common.scripting.lin

inline class TextEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(textID: Int): this(intArrayOf(textID shr 8, textID and 0xFF))
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)

    override val opcode: Int
        get() = 0x02

    val textID: Int
        get() = rawArguments[0] shl 8 or rawArguments[1]

    override fun format(): String = "Text|${rawArguments[0]}, ${rawArguments[1]}"
}