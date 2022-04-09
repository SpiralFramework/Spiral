package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1CheckCharacterEntry(override val rawArguments: IntArray): MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    public constructor(characterID: Int): this(intArrayOf(characterID))

    override val opcode: Int
        get() = 0x27

    public var characterID: Int
        get() = rawArguments[0]
        set(value) = set(0, value)
}