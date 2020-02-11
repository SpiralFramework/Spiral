package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1ShowBackgroundEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(backgroundID: Int, state: Int): this(intArrayOf(backgroundID, state))

    override val opcode: Int
        get() = 0x30

    var backgroundID: Int
        get() = get(0)
        set(value) = set(0, value)

    var state: Int
        get() = get(1)
        set(value) = set(1, value)
}