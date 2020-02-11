package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1FilterEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(arg1: Int, filter: Int, arg3: Int, arg4: Int): this(intArrayOf(arg1, filter, arg3, arg4))

    override val opcode: Int
        get() = 0x04

    var arg1: Int
        get() = rawArguments[0]
        set(value) = set(0, value)

    var filter: Int
        get() = rawArguments[1]
        set(value) = set(1, value)

    var arg3: Int
        get() = rawArguments[2]
        set(value) = set(2, value)

    var arg4: Int
        get() = rawArguments[3]
        set(value) = set(3, value)
}