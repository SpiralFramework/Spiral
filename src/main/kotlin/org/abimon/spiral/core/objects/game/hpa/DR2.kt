package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.*
import org.abimon.spiral.core.objects.scripting.lin.dr2.DR2LoadMapEntry
import org.abimon.spiral.core.objects.scripting.lin.dr2.DR2LoadScriptEntry
import org.abimon.spiral.core.objects.scripting.lin.dr2.DR2RunScriptEntry
import org.abimon.spiral.core.objects.scripting.lin.dr2.DR2TrialCameraEntry
import org.abimon.spiral.core.utils.*
import java.io.File
import java.util.*

object DR2 : HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> =
            DataMapper.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("pak/dr2.json"))?.mapValues { (_, value) ->
                ((value as? List<*>)?.asIterable()
                        ?: (value as? Array<*>)?.asIterable())?.mapNotNull { str -> str as? String }?.toTypedArray()
                        ?: emptyArray()
            } ?: emptyMap()

    fun StopScriptEntry(opCode: Int, args: IntArray): StopScriptEntry = org.abimon.spiral.core.objects.scripting.lin.StopScriptEntry()
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
                this[0x1F] = "Screen Flash" to 7 and ::ScreenFlashEntry
                this[0x20] = null to 5 and ::UnknownEntry
                this[0x21] = "Speaker" to 1 and ::SpeakerEntry
                this[0x22] = "Screen Fade" to 3 and ::ScreenFadeEntry
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
                this[0x3A] = null to 4 and ::UnknownEntry //Wait For Input DR1
                this[0x3B] = null to 2 and ::UnknownEntry //Wait Frame DR1
                this[0x3C] = "End Flag Check" to 0 and DR2::EndFlagCheckEntry
                this[0x4B] = "Wait For Input" to 0 and ::WaitForInputEntry
                this[0x4C] = "Wait Frame" to 0 and ::WaitFrameEntry

                val opCodes = File("dr2-ops.json")

                if (opCodes.exists()) {

                    DataMapper.fileToMap(opCodes)?.forEach { opName, params ->
                        val array = ((params as? Array<*>)?.toList() ?: (params as? List<*>))?.mapNotNull { any ->
                            val str = any.toString()
                            if (str.startsWith("0x"))
                                return@mapNotNull str.substring(2).toIntOrNull(16)
                            return@mapNotNull str.toIntOrNull()
                        } ?: return@forEach
                        this[array[0]] = opName to array[1] and ::UnknownEntry
                    }
                }
            }

    override val customOpCodeArgumentReader: Map<Int, (LinkedList<Int>) -> IntArray> =
            emptyMap()

    override val characterIDs: Map<Int, String> =
            mapOf(
                    0 to "Hajime Hinata",
                    1 to "Nagito Komaeda",
                    2 to "Byakuya Togami",
                    3 to "Gundham Tanaka",
                    4 to "Kazuichi Soda",
                    5 to "Teruteru Hanamura",
                    6 to "Nekomaru Nidai",
                    7 to "Fuyuhiko Kuzuryu",
                    8 to "Akane Owari",
                    9 to "Chiaki Nanami",
                    10 to "Sonia Nevermind",
                    11 to "Hiyoko Saionji",
                    12 to "Mahiru Koizumi",
                    13 to "Mikan Tsumiki",
                    14 to "Ibuki Mioda",
                    15 to "Peko Pekoyama",
                    16 to "Monokuma",
                    17 to "Monomi",
                    18 to "Junko Enoshima",
                    19 to "Nekomaru Nidai",
                    20 to "Makoto Naegi",
                    21 to "Kyoko Kirigiri",
                    22 to "Byakuya Togami",
                    23 to "Teruteru's Mom",
                    24 to "Alter Ego",
                    25 to "Minimaru",
                    26 to "Monokuma & Monomi",
                    27 to "Narrator",
                    39 to "Usami",
                    40 to "Sparkling Justice",
                    48 to "Junko Enoshima",
                    50 to "Girl A",
                    51 to "Girl B",
                    52 to "Girl C",
                    53 to "Girl D",
                    54 to "Girl E",
                    55 to "Guy F",
                    56 to "???"
            )

    override val characterIdentifiers: MutableMap<String, Int> =
            HashMap<String, Int>().apply {
                this["Hajime Hinata"] = 0
                this["Hajime"] = 0
                this["Hinata"] = 0
                this["HH"] = 0

                this["Nagito Komaeda"] = 1
                this["Nagito"] = 1
                this["Komaeda"] = 1
                this["NK"] = 1

                this["Byakuya Togami"] = 2
                this["Byakuya Twogami"] = 2
                this["Byakuya"] = 2
                this["Togami"] = 2
                this["Twogami"] = 2
                this["Imposter"] = 2
                this["Impostor"] = 2
                this["BT"] = 2

                this["Gundham Tanaka"] = 3
                this["Gundam Tanaka"] = 3
                this["Gundham"] = 3
                this["Gundam"] = 3
                this["GT"] = 3

                this["Kazuichi Soda"] = 4
                this["Kazuichi Souda"] = 4
                this["Kazuichi"] = 4
                this["Soda"] = 4
                this["Souda"] = 4
                this["KS"] = 4

                this["Teruteru Hanamura"] = 5
                this["Teruteru"] = 5
                this["Hanamura"] = 5
                this["TH"] = 5

                this["Nekomaru Nidai"] = 6
                this["Nekomaru"] = 6
                this["Nidai"] = 6
                this["NN"] = 6

                this["Fuyuhiko Kuzuryu"] = 7
                this["Fuyuhiko Kuzuryuu"] = 7
                this["Fuyuhiko"] = 7
                this["Kuzuryu"] = 7
                this["Kuzuryuu"] = 7

                this["Akane Owari"] = 8
                this["Akane"] = 8
                this["Owari"] = 8
                this["AO"] = 8

                this["Chiaki Nanami"] = 9
                this["Chiaki"] = 9
                this["Nanami"] = 9
                this["CN"] = 9

                this["Sonia Nevermind"] = 10
                this["Sonia"] = 10
                this["Nevermind"] = 10
                this["SN"] = 10

                this["Hiyoko Saionji"] = 11
                this["Hiyoko"] = 11
                this["Saionji"] = 11
                this["HS"] = 11

                this["Mahiru Koizumi"] = 12
                this["Mahiru"] = 12
                this["Koizumi"] = 12
                this["MK"] = 12

                this["Mikan Tsumiki"] = 13
                this["Mikan"] = 13
                this["Tsumiki"] = 13
                this["MT"] = 13

                this["Ibuki Mioda"] = 14
                this["Ibuki"] = 14
                this["Mioda"] = 14
                this["IM"] = 14

                this["Peko Pekoyama"] = 15
                this["Peko"] = 15
                this["Pekoyama"] = 15
                this["PP"] = 15

                this["Monokuma"] = 16
                this["MonoKuma"] = 16
                this["Monobear"] = 16
                this["MonoBear"] = 16

                this["Monomi"] = 17

                this["Junko Enoshima"] = 18
                this["Junko"] = 18
                this["Enoshima"] = 18
                this["JE"] = 18

                this["Mechamaru Nidai"] = 19
                this["Mechamaru"] = 19

                this["Makoto Naegi"] = 20
                this["Makoto"] = 20
                this["Naegi"] = 20
                this["MN"] = 20

                this["Kyoko Kirigiri"] = 21
                this["Kyouko Kirigiri"] = 21
                this["Kyoko"] = 21
                this["Kyouko"] = 21
                this["Kirigiri"] = 21
                this["KK"] = 21

                this["Byakuya Togami (Real)"] = 22
                this["Real Byakuya Togami"] = 22

                this["Teruteru's Mom"] = 23
                this["Teruteru's Mum"] = 23

                this["Alter Ego"] = 24
                this["AE"] = 24

                this["Minimaru"] = 25

                this["Monokuma & Monomi"] = 26
                this["MonoKuma & Monomi"] = 26
                this["Monobear & Monomi"] = 26
                this["MonoBear & Monomi"] = 26

                this["Narrator"] = 27

                this["?"] = 28

                this["Usami"] = 39

                this["Sparkling Justice"] = 40

                this["Girl A"] = 50
                this["Girl B"] = 51
                this["Girl C"] = 52
                this["Girl D"] = 53
                this["Girl E"] = 54
                this["Guy F"] = 55
                this["???"] = 56
            }

    override val names: Array<String> =
            arrayOf(
                    "DR2",
                    "SDR2",
                    "Danganronpa 2",
                    "Danganronpa 2: Goodbye Despair"
            )

    override val steamID: String = "413420"
}