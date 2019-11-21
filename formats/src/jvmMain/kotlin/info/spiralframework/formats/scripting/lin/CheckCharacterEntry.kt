package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class CheckCharacterEntry(val characterID: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opcode: Int = 0x27
    override val rawArguments: IntArray = intArrayOf(characterID)

    override fun format(): String = "Check Character|$characterID"
}