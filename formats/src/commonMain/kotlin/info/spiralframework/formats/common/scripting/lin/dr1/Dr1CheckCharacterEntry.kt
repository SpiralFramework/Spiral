package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1CheckCharacterEntry(override val rawArguments: IntArray): MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    constructor(characterID: Int): this(intArrayOf(characterID))

    override val opcode: Int
        get() = 0x27

    var characterID: Int
        get() = rawArguments[0]
        set(value) = set(0, value)

    override fun format(): String = "Check Character|$characterID"
}