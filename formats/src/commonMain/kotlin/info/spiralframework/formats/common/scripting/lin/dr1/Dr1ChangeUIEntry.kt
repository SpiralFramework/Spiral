package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1ChangeUIEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    constructor(element: Int, state: Int): this(intArrayOf(element, state))

    override val opcode: Int
        get() = 0x25

    var element: Int
        get() = rawArguments[0]
        set(value) = set(0, value)

    var state: Int
        get() = rawArguments[1]
        set(value) = set(1, value)

    override fun format(): String = "Change UI|$element, $state"
}