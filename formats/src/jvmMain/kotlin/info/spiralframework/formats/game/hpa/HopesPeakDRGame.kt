package info.spiralframework.formats.game.hpa

import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.utils.OpCodeMap
import java.util.*

/**
 * The Hope's Peak arc of games.
 * Each of them have different values, but similar (where applicable) types of values.
 * This helps bind things like Op Codes and PAK names to a general interface
 */
interface HopesPeakDRGame: DRGame {
    val pakNames: Map<String, List<String>>
    val opCodes: OpCodeMap<IntArray, LinEntry>

    val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray>

    val characterIdentifiers: Map<String, Int>
    val characterIDs: Map<Int, String>

    val itemNames: Array<String>
}