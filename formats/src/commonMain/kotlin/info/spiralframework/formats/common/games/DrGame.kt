package info.spiralframework.formats.common.games

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.mutableMapOfAll
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.defaultEquals
import info.spiralframework.base.common.properties.defaultHashCode
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
public interface DrGame {
    public companion object : ISpiralProperty.PropertyKey<DrGame>,
        KorneaTypeChecker<DrGame> by KorneaTypeChecker.ClassBased() {
        override val name: String = "DrGame"

        public val NAMES: Array<String> =
            arrayOf(Dr1.PRIMARY_NAME, Dr2.PRIMARY_NAME, UDG.PRIMARY_NAME, DRv3.PRIMARY_NAME)

        public val VALUES: MutableMap<String, suspend (context: SpiralContext) -> KorneaResult<DrGame>> =
            mutableMapOfAll(
                Dr1.NAMES to SpiralContext::Dr1,
                Dr2.NAMES to SpiralContext::Dr2,
                UDG.NAMES to SpiralContext::UDG,
                DRv3.NAMES to SpiralContext::DRv3
            )

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }

    public val names: Array<String>
    public val identifier: String
        get() = names.firstOrNull() ?: "none"

    public val steamID: String?

    /** Traits */

    public interface UnknownGame : DrGame {
        override val names: Array<String>
            get() = arrayOf("Unknown")

        override val identifier: String
            get() = "Unknown"

        override val steamID: String?
            get() = null
    }

    public interface ScriptOpcodeFactory<P, S> {
        public fun entryFor(opcode: Int, rawArguments: P): S
    }

    /** A game that supports lin scripts */
    public interface LinScriptable : DrGame {
        public companion object : ISpiralProperty.PropertyKey<LinScriptable>,
            KorneaTypeChecker<LinScriptable> by KorneaTypeChecker.ClassBased() {
            override val name: String = "LinScriptable"

            public val NAMES: Array<String> = arrayOf(Dr1.PRIMARY_NAME, Dr2.PRIMARY_NAME, UDG.PRIMARY_NAME)

            public val VALUES: MutableMap<String, suspend (context: SpiralContext) -> KorneaResult<LinScriptable>> =
                mutableMapOfAll(
                    Dr1.NAMES to SpiralContext::Dr1,
                    Dr2.NAMES to SpiralContext::Dr2,
                    UDG.NAMES to SpiralContext::UDG
                )

            override fun hashCode(): Int = defaultHashCode()
            override fun equals(other: Any?): Boolean = defaultEquals(other)
        }

        public object Unknown : LinScriptable, UnknownGame {
            override val linOpcodeMap: OpcodeMap<IntArray, LinEntry> = emptyMap()
            override val linCharacterIdentifiers: Map<String, Int> = emptyMap()
            override val linCharacterIDs: Map<Int, String> = emptyMap()
            override val linBgmNames: List<String> = emptyList()
            override val linItemNames: List<String> = emptyList()
            override val linEvidenceNames: List<String> = emptyList()
            override val linMapNames: List<String> = emptyList()
            override val linMovieNames: List<String> = emptyList()
            override val linSkillNames: List<String> = emptyList()
            override val linColourCodes: Map<String, Int> = emptyMap()
            override fun getLinVoiceFileID(character: Int, originalChapter: Int, voiceID: Int): Int = -1
            override fun getLinVoiceLineDetails(voiceID: Int): Triple<Int, Int, Int> = Triple(-1, -1, -1)
        }

        public val linOpcodeMap: OpcodeMap<IntArray, LinEntry>

        /** Name -> Internal ID */
        public val linCharacterIdentifiers: Map<String, Int>

        /** Internal ID -> Name */
        public val linCharacterIDs: Map<Int, String>

        public val linBgmNames: List<String>
        public val linItemNames: List<String>
        public val linEvidenceNames: List<String>
        public val linSkillNames: List<String>
        public val linMapNames: List<String>
        public val linMovieNames: List<String>

        /** A map of the colour to the internal clt number */
        public val linColourCodes: Map<String, Int>

        public fun getLinVoiceFileID(character: Int, originalChapter: Int, voiceID: Int): Int
        public fun getLinVoiceLineDetails(voiceID: Int): Triple<Int, Int, Int>?

        public fun getNameOfLinGameParameter(parameter: Int): String? = null
        public fun getNameOfLinGameParameterValue(parameter: Int, value: Int): String? = null

        public fun getNameOfLinUIElement(element: Int): String? = null

        public fun getLinFlagName(flagGroup: Int, flagID: Int): String? = null

