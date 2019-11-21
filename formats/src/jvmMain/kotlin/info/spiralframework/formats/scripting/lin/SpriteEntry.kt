package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class SpriteEntry(val positionID: Int, val characterID: Int, val spriteID: Int, val spriteState: Int, val spriteEntryTransition: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2], args[3], args[4])

    override val opcode: Int = 0x1E
    override val rawArguments: IntArray = intArrayOf(positionID, characterID, spriteID, spriteState, spriteEntryTransition)

    override fun format(): String = "Display $characterID, pose $spriteID, position $positionID, state $spriteState, transition $spriteEntryTransition"
}