package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1LoadScriptEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(chapter: Int, scene: Int, room: Int): this(intArrayOf(chapter, scene, room))

    var chapter: Int
        get() = get(0)
        set(value) = set(0, value)

    var scene: Int
        get() = get(1)
        set(value) = set(1, value)

    var room: Int
        get() = get(2)
        set(value) = set(2, value)

    override val opcode: Int
        get() = 0x19
}