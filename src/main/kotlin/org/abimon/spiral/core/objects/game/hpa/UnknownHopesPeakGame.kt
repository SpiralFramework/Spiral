package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.abimon.spiral.core.utils.*
import java.io.File
import java.util.*

object UnknownHopesPeakGame: HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> = emptyMap()
    override val opCodes: OpCodeMap<IntArray, LinScript> =
                OpCodeHashMap<IntArray, LinScript>().apply {
                    val opCodes = File("unk-ops.json")

                    if (opCodes.exists()) {

                        DataMapper.fileToMap(opCodes)?.forEach { opName, params ->
                            val array = ((params as? Array<*>)?.toList() ?: (params as? List<*>))?.mapNotNull { any ->
                                val str = any.toString()
                                if (str.startsWith("0x"))
                                    return@mapNotNull str.substring(2).toIntOrNull(16)
                                return@mapNotNull str.toIntOrNull()
                            } ?: return@forEach
                            this[array[0]] = opName to array[1] and ::UnknownEntry
                        }
                    }
                }
    override val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray> = emptyMap()
    override val characterIDs: Map<Int, String> = emptyMap()
    override val characterIdentifiers: MutableMap<String, Int> = HashMap<String, Int>()

    override val names: Array<String> =
            arrayOf(
                    "Unknown"
            )
    override val steamID: String? = null
}