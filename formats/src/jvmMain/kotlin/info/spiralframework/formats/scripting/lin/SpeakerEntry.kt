package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class SpeakerEntry(val characterID: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opcode: Int = 0x21
    override val rawArguments: IntArray = intArrayOf(characterID)

    override fun format(): String = "Speaker|$characterID"
}