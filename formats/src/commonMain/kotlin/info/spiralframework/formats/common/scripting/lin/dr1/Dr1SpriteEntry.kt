package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1SpriteEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(position: Int, characterID: Int, spriteID: Int, state: Int, transition: Int) : this(intArrayOf(position, characterID, spriteID, state, transition))

    override val opcode: Int
        get() = 0x1E

    var position: Int
        get() = get(0)
        set(value) = set(0, value)

    var characterID: Int
        get() = get(1)
        set(value) = set(1, value)

    var spriteID: Int
        get() = get(2)
        set(value) = set(2, value)

    var state: Int
        get() = get(3)
        set(value) = set(3, value)

    var transition: Int
        get() = get(4)
        set(value) = set(4, value)
}