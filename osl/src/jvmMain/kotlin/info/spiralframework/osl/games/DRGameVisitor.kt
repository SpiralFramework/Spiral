package info.spiralframework.osl.games

import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.osb.common.OSLUnion

public interface DRGameVisitor {
    public companion object {
        public fun visitorFor(game: DrGame): DRGameVisitor? {
            return when (game) {
//                is HopesPeakDRGame -> HopesPeakVisitor(game)
//                is V3 -> V3Visitor(game)
                else -> null
            }
        }
    }

    public fun handleScriptLine(line: OSLUnion)

    public fun entryForName(name: String, arguments: IntArray): OSLUnion
    public fun entryForOpCode(opCode: Int, arguments: IntArray): OSLUnion
    public fun handleArgumentForEntry(arguments: MutableList<Int>, argument: OSLUnion)

    public fun clearCltCode(builder: StringBuilder): Boolean
    public fun handleCltCode(builder: StringBuilder, code: String): Boolean
    public fun closeCltCode(builder: StringBuilder)

    public fun scriptResult(): OSLUnion
}