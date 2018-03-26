package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.*
import org.abimon.spiral.core.objects.scripting.lin.dr2.*
import org.abimon.spiral.core.utils.*
import java.util.*

object DR2 : HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> =
            DataMapper.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("pak/dr2.json"))?.mapValues { (_, value) ->
                ((value as? List<*>)?.asIterable()
                        ?: (value as? Array<*>)?.asIterable())?.mapNotNull { str -> str as? String }?.toTypedArray()
                        ?: emptyArray()
            } ?: emptyMap()

    fun StopScriptEntry(opCode: Int, args: IntArray): StopScriptEntry = org.abimon.spiral.core.objects.scripting.lin.StopScriptEntry()
    fun WaitForInputEntry(opCode: Int, args: IntArray): DR2WaitForInputEntry = DR2WaitForInputEntry()
    fun WaitFrameEntry(opCode: Int, args: IntArray): DR2WaitFrameEntry = DR2WaitFrameEntry()
    fun EndFlagCheckEntry(opCode: Int, args: IntArray): EndFlagCheckEntry = org.abimon.spiral.core.objects.scripting.lin.EndFlagCheckEntry()

    override val opCodes: OpCodeMap<IntArray, LinScript> =
            OpCodeHashMap<IntArray, LinScript>().apply {
                this[0x00] = "Text Count" to 2 and ::TextCountEntry
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
                this[0x1F] = null to 7 and ::UnknownEntry
                this[0x20] = null to 5 and ::UnknownEntry
                this[0x21] = "Speaker" to 1 and ::SpeakerEntry
                this[0x22] = null to 3 and ::UnknownEntry
                this[0x23] = null to 5 and ::UnknownEntry
                this[0x24] = null to -1 and ::UnknownEntry
                this[0x25] = "Change UI" to 2 and ::ChangeUIEntry
                this[0x26] = "Set Flag" to 3 and ::SetFlagEntry
                this[0x27] = "Check Character" to 1 and ::CheckCharacterEntry
                this[0x28] = null to -1 and ::UnknownEntry
                this[0x29] = "Check Object" to 1 and ::CheckObjectEntry
                this[0x2A] = "Set Label" to 2 and ::SetLabelEntry
                this[0x2B] = "Choice" to 1 and ::ChoiceEntry
                this[0x2C] = null to 2 and ::UnknownEntry
                this[0x2D] = null to -1 and ::UnknownEntry
                this[0x2E] = null to 5 and ::UnknownEntry
                this[0x2F] = null to 10 and ::UnknownEntry
                this[0x30] = "Show Background" to 3 and ::ShowBackgroundEntry
                this[0x31] = null to -1 and ::UnknownEntry
                this[0x32] = null to 1 and ::UnknownEntry
                this[0x33] = null to 4 and ::UnknownEntry
                this[0x34] = arrayOf("Go To Label", "Goto Label", "Goto") to 2 and ::GoToLabelEntry
                this[0x35] = "Check Flag A" to -1 and ::CheckFlagAEntry
                this[0x36] = "Check Flag B" to -1 and ::UnknownEntry
                this[0x37] = null to -1 and ::UnknownEntry
                this[0x38] = null to -1 and ::UnknownEntry
                this[0x39] = null to 5 and ::UnknownEntry
                this[0x3A] = null to 4 and ::UnknownEntry
                this[0x3B] = null to 2 and ::UnknownEntry
                this[0x3C] = "End Flag Check" to 0 and DR2::EndFlagCheckEntry
                this[0x4B] = "Wait For Input" to 0 and DR2::WaitForInputEntry
                this[0x4C] = "Wait Frame" to 0 and DR2::WaitFrameEntry
            }

    override val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray> =
            emptyMap()
}