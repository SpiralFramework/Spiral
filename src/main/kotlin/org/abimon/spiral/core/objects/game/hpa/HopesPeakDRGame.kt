package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.utils.OpCodeMap
import java.util.*

/**
 * The Hope's Peak arc of games.
 * Each of them have different values, but similar (where applicable) types of values.
 * This helps bind things like Op Codes and PAK names to a general interface
 */
interface HopesPeakDRGame: DRGame {
    val pakNames: Map<String, Array<String>>
    val opCodes: OpCodeMap<IntArray, LinScript>

    val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray>

    val characterIdentifiers: MutableMap<String, Int>
    val characterIDs: Map<Int, String>
}