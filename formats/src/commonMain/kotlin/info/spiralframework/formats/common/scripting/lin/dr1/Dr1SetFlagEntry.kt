package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1SetFlagEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(flagGroup: Int, flagID: Int, state: Int) : this(intArrayOf(flagGroup, flagID, state))
    constructor(flagGroup: Int, flagID: Int, enabled: Boolean) : this(intArrayOf(flagGroup, flagID, if (enabled) 1 else 0))

    override val opcode: Int
        get() = 0x26

    var flagGroup: Int
        get() = get(0)
        set(value) = set(0, value)

    var flagID: Int
        get() = get(1)
        set(value) = set(1, value)

    var state: Int
        get() = get(2)
        set(value) = set(2, value)

    var enabled: Boolean
        get() = get(2) > 0
        set(value) = set(2, if (value) 1 else 0)
}