package info.spiralframework.formats.common.scripting.lin

inline class CheckObjectEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x29

    val objectID: Int
        get() = rawArguments[0]

    override fun format(): String = "Check Object|$objectID"
}