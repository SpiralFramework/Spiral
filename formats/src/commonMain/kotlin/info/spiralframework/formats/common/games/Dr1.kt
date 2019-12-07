package info.spiralframework.formats.common.games

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.flow.readBytes
import info.spiralframework.base.common.io.useInputFlow
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.data.buildScriptOpcodes
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import info.spiralframework.formats.common.scripting.lin.dr1.*
import info.spiralframework.formats.common.withFormats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

@ExperimentalUnsignedTypes
open class Dr1(
        override val linCharacterIDs: Map<Int, String>,
        override val linCharacterIdentifiers: Map<String, Int>,
        override val linColourCodes: Map<String, Int>,
        override val linItemNames: Array<String>,
        override val pakNames: Map<String, Array<String>>,
        customOpcodes: List<JsonOpcode>
) : DrGame, DrGame.LinScriptable, DrGame.PakMapped, DrGame.ScriptOpcodeFactory<LinEntry> {
    companion object {
        @Serializable
        data class Dr1GameJson(val character_ids: Map<Int, String>, val character_identifiers: Map<String, Int>, val colour_codes: Map<String, Int>, val item_names: Array<String>, val pak_names: Map<String, Array<String>>)

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext): Dr1? {
            try {
                return unsafe(context)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.game.invalid", iae) }
                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext): Dr1 {
            withFormats(context) {
                //                if (isCachedShortTerm("games/dr1.json"))
                val gameSource = requireNotNull(loadResource("games/dr1.json", Dr1::class))
                val gameJson = Json.parse(Dr1GameJson.serializer(), requireNotNull(gameSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())

                val customOpcodeSource = loadResource("opcodes/dr1.json", Dr1::class)
                val customOpcodes: List<JsonOpcode>
                if (customOpcodeSource != null) {
                    customOpcodes = Json.parse(JsonOpcode.serializer().list, requireNotNull(customOpcodeSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())
                } else {
                    customOpcodes = emptyList()
                }

                return Dr1(gameJson.character_ids, gameJson.character_identifiers, gameJson.colour_codes, gameJson.item_names, gameJson.pak_names, customOpcodes)
            }
        }
    }

    override val names: Array<String> = arrayOf("DR1", "Danganronpa 1", "Danganronpa: Trigger Happy Havoc")
    override val steamID: String = "413410"
    override val identifier: String = "dr1"

    override val linOpcodeMap: OpcodeMap<LinEntry> = buildScriptOpcodes {
        opcode(0x00, argumentCount = 2, name = "Text Count")
        opcode(0x01, argumentCount = 3, names = null)
        opcode(0x02, argumentCount = 2, name = "Text")
        opcode(0x03, argumentCount = 1, name = "Format")
        opcode(0x04, argumentCount = 4, name = "Filter")
        opcode(0x05, argumentCount = 2, name = "Movie")
        opcode(0x06, argumentCount = 8, name = "Animation")
//            opcode(0x07, argumentCount = -1, names = null)
        opcode(0x08, argumentCount = 5, name = "Voice Line")
        opcode(0x09, argumentCount = 3, names = arrayOf("Music, BGM"), entryConstructor = ::UnknownLinEntry)
        opcode(0x0A, argumentCount = 3, name = "SFX A")
        opcode(0x0B, argumentCount = 2, name = "SFX B")
        opcode(0x0C, argumentCount = 2, name = "Truth Bullet")
        opcode(0x0D, argumentCount = 3, name = "Manage Item")
        opcode(0x0E, argumentCount = 2, names = null)
        opcode(0x0F, argumentCount = 3, name = "Set Title")

        opcode(0x10, argumentCount = 3, name = "Set Report Info")
        opcode(0x11, argumentCount = 4, names = null)
//            opcode(0x12, argumentCount = -1, names = null, entryConstructor = ::UnknownEntry)
//            opcode(0x13, argumentCount = -1, names = null, entryConstructor = ::UnknownEntry)
        opcode(0x14, argumentCount = 3, name = "Trial Camera")
        opcode(0x15, argumentCount = 3, name = "Load Map")
//            opcode(0x16, argumentCount = -1, names = null)
//            opcode(0x17, argumentCount = -1, names = null)
//            opcode(0x18, argumentCount = -1, names = null)
        opcode(0x19, argumentCount = 3, names = arrayOf("Script", "Load Script"))
        opcode(0x1A, argumentCount = 0, names = arrayOf("Stop Script", "End Script"))
        opcode(0x1B, argumentCount = 3, name = "Run Script")
        opcode(0x1C, argumentCount = 0, names = null)
//            opcode(0x1D, argumentCount = -1, names = null, entryConstructor = ::UnknownEntry)
        opcode(0x1E, argumentCount = 5, name = "Sprite")
        opcode(0x1F, argumentCount = 7, name = "Screen Flash")

        opcode(0x20, argumentCount = 5, names = null)
        opcode(0x21, argumentCount = 1, name = "Speaker")
        opcode(0x22, argumentCount = 3, name = "Screen Fade")
        opcode(0x23, argumentCount = 5, names = null)
//            opcode(0x24, argumentCount = -1, names = null)
        opcode(0x25, argumentCount = 2, name = "Change UI")
        opcode(0x26, argumentCount = 3, name = "Set Flag")
        opcode(0x27, argumentCount = 1, name = "Check Character")
//            opcode(0x28, argumentCount = -1, names = null, entryConstructor = ::UnknownEntry)
        opcode(0x29, argumentCount = 1, name = "Check Object")
        opcode(0x2A, argumentCount = 2, names = arrayOf("Mark Label", "Set Label"))
        opcode(0x2B, argumentCount = 1, names = arrayOf("Branch", "Choice"))
        opcode(0x2C, argumentCount = 2, names = null)
//            opcode(0x2D, argumentCount = -1, names = null, entryConstructor = ::UnknownEntry)
        opcode(0x2E, argumentCount = 2, names = null)
        opcode(0x2F, argumentCount = 10, names = null)

        opcode(0x30, argumentCount = 3, name = "Show Background")
//            opcode(0x31, argumentCount = -1, names = null, entryConstructor = ::UnknownEntry)
        opcode(0x32, argumentCount = 1, names = null)
        opcode(0x33, argumentCount = 4, name = "Set Game Parameter")
        opcode(0x34, argumentCount = 2, names = arrayOf("Go To Label", "Goto Label", "Goto"))

        flagCheck(0x35, names = arrayOf("Check Flag", "Check Flag A"), flagGroupLength = 4, endFlagCheckOpcode = 0x3C)
        flagCheck(0x36, names = arrayOf("Check Game Parameter", "Check Flag B"), flagGroupLength = 5, endFlagCheckOpcode = 0x3C)
//            flagCheck(0x37, names = null,                                               flagGroupLength = 5, endFlagCheckOpcode = 0x3C)
//            flagCheck(0x38, names = null,                                               flagGroupLength = 5, endFlagCheckOpcode = 0x3C)

        opcode(0x39, argumentCount = 5, names = null)
        opcode(0x3A, argumentCount = 0, name = "Wait for Input")
        opcode(0x3B, argumentCount = 0, name = "Wait Frame")
        opcode(0x3C, argumentCount = 0, name = "End Flag Check")

        fromList(customOpcodes)
    }

    override fun entryFor(opcode: Int, rawArguments: IntArray): LinEntry = when (opcode) {
        0x00 -> Dr1TextCountEntry(opcode, rawArguments)
        0x02 -> Dr1TextEntry(opcode, rawArguments)
        0x03 -> Dr1FormatEntry(opcode, rawArguments)
        0x04 -> Dr1FilterEntry(opcode, rawArguments)
        0x05 -> Dr1MovieEntry(opcode, rawArguments)
        0x06 -> Dr1AnimationEntry(opcode, rawArguments)
        0x08 -> Dr1VoiceLineEntry(opcode, rawArguments)
        0x0A -> Dr1SoundEffectAEntry(opcode, rawArguments)
        0x0B -> Dr1SoundEffectBEntry(opcode, rawArguments)
        0x0C -> Dr1TruthBulletEntry(opcode, rawArguments)
        0x0F -> Dr1SetStudentTitleEntry(opcode, rawArguments)

        0x10 -> Dr1SetStudentReportInfoEntry(opcode, rawArguments)
        0x14 -> Dr1TrialCameraEntry(opcode, rawArguments)
        0x15 -> Dr1LoadMapEntry(opcode, rawArguments)
        0x19 -> Dr1LoadScriptEntry(opcode, rawArguments)
        0x1A -> Dr1StopScriptEntry(opcode, rawArguments)
        0x1B -> Dr1RunScriptEntry(opcode, rawArguments)
        0x1E -> Dr1SpriteEntry(opcode, rawArguments)
        0x1F -> Dr1ScreenFlashEntry(opcode, rawArguments)

        0x21 -> Dr1SpeakerEntry(opcode, rawArguments)
        0x22 -> Dr1ScreenFadeEntry(opcode, rawArguments)
        0x25 -> Dr1ChangeUIEntry(opcode, rawArguments)
        0x26 -> Dr1SetFlagEntry(opcode, rawArguments)
        0x27 -> Dr1CheckCharacterEntry(opcode, rawArguments)
        0x29 -> Dr1CheckObjectEntry(opcode, rawArguments)
        0x2A -> Dr1MarkLabelEntry(opcode, rawArguments)
        0x2B -> Dr1BranchEntry(opcode, rawArguments)

        0x30 -> Dr1ShowBackgroundEntry(opcode, rawArguments)
        0x33 -> Dr1SetGameParameterEntry(opcode, rawArguments)
        0x34 -> Dr1GoToLabelEntry(opcode, rawArguments)

        0x35 -> Dr1CheckFlagEntry(opcode, rawArguments)
        0x36 -> Dr1CheckGameParameter(opcode, rawArguments)

        0x3A -> Dr1WaitForInputEntry(opcode, rawArguments)
        0x3B -> Dr1WaitFrameEntry(opcode, rawArguments)
        0x3C -> Dr1EndFlagCheckEntry(opcode, rawArguments)

        else -> UnknownLinEntry(opcode, rawArguments)
    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1(): Dr1? = Dr1(this)

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1(): Dr1 = Dr1.unsafe(this)