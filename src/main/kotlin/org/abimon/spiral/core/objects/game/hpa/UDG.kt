package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.TextCountEntry
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
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
                this[0x00] = "Text Count" to 2 and ::TextCountEntry
                this[0x01] = "Text" to 2 and ::UDGTextEntry
                this[0x05] = "Movie" to 3 and ::UnknownEntry
                this[0x07] = "Voice Line" to 5 and ::UnknownEntry
                this[0x08] = arrayOf("Music", "BGM") to 3 and ::UnknownEntry
                this[0x12] = "Sprite" to 5 and ::UnknownEntry
                this[0x13] = "Screen Flash" to 7 and ::UnknownEntry
                this[0x15] = "Speaker" to 1 and ::UnknownEntry

                this[0x18] = "Fade Out" to 2 and ::UnknownEntry
                this[0x1B] = "Fade In" to 2 and ::UnknownEntry
//                this[0x30] = "Set Flag" to 8 and ::UnknownEntry

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

    override val steamID: String = "555950"
}