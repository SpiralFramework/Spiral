package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1LoadMapEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(room: Int, state: Int, arg3: Int): this(intArrayOf(room, state, arg3))

    override val opcode: Int
        get() = 0x15

    var room: Int
        get() = get(0)
        set(value) = set(0, value)

    var state: Int
        get() = get(1)
        set(value) = set(1, value)

    var arg3: Int
        get() = get(2)
        set(value) = set(2, value)

    override fun format(): String = "Load Map|$room, $state, $arg3"
}