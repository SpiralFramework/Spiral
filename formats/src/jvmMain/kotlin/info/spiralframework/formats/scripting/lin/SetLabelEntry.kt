package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.game.DRGame

data class SetLabelEntry(override val opcode: Int, val id: Int): LinEntry {
    companion object {
        fun DR1(labelID: Int): GoToLabelEntry = GoToLabelEntry(0x2A, labelID)
        fun DR2(labelID: Int): GoToLabelEntry = GoToLabelEntry(0x2C, labelID)
        fun forGame(game: DRGame?, labelID: Int): GoToLabelEntry =
                when (game) {
                    info.spiralframework.formats.game.hpa.DR1 -> DR1(labelID)
                    info.spiralframework.formats.game.hpa.DR2 -> DR2(labelID)
                    else -> TODO("SetLabelEntry is undocumented for $game")
                }
    }
    constructor(opCode: Int, args: IntArray): this(opCode,(args[1] shl 8) or args[0])

    override val rawArguments: IntArray = intArrayOf(id % 256, id shr 8)

    override fun format(): String = "Set Label|$id"
}