package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.game.DRGame

data class GoToLabelEntry(override val opCode: Int, val labelID: Int): LinScript {
    companion object {
        fun DR1(labelID: Int): GoToLabelEntry = GoToLabelEntry(0x34, labelID)
        fun DR2(labelID: Int): GoToLabelEntry = GoToLabelEntry(0x3B, labelID)
        fun forGame(game: DRGame?, labelID: Int): GoToLabelEntry =
                when (game) {
                    info.spiralframework.formats.game.hpa.DR1 -> DR1(labelID)
                    info.spiralframework.formats.game.hpa.DR2 -> DR2(labelID)
                    else -> TODO("GoToLabelEntry is undocumented for $game")
                }
    }

    constructor(opCode: Int, args: IntArray): this(opCode, (args[1] shl 8) or args[0])

    override val rawArguments: IntArray = intArrayOf(labelID % 256, labelID shr 8)

    override fun format(): String = "Go To Label|${labelID % 256}, ${labelID shr 8}"
}