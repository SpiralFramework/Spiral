package info.spiralframework.formats.common.games

import info.spiralframework.formats.common.OpcodeCommandTypeMap
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.data.EnumWordScriptCommand
import info.spiralframework.formats.common.get
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1SpeakerEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1TextEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1WaitForInputEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1WaitFrameEntry
import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry

/**
 * The Danganronpa Games all share similar properties, which can be accessed here
 * This is only used as a form of abstraction.
 */
interface DrGame {
    val names: Array<String>
    val identifier: String
        get() = names.firstOrNull() ?: "none"

    val steamID: String?

    /** Traits */

    interface ScriptOpcodeFactory<P, S> {
        fun entryFor(opcode: Int, rawArguments: P): S
    }

    /** A game that supports lin scripts */
    interface LinScriptable {
        object Unknown : LinScriptable {
            override val linOpcodeMap: OpcodeMap<IntArray, LinEntry> = emptyMap()
            override val linCharacterIdentifiers: Map<String, Int> = emptyMap()
            override val linCharacterIDs: Map<Int, String> = emptyMap()
            override val linBgmNames: Array<String> = emptyArray()
            override val linItemNames: Array<String> = emptyArray()
            override val linEvidenceNames: Array<String> = emptyArray()
            override val linMapNames: Array<String> = emptyArray()
            override val linMovieNames: Array<String> = emptyArray()
            override val linSkillNames: Array<String> = emptyArray()
            override val linColourCodes: Map<String, Int> = emptyMap()
            override fun getVoiceFileID(character: Int, originalChapter: Int, voiceID: Int): Int = -1
            override fun getVoiceLineDetails(voiceID: Int): Triple<Int, Int, Int> = Triple(-1, -1, -1)
        }

        val linOpcodeMap: OpcodeMap<IntArray, LinEntry>

        /** Name -> Internal ID */
        val linCharacterIdentifiers: Map<String, Int>

        /** Internal ID -> Name */
        val linCharacterIDs: Map<Int, String>

        val linBgmNames: Array<String>
        val linItemNames: Array<String>
        val linEvidenceNames: Array<String>
        val linSkillNames: Array<String>
        val linMapNames: Array<String>
        val linMovieNames: Array<String>

        /** A map of the colour to the internal clt number */
        val linColourCodes: Map<String, Int>

        fun getVoiceFileID(character: Int, originalChapter: Int, voiceID: Int): Int
        fun getVoiceLineDetails(voiceID: Int): Triple<Int, Int, Int>

        fun getNameOfGameParameter(parameter: Int): String? = null
        fun getNameOfGameParameterValue(parameter: Int, value: Int): String? = null
    }

    /** TODO: Figure out how to do this full stop */
    interface LinNonstopScriptable {
        val linNonstopOpcodeNames: OpcodeMap<IntArray, String>
        val linNonstopSectionSize: Int
    }

    /** TODO: Figure out how to do this for V3 */
    interface LinTrialSupported {
        val trialCameraNames: Array<String>
        val evidenceNames: Array<String>
    }

    /** A game that supports word scripts */
    interface WordScriptable {
        object Unknown : WordScriptable {
            override val wrdOpcodeMap: OpcodeMap<Array<WordScriptValue>, WrdEntry> = emptyMap()
            override val wrdOpcodeCommandType: OpcodeCommandTypeMap<EnumWordScriptCommand> = emptyMap()
            override val wrdCharacterIdentifiers: Map<String, String> = emptyMap()
            override val wrdCharacterNames: Map<String, String> = emptyMap()
            override val wrdItemNames: Array<String> = emptyArray()
            override val wrdColourCodes: Map<String, String> = emptyMap()
        }

        val wrdOpcodeMap: OpcodeMap<Array<WordScriptValue>, WrdEntry>

        val wrdOpcodeCommandType: OpcodeCommandTypeMap<EnumWordScriptCommand>

        /** Name -> Internal ID */
        val wrdCharacterIdentifiers: Map<String, String>

        /** Internal ID -> Name */
        val wrdCharacterNames: Map<String, String>

        val wrdItemNames: Array<String>

        /** A map of the colour to the internal clt name */
        val wrdColourCodes: Map<String, String>
    }

    /** A game that has subfiles stored within pak archives. */
    interface PakMapped {
        val pakNames: Map<String, Array<String>>
    }
}

@ExperimentalUnsignedTypes
fun DrGame.LinScriptable.SpeakerEntry(speaker: Int): LinEntry? =
        when (this) {
            is Dr1 -> Dr1SpeakerEntry(speaker)
            else -> linOpcodeMap["Speaker"]?.let { opcode -> opcode.entryConstructor(opcode.opcode, intArrayOf(speaker)) }
        }

@ExperimentalUnsignedTypes
fun DrGame.LinScriptable.TextEntry(textID: Int): LinEntry? =
        when (this) {
            is Dr1 -> Dr1TextEntry(textID)
            else -> linOpcodeMap["Text"]?.let { opcode -> opcode.entryConstructor(opcode.opcode, intArrayOf(textID)) }
        }

@ExperimentalUnsignedTypes
fun DrGame.LinScriptable.WaitFrame(): LinEntry? =
        when (this) {
            is Dr1 -> Dr1WaitFrameEntry()
            else -> linOpcodeMap["Wait Frame"]?.let { opcode -> opcode.entryConstructor(opcode.opcode, LinEntry.EMPTY_ARGUMENT_ARRAY) }
        }

@ExperimentalUnsignedTypes
fun DrGame.LinScriptable.WaitForInput(): LinEntry? =
        when (this) {
            is Dr1 -> Dr1WaitForInputEntry()
            else -> linOpcodeMap["Wait for Input"]?.let { opcode -> opcode.entryConstructor(opcode.opcode, LinEntry.EMPTY_ARGUMENT_ARRAY) }
        }