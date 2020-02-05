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
open class Dr2(
        override val linCharacterIDs: Map<Int, String>,
        override val linCharacterIdentifiers: Map<String, Int>,
        override val linColourCodes: Map<String, Int>,
        override val linItemNames: Array<String>,
        override val pakNames: Map<String, Array<String>>,
        customOpcodes: List<JsonOpcode>
) : DrGame, DrGame.LinScriptable, DrGame.PakMapped, DrGame.ScriptOpcodeFactory<IntArray, LinEntry>, DrGame.LinNonstopScriptable {
    companion object {
        const val NONSTOP_DEBATE_SECTION_SIZE = 34

        @Serializable
        data class Dr2GameJson(val character_ids: Map<Int, String>, val character_identifiers: Map<String, Int>, val colour_codes: Map<String, Int>, val item_names: Array<String>, val pak_names: Map<String, Array<String>>)

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext): Dr2? {
            try {
                return unsafe(context)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.game.invalid", iae) }
                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext): Dr2 {
            withFormats(context) {
                //                if (isCachedShortTerm("games/dr2.json"))
                val gameSource = requireNotNull(loadResource("games/dr2.json", Dr2::class))
                val gameJson = Json.parse(Dr2GameJson.serializer(), requireNotNull(gameSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())

                val customOpcodeSource = loadResource("opcodes/dr2.json", Dr2::class)
                val customOpcodes: List<JsonOpcode>
                if (customOpcodeSource != null) {
                    customOpcodes = Json.parse(JsonOpcode.serializer().list, requireNotNull(customOpcodeSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())
                } else {
                    customOpcodes = emptyList()
                }

                return Dr2(gameJson.character_ids, gameJson.character_identifiers, gameJson.colour_codes, gameJson.item_names, gameJson.pak_names, customOpcodes)
            }
        }
    }

    override val names: Array<String> = arrayOf("DR2", "SDR2", "Danganronpa 2", "Danganronpa 2: Goodbye Despair")
    override val steamID: String = "413420"
    override val identifier: String = "dr2"
    override val linOpcodeMap: OpcodeMap<IntArray, LinEntry> = buildScriptOpcodes {
        opcode(0x00, argumentCount = 2, name = "Text Count")
        opcode(0x02, argumentCount = 2, name = "Text")
        opcode(0x05, argumentCount = 2, name = "Movie")
        opcode(0x08, argumentCount = 5, name = "Voice Line")
        opcode(0x09, argumentCount = 3, names = arrayOf("Music", "BGM"))

        opcode(0x14, argumentCount = 6, name = "Trial Camera")
        opcode(0x15, argumentCount = 4, name = "Load Map")
        opcode(0x1E, argumentCount = 5, name = "Sprite")

        opcode(0x4B, argumentCount = 0, name = "Wait for Input")
        opcode(0x4C, argumentCount = 0, name = "Wait Frame")

        /**
         * this[0x00] = "Text Count" to 2 and ::TextCountEntry
        this[0x01] = null to 4 and ::UnknownEntry
        this[0x02] = "Text" to 2 and ::TextEntry
        this[0x03] = "Format" to 1 and ::FormatEntry
        this[0x04] = "Filter" to 4 and ::FilterEntry
        this[0x05] = "Movie" to 2 and ::MovieEntry
        this[0x06] = "Animation" to 8 and ::AnimationEntry
        this[0x07] = null to -1 and ::UnknownEntry
        this[0x08] = "Voice Line" to 5 and ::VoiceLineEntry
        this[0x09] = arrayOf("Music", "BGM") to 3 and ::UnknownEntry
        this[0x0A] = "SFX A" to 3 and ::SoundEffectAEntry
        this[0x0B] = "SFX B" to 2 and ::SoundEffectBEntry
        this[0x0C] = "Truth Bullet" to 2 and ::TruthBulletEntry
        this[0x0D] = null to 3 and ::UnknownEntry
        this[0x0E] = null to 2 and ::UnknownEntry
        this[0x0F] = "Set Title" to 3 and ::SetStudentTitleEntry
        this[0x10] = "Set Report Info" to 3 and ::SetStudentReportInfo
        this[0x11] = null to 4 and ::UnknownEntry
        this[0x12] = null to -1 and ::UnknownEntry
        this[0x13] = null to -1 and ::UnknownEntry
        this[0x14] = "Trial Camera" to 6 and ::DR2TrialCameraEntry
        this[0x15] = "Load Map" to 4 and ::DR2LoadMapEntry
        this[0x16] = null to -1 and ::UnknownEntry
        this[0x17] = null to -1 and ::UnknownEntry
        this[0x18] = null to -1 and ::UnknownEntry
        this[0x19] = arrayOf("Script", "Load Script") to 5 and ::DR2LoadScriptEntry
        this[0x1A] = arrayOf("Stop Script", "End Script") to 0 and DR2::StopScriptEntry
        this[0x1B] = "Run Script" to 5 and ::DR2RunScriptEntry
        this[0x1C] = null to 0 and ::UnknownEntry
        this[0x1D] = null to -1 and ::UnknownEntry
        this[0x1E] = "Sprite" to 5 and ::SpriteEntry
        this[0x1F] = "Screen Flash" to 7 and ::ScreenFlashEntry
        this[0x20] = null to 5 and ::UnknownEntry
        this[0x21] = "Speaker" to 1 and ::SpeakerEntry
        this[0x22] = "Screen Fade" to 3 and ::ScreenFadeEntry
        this[0x23] = null to 5 and ::UnknownEntry
        this[0x24] = null to -1 and ::UnknownEntry
        this[0x25] = "Change UI" to 2 and ::ChangeUIEntry
        this[0x26] = "Set Flag" to 3 and ::SetFlagEntry
        this[0x27] = null to -1 and ::CheckCharacterEntry //Was: Check Character
        this[0x28] = null to -1 and ::UnknownEntry
        this[0x29] = null to -1 and ::CheckObjectEntry //Was: Check Object
        this[0x2A] = null to -1 and ::UnknownEntry //Was: Set Label; I was LIED to; something with Twilight Syndrome?
        this[0x2B] = "Choice" to 1 and ::ChoiceEntry
        this[0x2C] = "Set Label" to 2 and ::SetLabelEntry
        this[0x2D] = null to -1 and ::UnknownEntry
        this[0x2E] = null to 5 and ::UnknownEntry
        this[0x2F] = null to 10 and ::UnknownEntry
        this[0x30] = "Show Background" to 3 and ::ShowBackgroundEntry
        this[0x31] = null to -1 and ::UnknownEntry
        this[0x32] = null to 1 and ::UnknownEntry
        this[0x33] = null to 4 and ::UnknownEntry
        this[0x34] = null to -1 and ::UnknownEntry //Was: Go To Label
        this[0x35] = "Check Flag A" to -1 and ::CheckFlagAEntry
        this[0x36] = "Check Flag B" to -1 and ::UnknownEntry
        this[0x37] = null to -1 and ::UnknownEntry
        this[0x38] = null to -1 and ::UnknownEntry
        this[0x39] = null to 5 and ::UnknownEntry
        this[0x3A] = "Set Game Parameter" to 4 and ::UnknownEntry
        this[0x3B] = arrayOf("Go To Label", "Goto Label", "Goto") to 2 and ::GoToLabelEntry
        this[0x3C] = "End Flag Check" to 0 and DR2::EndFlagCheckEntry
        this[0x4B] = "Wait For Input" to 0 and ::WaitForInputEntry
        this[0x4C] = "Wait Frame" to 0 and ::WaitFrameEntry
         */

        fromList(customOpcodes)
    }

    override fun entryFor(opcode: Int, rawArguments: IntArray): LinEntry = when (opcode) {
        else -> UnknownLinEntry(opcode, rawArguments)
    }

    override fun getVoiceFileID(character: Int, originalChapter: Int, voiceID: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVoiceLineDetails(voiceID: Int): Triple<Int, Int, Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val linNonstopOpcodeNames: OpcodeMap<IntArray, String> = buildScriptOpcodes {  }
    override val linNonstopSectionSize: Int = NONSTOP_DEBATE_SECTION_SIZE
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr2(): Dr2? = Dr2(this)

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr2(): Dr2 = Dr2.unsafe(this)