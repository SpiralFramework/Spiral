package info.spiralframework.formats.common.scripting.lin

inline class CheckCharacterEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)

    override val opcode: Int
        get() = 0x27

    val characterID: Int
        get() = rawArguments[0]

    override fun format(): String = "Check Character|$characterID"
}