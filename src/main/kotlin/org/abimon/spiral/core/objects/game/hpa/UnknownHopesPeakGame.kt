package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.utils.OpCodeMap
import java.util.*

object UnknownHopesPeakGame: HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> = emptyMap()
    override val opCodes: OpCodeMap<IntArray, LinScript> = emptyMap()
    override val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray> = emptyMap()
    override val characterIDs: Map<Int, String> = emptyMap()
    override val characterIdentifiers: MutableMap<String, Int> = HashMap<String, Int>()
}