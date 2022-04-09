package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1SpriteEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(position: Int, characterID: Int, spriteID: Int, state: Int, transition: Int) : this(intArrayOf(position, characterID, spriteID, state, transition))

    override val opcode: Int
        get() = 0x1E

    public var position: Int
        get() = get(0)
        set(value) = set(0, value)

    public var characterID: Int
        get() = get(1)
        set(value) = set(1, value)

    public var spriteID: Int
        get() = get(2)
        set(value) = set(2, value)

    public var state: Int
        get() = get(3)
        set(value) = set(3, value)

    public var transition: Int
        get() = get(4)
        set(value) = set(4, value)
}