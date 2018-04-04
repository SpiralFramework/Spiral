package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.*
import org.abimon.spiral.core.objects.scripting.lin.dr1.DR1LoadMapEntry
import org.abimon.spiral.core.objects.scripting.lin.dr1.DR1LoadScriptEntry
import org.abimon.spiral.core.objects.scripting.lin.dr1.DR1RunScript
import org.abimon.spiral.core.objects.scripting.lin.dr1.DR1TrialCameraEntry
import org.abimon.spiral.core.utils.*
import java.util.*

object DR1 : HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> =
            DataMapper.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("pak/dr1.json"))?.mapValues { (_, value) ->
                ((value as? List<*>)?.asIterable()
                        ?: (value as? Array<*>)?.asIterable())?.mapNotNull { str -> str as? String }?.toTypedArray()
                        ?: emptyArray()
            } ?: emptyMap()

    fun StopScriptEntry(opCode: Int, args: IntArray): StopScriptEntry = StopScriptEntry()
    fun EndFlagCheckEntry(opCode: Int, args: IntArray): EndFlagCheckEntry = EndFlagCheckEntry()

    override val opCodes: OpCodeMap<IntArray, LinScript> =
            OpCodeHashMap<IntArray, LinScript>().apply {
                this[0x00] = "Text Count" to 2 and ::TextCountEntry
                this[0x01] = null to 3 and ::UnknownEntry
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
                this[0x14] = "Trial Camera" to 3 and ::DR1TrialCameraEntry
                this[0x15] = "Load Map" to 3 and ::DR1LoadMapEntry
                this[0x16] = null to -1 and ::UnknownEntry
                this[0x17] = null to -1 and ::UnknownEntry
                this[0x18] = null to -1 and ::UnknownEntry
                this[0x19] = arrayOf("Script", "Load Script") to 3 and ::DR1LoadScriptEntry
                this[0x1A] = arrayOf("Stop Script", "End Script") to 0 and DR1::StopScriptEntry

                this[0x1B] = "Run Script" to 3 and ::DR1RunScript
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
                this[0x2E] = null to 2 and ::UnknownEntry
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
                this[0x3A] = "Wait For Input" to 0 and ::WaitForInputEntry
                this[0x3B] = "Wait Frame" to 0 and ::WaitFrameEntry
                this[0x3C] = "End Flag Check" to 0 and DR1::EndFlagCheckEntry
            }

    override val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray> =
            hashMapOf(
                    0x35 to this::readCheckFlagA
            )

    override val characterIDs: Map<Int, String> =
            mapOf(
                    0 to "Makoto Naegi",
                    1 to "Kiyotaka Ishimaru",
                    2 to "Byakuya Togami",
                    3 to "Mondo Owada",
                    4 to "Leon Kuwata",
                    5 to "Hifumi Yamada",
                    6 to "Yasuhiro Hagakure",
                    7 to "Sayaka Maizono",
                    8 to "Kyoko Kirigiri",
                    9 to "Aoi Asahina",
                    10 to "Toko Fukawa",
                    11 to "Sakura Ogami",
                    12 to "Celeste",
                    13 to "Junko Enoshima",
                    14 to "Chihiro Fujisaki",
                    15 to "Monokuma",
                    16 to "Junko Enoshima",
                    17 to "Alter Ego",
                    18 to "Genocide Jill",
                    19 to "Headmaster",
                    20 to "Makoto's Mom",
                    21 to "Makoto's Dad",
                    22 to "Makoto's Sister",
                    23 to "Narrator",
                    24 to "Kiyotaka-Mondo",
                    25 to "Daiya Owada",
                    30 to "???",
                    33 to "Usami",
                    34 to "Monokuma Backup",
                    35 to "Monokuma Backup (R)",
                    36 to "Monokuma Backup (L)",
                    37 to "Monokuma Backup (M)"
            )

    override val characterIdentifiers: MutableMap<String, Int> =
            HashMap<String, Int>().apply {
                this["Makoto Naegi"]
                this["Makoto"] = 0
                this["Naegi"] = 0
                this["MN"] = 0

                this["Kiyotaka Ishimaru"] = 1
                this["Kiyotaka"] = 1
                this["Ishimaru"] = 1
                this["KI"] = 1

                this["Byakuya Togami"] = 2
                this["Byakuya"] = 2
                this["Togami"] = 2

                this["Mondo Owada"] = 3
                this["Mondo Oowada"] = 3
                this["Mondo"] = 3
                this["Owada"] = 3
                this["Oowada"] = 3
                this["MO"] = 3

                this["Leon Kuwata"] = 4
                this["Leon"] = 4
                this["Kuwata"] = 4
                this["LK"] = 4

                this["Hifumi Yamada"] = 5
                this["Hifumi"] = 5
                this["Yamada"] = 5
                this["HY"] = 5

                this["Yasuhiro Hagakure"] = 6
                this["Yasuhiro"] = 6
                this["Hagakure"] = 6
                this["YH"] = 6

                this["Sayaka Maizono"] = 7
                this["Sayaka"] = 7
                this["Maizono"] = 7
                this["SM"] = 7

                this["Kyoko Kirigiri"] = 8
                this["Kyouko Kirigiri"] = 8
                this["Kyoko"] = 8
                this["Kyouko"] = 8
                this["KK"] = 8

                this["Aoi Asahina"] = 9
                this["Aoi"] = 9
                this["Asahina"] = 9
                this["AA"] = 9

                this["Toko Fukawa"] = 10
                this["Touko Fukawa"] = 10
                this["Toko"] = 10
                this["Touko"] = 10
                this["Fukawa"] = 10
                this["TF"] = 10

                this["Sakura Ogami"] = 11
                this["Sakura Oogami"] = 11
                this["Sakura"] = 11
                this["Ogami"] = 11
                this["Oogami"] = 11
                this["SO"] = 11

                this["Celeste"] = 12

                this["Junko Enoshima"] = 13
                this["Junko"] = 13
                this["Enoshima"] = 13
                this["JE"] = 13

                this["Chihiro Fujisaki"] = 14
                this["Chihiro"] = 14
                this["Fujisaki"] = 14
                this["CF"] = 14

                this["Monokuma"] = 15
                this["MonoKuma"] = 15
                this["Monobear"] = 15
                this["MonoBear"] = 15

                this["Real Junko Enoshima"] = 16
                this["Junko Enoshima (Real)"] = 16

                this["Alter Ego"] = 17
                this["AE"] = 17

                this["Genocide Jill"] = 18
                this["Genocide Jack"] = 18
                this["Genocide Syo"] = 18
                this["Genocider Jill"] = 18
                this["Genocider Jack"] = 18
                this["Genocider Syo"] = 18
                this["Genocider"] = 18
                this["Syo"] = 18

                this["Headmaster"] = 19

                this["Makoto's Mom"] = 20
                this["Makoto's Mum"] = 20

                this["Makoto's Dad"] = 21

                this["Makoto's Sister"] = 22
                this["Komaru Naegi"] = 22
                this["Komaru"] = 22
                this["KN"] = 22

                this["Narrator"] = 23

                this["Kiyotaka-Mondo"] = 24
                this["Kiyondo"] = 24

                this["Daiya Owada"] = 25
                this["Daiya Oowada"] = 25
                this["Daiya"] = 25
                this["DO"] = 25

                this["???"] = 30
                this["Usami"] = 33
                this["Monokuma Backup"] = 34
                this["Monokuma Backup (R)"] = 35
                this["Monokuma Backup (L)"] = 36
                this["Monokuma Backup (M)"] = 37
            }

    override val names: Array<String> =
            arrayOf(
                    "DR1",
                    "Danganronpa 1",
                    "Danganronpa: Trigger Happy Havoc"
            )


    fun readCheckFlagA(stream: LinkedList<Int>): IntArray {
        val args: MutableList<Int> = ArrayList()

        while (stream.isNotEmpty()) {
            if (stream.peek() == 0x70) {
                if (stream.size > 1 && stream[1] == 0x3C)
                    break
            }

            args.add(stream.poll() ?: break)
        }

        return args.toIntArray()
    }

}