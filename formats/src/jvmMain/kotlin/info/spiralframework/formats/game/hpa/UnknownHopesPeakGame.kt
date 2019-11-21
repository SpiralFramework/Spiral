package info.spiralframework.formats.game.hpa

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.utils.OpCodeHashMap
import info.spiralframework.formats.utils.OpCodeMap
import kotlinx.serialization.UnstableDefault
import java.io.File
import java.util.*

object UnknownHopesPeakGame : HopesPeakKillingGame {
    override val pakNames: Map<String, List<String>> = emptyMap()
    @UnstableDefault
    override val opCodes: OpCodeMap<IntArray, LinEntry> =
            OpCodeHashMap<IntArray, LinEntry>().apply {
                val opCodes = File("unk-ops.json")

//                if (opCodes.exists()) {
//                    Json.parse((String.serializer() to JsonOpCode.serializer()).map, opCodes.readText())
//                            .forEach { (name, op) ->
//                                this[op.opcode.toIntBaseN()] = name to op.argCount and ::UnknownEntry
//                            }
//                }
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