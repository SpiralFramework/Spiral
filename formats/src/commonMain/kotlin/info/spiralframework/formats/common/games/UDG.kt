package info.spiralframework.formats.common.games

import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useAndMapInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.STEAM_DANGANRONPA_ANOTHER_EPISODE_ULTRA_DESPAIR_GIRLS
import info.spiralframework.formats.common.data.buildScriptOpcodes
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import info.spiralframework.formats.common.withFormats
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames

public class UDG(
    override val linCharacterIDs: Map<Int, String>,
    override val linCharacterIdentifiers: Map<String, Int>,
    override val linColourCodes: Map<String, Int>,
    override val linItemNames: List<String>,
    customOpcodes: List<JsonOpcode>
) : DrGame, DrGame.LinScriptable, DrGame.ScriptOpcodeFactory<IntArray, LinEntry> {
    public companion object {
        public const val IDENTIFIER: String = "udg"

        public val NAMES: Array<String> = arrayOf(
            "UDG",
            "Danganronpa Another Episode",
            "Danganronpa Another Episode: Ultra Despair Girls",
            "Danganronpa: Ultra Despair Girls",
            "Ultra Despair Girls"
        )

        public val PRIMARY_NAME: String
            get() = NAMES.first()

        @OptIn(ExperimentalSerializationApi::class)
        @Serializable
        public data class UDGGameJson(
            @JsonNames("character_ids", "characterIds")
            val characterIDs: Map<Int, String>,

            @JsonNames("character_identifiers")
            val characterIdentifiers: Map<String, Int>,

            @JsonNames("colour_codes", "color_codes", "colorCodes")
            val colourCodes: Map<String, Int>,

            @JsonNames("item_names")
            val itemNames: List<String>
        )

        public suspend operator fun invoke(context: SpiralContext): KorneaResult<UDG> {
            withFormats(context) {
                //                if (isCachedShortTerm("games/udg.json"))
                val gameString = loadResource("games/udg.json", UDG::class)
                    .useAndMapInputFlow { flow -> flow.readBytes().decodeToString() }
                    .getOrBreak { return it.asType() }

                val gameJson = Json.decodeFromString(UDGGameJson.serializer(), gameString)

                val customOpcodes: List<JsonOpcode> = loadResource("opcodes/udg.json", Dr1::class)
                    .useAndMapInputFlow { flow -> flow.readBytes().decodeToString() }
                    .map { str -> Json.decodeFromString(ListSerializer(JsonOpcode.serializer()), str) }
                    .getOrElse { emptyList() }

                return KorneaResult.success(
                    UDG(
                        gameJson.characterIDs,
                        gameJson.characterIdentifiers,
                        gameJson.colourCodes,
                        gameJson.itemNames,
                        customOpcodes
                    )
                )
            }
        }
    }

    override val names: Array<String> = NAMES
    override val identifier: String = IDENTIFIER
    override val steamID: String = STEAM_DANGANRONPA_ANOTHER_EPISODE_ULTRA_DESPAIR_GIRLS

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

    override val linBgmNames: List<String> = emptyList()
    override val linEvidenceNames: List<String> = emptyList()
    override val linMapNames: List<String> = emptyList()
    override val linMovieNames: List<String> = emptyList()
    override val linSkillNames: List<String> = emptyList()

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

@Suppress("FunctionName")
public suspend fun SpiralContext.UDG(): KorneaResult<UDG> = UDG(this)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeUDG(): UDG = UDG(this).getOrThrow()