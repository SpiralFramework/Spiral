package info.spiralframework.formats.common.scripting.lin

inline class ChangeUIEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    constructor(element: Int, state: Int): this(intArrayOf(element, state))

    override val opcode: Int
        get() = 0x25

    val element: Int
        get() = rawArguments[0]

    val state: Int
        get() = rawArguments[1]

    override fun format(): String = "Change UI|$element, $state"
}