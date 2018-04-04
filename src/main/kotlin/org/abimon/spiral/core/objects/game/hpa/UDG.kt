package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.udg.UDGTextEntry
import org.abimon.spiral.core.utils.OpCodeHashMap
import org.abimon.spiral.core.utils.OpCodeMap
import org.abimon.spiral.core.utils.and
import org.abimon.spiral.core.utils.set
import java.util.*

object UDG: HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> = emptyMap() //Probably never gonna get this huh
    override val opCodes: OpCodeMap<IntArray, LinScript> =
            OpCodeHashMap<IntArray, LinScript>().apply {
                this[0x01] = "Text" to 2 and ::UDGTextEntry
            }

    override val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray> =
            emptyMap()

    override val characterIDs: Map<Int, String> =
            emptyMap()

    override val characterIdentifiers: MutableMap<String, Int> =
            HashMap<String, Int>()

    override val names: Array<String> =
            arrayOf(
                    "UDG",
                    "Danganronpa Another Episode",
                    "Danganronpa Another Episode: Ultra Despair Girls",
                    "Danganronpa: Ultra Despair Girls",
                    "Ultra Despair Girls"
            )
}