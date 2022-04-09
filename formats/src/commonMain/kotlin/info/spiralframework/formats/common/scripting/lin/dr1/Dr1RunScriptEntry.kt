package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1RunScriptEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(chapter: Int, scene: Int, room: Int) : this(intArrayOf(chapter, scene, room))

    public var chapter: Int
        get() = get(0)
        set(value) = set(0, value)

    public var scene: Int
        get() = get(1)
        set(value) = set(1, value)

    public var room: Int
        get() = get(2)
        set(value) = set(2, value)

    override val opcode: Int
        get() = 0x1B
}