        public fun formLinOpcode(name: String, args: IntArray): LinEntry? =
            linOpcodeMap[name]?.let { opcode -> opcode.entryConstructor(opcode.opcode, args) }
    }

    /** TODO: Figure out how to do this full stop */
    public interface LinNonstopScriptable : DrGame {
        public companion object : ISpiralProperty.PropertyKey<LinNonstopScriptable>,
            KorneaTypeChecker<LinNonstopScriptable> by KorneaTypeChecker.ClassBased() {
            override val name: String = "LinNonstopScriptable"

            override fun hashCode(): Int = defaultHashCode()
            override fun equals(other: Any?): Boolean = defaultEquals(other)
        }

        public val linNonstopOpcodeNames: OpcodeMap<IntArray, String>
        public val linNonstopSectionSize: Int
    }

    /** TODO: Figure out how to do this for V3 */
    public interface LinTrialSupported {
        public companion object : ISpiralProperty.PropertyKey<LinTrialSupported>,
            KorneaTypeChecker<LinTrialSupported> by KorneaTypeChecker.ClassBased() {
            override val name: String = "LinTrialSupported"

            override fun hashCode(): Int = defaultHashCode()
            override fun equals(other: Any?): Boolean = defaultEquals(other)
        }


        public val trialCameraNames: Array<String>
        public val evidenceNames: Array<String>
    }

    /** A game that supports word scripts */
    public interface WordScriptable : DrGame {
        public companion object : ISpiralProperty.PropertyKey<WordScriptable>,
            KorneaTypeChecker<WordScriptable> by KorneaTypeChecker.ClassBased() {
            override val name: String = "WordScriptable"

            override fun hashCode(): Int = defaultHashCode()
            override fun equals(other: Any?): Boolean = defaultEquals(other)
        }

        public object Unknown : WordScriptable, UnknownGame {
            override val wrdOpcodeMap: OpcodeMap<Array<WordScriptValue>, WrdEntry> = emptyMap()
            override val wrdOpcodeCommandType: OpcodeCommandTypeMap<EnumWordScriptCommand> = emptyMap()
            override val wrdCharacterIdentifiers: Map<String, String> = emptyMap()
            override val wrdCharacterNames: Map<String, String> = emptyMap()
            override val wrdItemNames: List<String> = emptyList()
            override val wrdColourCodes: Map<String, String> = emptyMap()
        }

        public val wrdOpcodeMap: OpcodeMap<Array<WordScriptValue>, WrdEntry>

        public val wrdOpcodeCommandType: OpcodeCommandTypeMap<EnumWordScriptCommand>

        /** Name -> Internal ID */
        public val wrdCharacterIdentifiers: Map<String, String>

        /** Internal ID -> Name */
        public val wrdCharacterNames: Map<String, String>

        public val wrdItemNames: List<String>

        /** A map of the colour to the internal clt name */
        public val wrdColourCodes: Map<String, String>
    }

    /** A game that has subfiles stored within pak archives. */
    public interface PakMapped {
        public companion object : ISpiralProperty.PropertyKey<PakMapped>,
            KorneaTypeChecker<PakMapped> by KorneaTypeChecker.ClassBased() {
            override val name: String = "PakMapped"

            override fun hashCode(): Int = defaultHashCode()
            override fun equals(other: Any?): Boolean = defaultEquals(other)
        }

        public val pakNames: Map<String, List<String>>
    }
}

@Suppress("FunctionName")
public fun DrGame.LinScriptable.SpeakerEntry(speaker: Int): LinEntry? =
    when (this) {
        is Dr1 -> Dr1SpeakerEntry(speaker)
        else -> formLinOpcode("Speaker", intArrayOf(speaker))
    }

@Suppress("FunctionName")
public fun DrGame.LinScriptable.TextEntry(textID: Int): LinEntry? =
    when (this) {
        is Dr1 -> Dr1TextEntry(textID)
        else -> formLinOpcode("Text", intArrayOf(textID))
    }

@Suppress("FunctionName")
public fun DrGame.LinScriptable.WaitFrame(): LinEntry? =
    when (this) {
        is Dr1 -> Dr1WaitFrameEntry()
        else -> formLinOpcode("Wait Frame", LinEntry.EMPTY_ARGUMENT_ARRAY)
    }

@Suppress("FunctionName")
public fun DrGame.LinScriptable.WaitForInput(): LinEntry? =
    when (this) {
        is Dr1 -> Dr1WaitForInputEntry()
        else -> formLinOpcode("Wait for Input", LinEntry.EMPTY_ARGUMENT_ARRAY)
    }