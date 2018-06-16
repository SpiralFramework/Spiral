package org.abimon.spiral.core.objects.game.v3

import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.core.utils.*
import java.io.File

object V3 : DRGame {
    val opCodes: OpCodeMap<IntArray, WrdScript> =
            OpCodeHashMap<IntArray, WrdScript>().apply {
                this[0x00] = "Set Flag" to 4 and ::UnknownEntry
                this[0x01] = null to -1 and ::UnknownEntry
                this[0x02] = "Check Flag" to 6 and ::UnknownEntry
                this[0x03] = null to 6 and ::UnknownEntry
                this[0x04] = null to 2 and ::UnknownEntry
                this[0x05] = null to 2 and ::UnknownEntry
                this[0x06] = null to 6 and ::UnknownEntry
                this[0x07] = null to -1 and ::UnknownEntry
                this[0x08] = null to 8 and ::UnknownEntry
                this[0x09] = null to 2 and ::UnknownEntry
                this[0x0A] = null to 2 and ::UnknownEntry
                this[0x0B] = null to 4 and ::UnknownEntry
                this[0x0C] = null to -1 and ::UnknownEntry
                this[0x0D] = null to -1 and ::UnknownEntry
                this[0x0E] = null to 10 and ::UnknownEntry
                this[0x0F] = null to -1 and ::UnknownEntry

                this[0x10] = arrayOf("Script", "Load Script") to 4 and ::UnknownEntry
                this[0x11] = arrayOf("Stop Script", "End Script") to 0 and ::UnknownEntry
                this[0x12] = "Run Script" to 4 and ::UnknownEntry
                this[0x13] = null to 0 and ::UnknownEntry
                this[0x14] = "Label" to 2 and ::UnknownEntry
                this[0x15] = null to 2 and ::UnknownEntry
                this[0x16] = null to -1 and ::UnknownEntry
                this[0x17] = "Animation" to 8 and ::UnknownEntry
                this[0x18] = null to 12 and ::UnknownEntry
                this[0x19] = "Voice" to 4 and ::UnknownEntry
                this[0x1A] = "Music" to 6 and ::UnknownEntry
                this[0x1B] = null to 4 and ::UnknownEntry
                this[0x1C] = null to -1 and ::UnknownEntry
                this[0x1D] = "Speaker" to 2 and ::UnknownEntry
                this[0x1E] = null to 6 and ::UnknownEntry
                this[0x1F] = null  to 6 and ::UnknownEntry
                this[0x20] = null to -1 and ::UnknownEntry
                this[0x21] = null to 6 and ::UnknownEntry
                this[0x22] = null to 10 and ::UnknownEntry
                this[0x23] = null to 8 and ::UnknownEntry
                this[0x24] = null to 4 and ::UnknownEntry
                this[0x25] = null to 10 and ::UnknownEntry
                this[0x26] = null to -1 and ::UnknownEntry
                this[0x27] = null to 6 and ::UnknownEntry
                this[0x28] = null to 6 and ::UnknownEntry
                this[0x29] = null to 16 and ::UnknownEntry //Unsure, that's a lot of variables
                this[0x2A] = null to -1 and ::UnknownEntry
                this[0x2B] = null to 10 and ::UnknownEntry
                this[0x2C] = null to 2 and ::UnknownEntry
                this[0x2D] = null to 6 and ::UnknownEntry
                this[0x2E] = null to -1 and ::UnknownEntry
                this[0x2F] = null to 8 and ::UnknownEntry
                this[0x30] = null to -1 and ::UnknownEntry
                this[0x31] = null to -1 and ::UnknownEntry
                this[0x32] = null to 10 and ::UnknownEntry
                this[0x33] = null to 10 and ::UnknownEntry
                this[0x34] = null to 4 and ::UnknownEntry
                this[0x35] = null to 10 and ::UnknownEntry
                this[0x36] = null to 0 and ::UnknownEntry
                this[0x37] = null to 2 and ::UnknownEntry
                this[0x38] = null to 20 and ::UnknownEntry //Unsure, that is an awful lot of variables
                this[0x39] = null to 8 and ::UnknownEntry
                this[0x3A] = null to 2 and ::UnknownEntry
                this[0x3B] = null to 2 and ::UnknownEntry
                this[0x3C] = null to 2 and ::UnknownEntry
                this[0x3D] = null to -1 and ::UnknownEntry
                this[0x3E] = null to 10 and ::UnknownEntry
                this[0x3F] = null to -1 and ::UnknownEntry
                this[0x40] = null to 10 and ::UnknownEntry
                this[0x41] = null to -1 and ::UnknownEntry
                this[0x42] = null to -1 and ::UnknownEntry
                this[0x43] = null to -1 and ::UnknownEntry
                this[0x44] = null to -1 and ::UnknownEntry
                this[0x45] = null to -1 and ::UnknownEntry
                this[0x46] = "Text" to 2 and ::UnknownEntry
                this[0x47] = "Wait For Input" to 0 and ::UnknownEntry
                this[0x48] = null to -1 and ::UnknownEntry
                this[0x49] = null to 0 and ::UnknownEntry //Possibly Wait Frame
                this[0x4A] = null to 2 and ::UnknownEntry
                this[0x4B] = null to 2 and ::UnknownEntry
                this[0x4C] = null to -1 and ::UnknownEntry
                this[0x4D] = null to -1 and ::UnknownEntry
                this[0x4E] = null to -1 and ::UnknownEntry
                this[0x4F] = null to -1 and ::UnknownEntry
                this[0x50] = null to -1 and ::UnknownEntry
                this[0x51] = null to -1 and ::UnknownEntry
                this[0x52] = null to -1 and ::UnknownEntry
                this[0x53] = "Speaker" to 2 and ::UnknownEntry

                val opCodes = File("v3-ops.json")

                if (opCodes.exists()) {

                    DataHandler.fileToMap(opCodes)?.forEach { opName, params ->
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

    val opCodeCommandEntries: Map<Int, Array<EnumWordScriptCommand>> =
            HashMap<Int, Array<EnumWordScriptCommand>>().apply {
                this[0x14] = arrayOf(EnumWordScriptCommand.LABEL)
                this[0x1D] = arrayOf(EnumWordScriptCommand.PARAMETER)
                this[0x46] = arrayOf(EnumWordScriptCommand.STRING)
            }

    override val names: Array<String> =
            arrayOf(
                    "DRv3",
                    "NDRv3",
                    "V3",
                    "Danganronpa V3: Killing Harmony",
                    "New Danganronpa V3: Killing Harmony"
            )

    override val steamID: String = "567640"

    val characterIDs: Map<Int, String> =
            mapOf(
                      0 to "C000_Saiha",
                      1 to "C001_Momot",
                      2 to "C002_Hoshi",
                      3 to "C003_Amami",
                      4 to "C004_Gokuh",
                      5 to "C005_Oma__",
                      6 to "C006_Shing",
                      7 to "C007_Ki-Bo",
                      8 to "C008_Tojo_",
                      9 to "C009_Yumen",
                     10 to "C010_Haruk",
                     11 to "C011_Chaba",
                     12 to "C012_Shiro",
                     13 to "C013_Yonag",
                     14 to "C014_Iruma",
                     15 to "C015_Akama",
                     16 to "C016_GokuA",
                     20 to "C020_Monok",
                     21 to "C021_Mtaro",
                     22 to "C022_Msuke",
                     23 to "C023_Mfunn",
                     24 to "C024_Mdam_",
                     25 to "C025_Mkid_",
                     26 to "C026_Eguis",
                     27 to "C027_Mono5",
                     28 to "C028_Mono4",
                     29 to "C029_Mono3",
                     31 to "C031_MonoMono2",
                     33 to "C033_MonoMono5",
                     34 to "C034_Mmono",
                     35 to "C035_Exred",
                     36 to "C036_Exyel",
                     37 to "C037_Expin",
                     //38 to "C038_Exgre"
                     39 to "C039_Exblu",
                     40 to "C040_ExMom",
                     41 to "C041_ExOma",
                     42 to "C042_Eno53",
                     43 to "C043_MonoKibo",
                     46 to "C046_Makot",
                     47 to "C047_Clas1",
                     48 to "C048_Clas2",
                     49 to "C049_Clas3",
                     54 to "C054_MomGF",
                     55 to "C055_MomGM",
                     56 to "C056_Child",
                     57 to "C057_HeadT",
                     58 to "C058_MonoMono4",
                    100 to "C100_Naegi",
                    101 to "C101_Ishim",
                    102 to "C102_Togam",
                    103 to "C103_Owada",
                    104 to "C104_Kuwat",
                    105 to "C105_Yamad",
                    106 to "C106_Hagak",
                    107 to "C107_Fujis",
                    108 to "C108_Maizo",
                    109 to "C109_Kirig",
                    110 to "C110_Asahi",
                    111 to "C111_Fukaw",
                    112 to "C112_Genoc",
                    113 to "C113_Ogami",
                    114 to "C114_Celes",
                    115 to "C115_Enosh",
                    120 to "C120_Hinat",
                    121 to "C121_Komae",
                    122 to "C122_Swind",
                    123 to "C123_Tanak",
                    124 to "C124_Souda",
                    125 to "C125_Hanam",
                    126 to "C126_Nidai",
                    127 to "C127_Kuzur",
                    128 to "C128_Owari",
                    129 to "C129_Nanam",
                    130 to "C130_Never",
                    131 to "C131_Saion",
                    132 to "C132_Koizu",
                    133 to "C133_Tsumi",
                    134 to "C134_Mioda",
                    135 to "C135_Pekoy",
                    136 to "C136_Kamuk"
            )

    val characterIdentifiers: Map<String, Int> =
            HashMap<String, Int>().apply {
                this["Shuichi Saihara"] = 0
                this["Shuichi"] = 0
                this["Saihara"] = 0

                this["Kaito Momota"] = 1
                this["Kaito"] = 1
                this["Momota"] = 1

                this["Ryoma Hoshi"] = 2
                this["Ryoma"] = 2
                this["Hoshi"] = 2

                this["Rantaro Amami"] = 3
                this["Rantaro"] = 3
                this["Amami"] = 3

                this["Gonta Gokuhara"] = 4
                this["Gonta"] = 4
                this["Gokuhara"] = 4

                this["Kokichi Oma"] = 5
                this["Kokichi Ouma"] = 5
                this["Kokichi"] = 5
                this["Oma"] = 5
                this["Ouma"] = 5

                this["Korekiyo Shinguji"] = 6
                this["Korekiyo"] = 6
                this["Shinguji"] = 6

                this["Ki-Bo"] = 7
                this["ki-bo"] = 7
                this["KiBo"] = 7
                this["Kibo"] = 7
                this["Keebo"] = 7

                this["Kirumi Tojo"] = 8
                this["Kirumi"] = 8
                this["Tojo"] = 8

                this["Himiko Yumeno"] = 9
                this["Himiko"] = 9
                this["Yumeno"] = 9

                this["Maki Harukawa"] = 10
                this["Maki"] = 10
                this["Harukawa"] = 10

                this["Tenko Chabashira"] = 11
                this["Tenko"] = 11
                this["Chabashira"] = 11

                this["Tsumugi Shirogane"] = 12
                this["Tsumugi"] = 12
                this["Shirogane"] = 12

                this["Angie Yonaga"] = 13
                this["Angie"] = 13
                this["Yonaga"] = 13

                this["Miu Iruma"] = 14
                this["Miu"] = 14
                this["Iruma"] = 14

                this["Kaede Akamatsu"] = 15
                this["Kaede"] = 15
                this["Akamatsu"] = 15

                this["Monokuma"] = 20
                this["Monotaro"] = 21
                this["Monosuke"] = 22
                this["Monophanie"] = 23
                this["Monofunny"] = 23
                this["Monodam"] = 24
                this["Monokid"] = 25

                this["Exisal"] = 26
            }
}