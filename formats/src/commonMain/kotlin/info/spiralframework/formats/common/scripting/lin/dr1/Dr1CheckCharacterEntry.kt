package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1CheckCharacterEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)

    override val opcode: Int
        get() = 0x27

    val characterID: Int
        get() = rawArguments[0]

    override fun format(): String = "Check Character|$characterID"
}