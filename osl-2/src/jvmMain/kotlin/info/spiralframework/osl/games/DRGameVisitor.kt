package info.spiralframework.osl.games

import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.osbc.common.OSLUnion

interface DRGameVisitor {
    companion object {
        fun visitorFor(game: DrGame): DRGameVisitor? {
            return when (game) {
//                is HopesPeakDRGame -> HopesPeakVisitor(game)
//                is V3 -> V3Visitor(game)
                else -> null
            }
        }
    }

    fun handleScriptLine(line: OSLUnion)

    fun entryForName(name: String, arguments: IntArray): OSLUnion
    fun entryForOpCode(opCode: Int, arguments: IntArray): OSLUnion
    fun handleArgumentForEntry(arguments: MutableList<Int>, argument: OSLUnion)

    fun clearCltCode(builder: StringBuilder): Boolean
    fun handleCltCode(builder: StringBuilder, code: String): Boolean
    fun closeCltCode(builder: StringBuilder)

    fun scriptResult(): OSLUnion
}