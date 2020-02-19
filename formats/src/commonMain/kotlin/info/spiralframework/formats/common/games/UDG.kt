package info.spiralframework.formats.common.games


import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.data.buildScriptOpcodes
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import info.spiralframework.formats.common.withFormats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
class UDG(
        override val linCharacterIDs: Map<Int, String>,
        override val linCharacterIdentifiers: Map<String, Int>,
        override val linColourCodes: Map<String, Int>,
        override val linItemNames: Array<String>,
        customOpcodes: List<JsonOpcode>
) : DrGame, DrGame.LinScriptable, DrGame.ScriptOpcodeFactory<IntArray, LinEntry> {
    companion object {
        @Serializable
        data class UDGGameJson(val character_ids: Map<Int, String>, val character_identifiers: Map<String, Int>, val colour_codes: Map<String, Int>, val item_names: Array<String>)

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext): UDG? {
            try {
                return unsafe(context)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.game.invalid", iae) }
                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext): UDG {
            withFormats(context) {
                //                if (isCachedShortTerm("games/udg.json"))
                val gameSource = requireNotNull(loadResource("games/udg.json", UDG::class))
                val gameJson = Json.parse(UDGGameJson.serializer(), requireNotNull(gameSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())

                val customOpcodeSource = loadResource("opcodes/udg.json", UDG::class)
                val customOpcodes: List<JsonOpcode>
                if (customOpcodeSource != null) {
                    customOpcodes = Json.parse(JsonOpcode.serializer().list, requireNotNull(customOpcodeSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())
                } else {
                    customOpcodes = emptyList()
                }

                return UDG(gameJson.character_ids, gameJson.character_identifiers, gameJson.colour_codes, gameJson.item_names, customOpcodes)
            }
        }
    }

    override val names: Array<String> = arrayOf("UDG", "Danganronpa Another Episode", "Danganronpa Another Episode: Ultra Despair Girls", "Danganronpa: Ultra Despair Girls", "Ultra Despair Girls")
    override val identifier: String = "udg"
    override val steamID: String = "555950"

    override val linOpcodeMap: OpcodeMap<IntArray, LinEntry> = buildScriptOpcodes {
        opcode(0x00, argumentCount = 2, name = "Text Count")
        opcode(0x01, argumentCount = 2, name = "Text")
        opcode(0x05, argumentCount = 3, name = "Movie")
        opcode(0x07, argumentCount = 5, name = "Voice Line")
        opcode(0x08, argumentCount = 3, names = arrayOf("Music, BGM"))
        opcode(0x0C, argumentCount = 0, name = "Wait for Input")

        opcode(0x12, argumentCount = 5, name = "Sprite")
        opcode(0x13, argumentCount = 7, name = "Screen Flash")
        opcode(0x15, argumentCount = 1, name = "Speaker")
        opcode(0x18, argumentCount = 2, name = "Fade Out")
        opcode(0x1B, argumentCount = 2, name = "Fade In")

        fromList(customOpcodes)
    }

    override val linBgmNames: Array<String> = emptyArray()
    override val linEvidenceNames: Array<String> = emptyArray()
    override val linMapNames: Array<String> = emptyArray()
    override val linMovieNames: Array<String> = emptyArray()
    override val linSkillNames: Array<String> = emptyArray()

    override fun entryFor(opcode: Int, rawArguments: IntArray): LinEntry = when (opcode) {
        else -> UnknownLinEntry(opcode, rawArguments)
    }

    override fun getLinVoiceFileID(character: Int, originalChapter: Int, voiceID: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLinVoiceLineDetails(voiceID: Int): Triple<Int, Int, Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.UDG() = UDG(this)
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.UnsafeUDG() = UDG.unsafe(this)