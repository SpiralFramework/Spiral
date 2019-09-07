package info.spiralframework.formats.game.hpa

import info.spiralframework.base.common.text.toIntBaseN
import info.spiralframework.formats.common.data.JsonOpCode
import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.formats.scripting.lin.TextCountEntry
import info.spiralframework.formats.scripting.lin.UnknownEntry
import info.spiralframework.formats.scripting.lin.udg.UDGTextEntry
import info.spiralframework.formats.utils.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

object UDG: HopesPeakDRGame {
    override val pakNames: Map<String, List<String>> = emptyMap() //Probably never gonna get this huh

    @UnstableDefault
    override val opCodes: OpCodeMap<IntArray, LinScript> =
            OpCodeHashMap<IntArray, LinScript>().apply {
                this[0x00] = "Text Count" to 2 and ::TextCountEntry
                this[0x01] = "Text" to 2 and ::UDGTextEntry
                this[0x05] = "Movie" to 3 and ::UnknownEntry
                this[0x07] = "Voice Line" to 5 and ::UnknownEntry
                this[0x08] = arrayOf("Music", "BGM") to 3 and ::UnknownEntry
                this[0x0C] = "Wait For Input" to 0 and ::UnknownEntry
                this[0x12] = "Sprite" to 5 and ::UnknownEntry
                this[0x13] = "Screen Flash" to 7 and ::UnknownEntry
                this[0x15] = "Speaker" to 1 and ::UnknownEntry

                this[0x18] = "Fade Out" to 2 and ::UnknownEntry
                this[0x1B] = "Fade In" to 2 and ::UnknownEntry
//                this[0x30] = "Set Flag" to 8 and ::UnknownEntry

                val opCodes = File("udg-ops.json")

                if (opCodes.exists()) {
                    Json.parse((String.serializer() to JsonOpCode.serializer()).map, opCodes.readText())
                            .forEach { (name, op) ->
                                this[op.opcode.toIntBaseN()] = name to op.argCount and ::UnknownEntry
                            }
                }
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
    override val identifier: String = "udg"

    //TODO: Find clt codes
    override val colourCodes: Map<String, String> =
            mapOf()
    override val steamID: String = "555950"

    @UnstableDefault
    @ExperimentalStdlibApi
    override val itemNames: Array<String> = SharedHPA.itemNames.udg
}