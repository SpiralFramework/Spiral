package info.spiralframework.formats.game.hpa

import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.formats.scripting.lin.UnknownEntry
import info.spiralframework.formats.utils.*
import java.io.File
import java.util.*

object UnknownHopesPeakGame: HopesPeakKillingGame {
    override val pakNames: Map<String, Array<String>> = emptyMap()
    override val opCodes: OpCodeMap<IntArray, LinScript> =
                OpCodeHashMap<IntArray, LinScript>().apply {
                    val opCodes = File("unk-ops.json")

                    if (opCodes.exists()) {

                        DataHandler.fileToMap(opCodes)?.forEach { opName, params ->
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

    override val names: Array<String> = arrayOf("Unknown")
    override val identifier: String = "unk"
    override val colourCodes: Map<String, String> = emptyMap()
    override val steamID: String? = null
    override val itemNames: Array<String> = emptyArray()
    override val nonstopDebateOpCodeNames: Map<Int, String> = emptyMap()
    override val nonstopDebateSectionSize: Int = 0
    override val trialCameraNames: Array<String> = emptyArray()
    override val evidenceNames: Array<String> = emptyArray()
}