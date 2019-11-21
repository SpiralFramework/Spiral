package info.spiralframework.formats.common.scripting.lin

inline class SetStudentTitleEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x0F

    val characterID: Int
        get() = rawArguments[0]

    val arg2: Int
        get() = rawArguments[1]

    val state: Int
        get() = rawArguments[2]

    override fun format(): String = "Set Title|$characterID, $arg2, $state"
}