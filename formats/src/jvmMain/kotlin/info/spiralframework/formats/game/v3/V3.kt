package info.spiralframework.formats.game.v3

import info.spiralframework.base.common.text.toIntBaseN
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.scripting.EnumWordScriptCommand
import info.spiralframework.formats.scripting.wrd.*
import info.spiralframework.formats.utils.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.mapNotNull
import kotlin.collections.set

object V3 : DRGame {
    @UnstableDefault
    val opCodes: OpCodeMap<IntArray, WrdScript> =
            OpCodeHashMap<IntArray, WrdScript>().apply {
                this[0x00] = "Set Flag" to 4 and ::UnknownEntry //FLG
                this[0x01] = "If Flag" to -1 and ::UnknownEntry //IFF
                this[0x02] = "Set Game Parameter" to 6 and ::UnknownEntry //WAK
                this[0x03] = "If Game Parameter" to 6 and ::UnknownEntry //IFW
                this[0x04] = "Switch" to 2 and ::UnknownEntry //SWI
                this[0x05] = "Case" to 2 and ::UnknownEntry //CAS
                this[0x06] = null to 6 and ::UnknownEntry //MPF, Map Flag?
                this[0x07] = null to -1 and ::UnknownEntry //SPW
                this[0x08] = "Set Game Mode" to 8 and ::UnknownEntry //MOD
                this[0x09] = null to 2 and ::UnknownEntry //HUM
                this[0x0A] = "Check For Variable" to 2 and ::UnknownEntry //CHK, seems to check if a given variable/flag exists?
                this[0x0B] = "Truth Bullet" to 4 and ::UnknownEntry //KTB
                this[0x0C] = null to -1 and ::UnknownEntry //CLR
                this[0x0D] = null to -1 and ::UnknownEntry //RET
                this[0x0E] = "Camera Look" to 10 and ::UnknownEntry //KNM
                this[0x0F] = null to -1 and ::UnknownEntry //CAP
                this[0x10] = arrayOf("Script", "Load Script") to 4 and ::ScriptEntry //FIL
                this[0x11] = arrayOf("Stop Script", "End Script") to 0 and ::UnknownEntry //END
                this[0x12] = "Run Subroutine Script" to 4 and ::UnknownEntry //SUB
                this[0x13] = "Return" to 0 and ::UnknownEntry //RTN
                this[0x14] = "Label" to 2 and ::LabelEntry //LAB
                this[0x15] = arrayOf("Jump To Label", "Goto") to 2 and ::UnknownEntry //JMP
                this[0x16] = "Movie" to -1 and ::UnknownEntry //MOV
                this[0x17] = "Animation" to 8 and ::UnknownEntry //FLS
                this[0x18] = "Animation Effect" to 12 and ::UnknownEntry //FLM
                this[0x19] = "Voice" to 4 and ::VoiceLineEntry //VOI
                this[0x1A] = "Music" to 6 and ::UnknownEntry //BGM
                this[0x1B] = "Sound Effect" to 4 and ::UnknownEntry //SE_
                this[0x1C] = "Jingle" to -1 and ::UnknownEntry //JIN
                this[0x1D] = "Speaker" to 2 and ::SpeakerEntry//CHN
                this[0x1E] = "Camera Vibration" to 6 and ::UnknownEntry //VIB
                this[0x1F] = "Fade Screen" to 6 and ::UnknownEntry //FDS
                this[0x20] = null to -1 and ::UnknownEntry //FLA
                this[0x21] = "Set Lighting Parameter" to 6 and ::UnknownEntry //LIG
                this[0x22] = "Set Character Parameter" to 10 and ::UnknownEntry //CHR
                this[0x23] = "Set Background Parameter" to 8 and ::UnknownEntry //BGD
                this[0x24] = "Cutin" to 4 and ::UnknownEntry //CUT, used for stuff like "Truth Bullet Get!" popups I think
                this[0x25] = "Character Vibration" to 10 and ::UnknownEntry //ADF
                this[0x26] = null to -1 and ::UnknownEntry //PAL
                this[0x27] = "Load Map" to 6 and ::UnknownEntry //MAP
                this[0x28] = "Load Object" to 6 and ::UnknownEntry //OBJ
                this[0x29] = null to 16 and ::UnknownEntry //BUL: Unsure, that's a lot of variables
                this[0x2A] = "Cross Fade" to -1 and ::UnknownEntry //CRF
                this[0x2B] = "Camera Command" to 10 and ::UnknownEntry //CAM
                this[0x2C] = "Split Screen Mode" to 2 and ::UnknownEntry //KWM
                this[0x2D] = null to 6 and ::UnknownEntry //ARE, area parameters?
                this[0x2E] = "Set Key Item" to -1 and ::UnknownEntry //KEY, enables/disables "key" items for unlocking new areas of the school
                this[0x2F] = "Set Text Window Parameter" to 8 and ::UnknownEntry //WIN
                this[0x30] = null to -1 and ::UnknownEntry //MSC
                this[0x31] = null to -1 and ::UnknownEntry //CSM
                this[0x32] = "Post-Processing" to 10 and ::UnknownEntry //PST
                this[0x33] = null to 10 and ::UnknownEntry //KNS, numeric parameters related to camera movement? (raw camera coords???)
                this[0x34] = "Set Font" to 4 and ::UnknownEntry //FON
                this[0x35] = "Load Background Object" to 10 and ::UnknownEntry //BGO
                this[0x36] = null to 0 and ::UnknownEntry //LOG, edits text backlog
                this[0x37] = null to 2 and ::UnknownEntry //SPT, only used in Class Trials, always passed "non"?
                this[0x38] = null to 20 and ::UnknownEntry //CDV: Unsure, that is an awful lot of variables
                this[0x39] = "Size Modifier" to 8 and ::UnknownEntry //SZM, only used in Class Trials?
                this[0x3A] = null to 2 and ::UnknownEntry //PVI, Class Trial Chapter? Pre-trial intermission?
                this[0x3B] = "Give EXP" to 2 and ::UnknownEntry //EXP
                this[0x3C] = null to 2 and ::UnknownEntry //MTA, only used in Class Trials(?), usually passed "non"?
                this[0x3D] = "Move Object to Position" to -1 and ::UnknownEntry //MVP
                this[0x3E] = "Create Position" to 10 and ::UnknownEntry //POS
                this[0x3F] = "Program World Character Icon" to -1 and ::UnknownEntry //ICO
                this[0x40] = "Set Exisal AI Parameters" to 20 and ::UnknownEntry //EAI
                this[0x41] = "Set Object Collision" to -1 and ::UnknownEntry //COL
                this[0x42] = "Camera Follow Path" to -1 and ::UnknownEntry //CFP
                this[0x43] = "Set Text Color" to -1 and ::UnknownEntry //CLT=
                this[0x44] = null to -1 and ::UnknownEntry //R=
                this[0x45] = "Display Gamepad Symbol" to -1 and ::UnknownEntry //PAD=
                this[0x46] = "Text" to 2 and ::UnknownEntry //LOC
                this[0x47] = "Wait For Input" to 0 and ::UnknownEntry //BTN
                this[0x48] = null to -1 and ::UnknownEntry //ENT
                this[0x49] = "End If" to 0 and ::UnknownEntry //CED, called immediately after all variable-length "if" opcodes
                this[0x4A] = "Sublabel" to 2 and ::UnknownEntry //LBN
                this[0x4B] = "Jump To Sublabel" to 2 and ::UnknownEntry //JMN

                val opCodes = File("v3-ops.json")

                if (opCodes.exists()) {
                    Json.parse((String.serializer() to JsonOpcode.serializer()).map, opCodes.readText())
                            .forEach { (name, op) ->
                                this[op.opcode.toIntBaseN()] = name to op.argCount and ::UnknownEntry
                            }
                }
            }

    @UnstableDefault
    val opCodeCommandEntries: Map<Int, Array<EnumWordScriptCommand>> =
            HashMap<Int, Array<EnumWordScriptCommand>>().apply {
                //Regex to parse Captain's table:
                //\{(0x[0-9A-F]{2}), ".{0,3}=?", \{\}, \{([0-3]?(?: ?, ?[0-3])*)\}\},?`
                //this[$1] = arrayOf($2)
                this[0x00] = EnumWordScriptCommand.arrayOf(0, 0)              // Set Flag
                this[0x01] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // If Flag
                this[0x02] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Wake? Work? (Seems to be used to configure game engine parameters)
                this[0x03] = EnumWordScriptCommand.arrayOf(0, 0, 1)           // If WAK
                this[0x04] = EnumWordScriptCommand.arrayOf(0)                 // Begin switch statement
                this[0x05] = EnumWordScriptCommand.arrayOf(1)                 // Switch Case
                this[0x06] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Map Flag?
                this[0x07] = EnumWordScriptCommand.arrayOf()
                this[0x08] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0)        // Set Modifier (Also used to configure game engine parameters)
                this[0x09] = EnumWordScriptCommand.arrayOf(0)                 // Human? Seems to be used to initialize "interactable" objects in a map?
                this[0x0A] = EnumWordScriptCommand.arrayOf(0)                 // Check?
                this[0x0B] = EnumWordScriptCommand.arrayOf(0, 0)              // Kotodama?
                this[0x0C] = EnumWordScriptCommand.arrayOf()                  // Clear?
                this[0x0D] = EnumWordScriptCommand.arrayOf()                  // Return? There's another command later which is definitely return, though...
                this[0x0E] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0, 0)     // Kinematics (camera movement)
                this[0x0F] = EnumWordScriptCommand.arrayOf()                  // Camera Parameters?
                this[0x10] = EnumWordScriptCommand.arrayOf(0, 0)              // Load Script File & jump to label
                this[0x11] = EnumWordScriptCommand.arrayOf()                  // End of script or switch case
                this[0x12] = EnumWordScriptCommand.arrayOf(0, 0)              // Jump to subroutine
                this[0x13] = EnumWordScriptCommand.arrayOf()                  // Return (called inside subroutine)
                this[0x14] = EnumWordScriptCommand.arrayOf(3)                 // Label number
                this[0x15] = EnumWordScriptCommand.arrayOf(0)                 // Jump to label
                this[0x16] = EnumWordScriptCommand.arrayOf(0, 0)              // Movie
                this[0x17] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0)        // Flash
                this[0x18] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0, 0, 0)  // Flash Modifier?
                this[0x19] = EnumWordScriptCommand.arrayOf(0, 0)              // Play voice clip
                this[0x1A] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Play BGM
                this[0x1B] = EnumWordScriptCommand.arrayOf(0, 0)              // Play sound effect
                this[0x1C] = EnumWordScriptCommand.arrayOf(0, 0)              // Play jingle
                this[0x1D] = EnumWordScriptCommand.arrayOf(0)                 // Set active character ID (current person speaking)
                this[0x1E] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Camera Vibration
                this[0x1F] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Fade Screen
                this[0x20] = EnumWordScriptCommand.arrayOf()
                this[0x21] = EnumWordScriptCommand.arrayOf(0, 1, 0)           // Lighting Parameters
                this[0x22] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0, 0)     // Character Parameters
                this[0x23] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0)        // Background Parameters
                this[0x24] = EnumWordScriptCommand.arrayOf(0, 0)              // Cutin (display image for things like Truth Bullets, etc.)
                this[0x25] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0 ,0)     // Character Vibration?
                this[0x26] = EnumWordScriptCommand.arrayOf()
                this[0x27] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Load Map
                this[0x28] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Load Object
                this[0x29] = EnumWordScriptCommand.arrayOf(0,0,0,0,0,0,0,0)
                this[0x2A] = EnumWordScriptCommand.arrayOf(0,0,0,0,0,0,0)     // Cross Fade
                this[0x2B] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0, 0)     // Camera command
                this[0x2C] = EnumWordScriptCommand.arrayOf(0)                 // Game/UI Mode
                this[0x2D] = EnumWordScriptCommand.arrayOf(0, 0, 0)
                this[0x2E] = EnumWordScriptCommand.arrayOf(0, 0)              // Enable/disable "key" items for unlocking areas
                this[0x2F] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0)        // Window parameters
                this[0x30] = EnumWordScriptCommand.arrayOf()
                this[0x31] = EnumWordScriptCommand.arrayOf()
                this[0x32] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0, 0)     // Post-Processing
                this[0x33] = EnumWordScriptCommand.arrayOf(0, 1, 1, 1, 1)     // Kinematics Numeric parameters?
                this[0x34] = EnumWordScriptCommand.arrayOf(1, 1)              // Set Font
                this[0x35] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0, 0)     // Load Background Object
                this[0x36] = EnumWordScriptCommand.arrayOf()                  // Edit Text Backlog?
                this[0x37] = EnumWordScriptCommand.arrayOf(0)                 // Used only in Class Trial? Always set to "non"?
                this[0x38] = EnumWordScriptCommand.arrayOf(0,0,0,0,0,0,0,0,0,0)
                this[0x39] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0)        // Size Modifier (Class Trial)?
                this[0x3A] = EnumWordScriptCommand.arrayOf(0)                 // Class Trial Chapter? Pre-trial intermission?
                this[0x3B] = EnumWordScriptCommand.arrayOf(0)                 // Give EXP
                this[0x3C] = EnumWordScriptCommand.arrayOf(0)                 // Used only in Class Trial? Usually set to "non"?
                this[0x3D] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Move object to its designated position?
                this[0x3E] = EnumWordScriptCommand.arrayOf(0, 0, 0, 0, 0)     // Object/Exisal position
                this[0x3F] = EnumWordScriptCommand.arrayOf(0,0,0,0)           // Display a Program World character portrait
                this[0x40] = EnumWordScriptCommand.arrayOf(0,0,0,0,0,0,0,0,0,0)  // Exisal AI
                this[0x41] = EnumWordScriptCommand.arrayOf(0, 0, 0)           // Set object collision
                this[0x42] = EnumWordScriptCommand.arrayOf(0,0,0,0,0,0,0,0,0) // Camera Follow Path? Seems to make the camera move in some way
                this[0x43] = EnumWordScriptCommand.arrayOf(0)                // Text modifier command
                this[0x44] = EnumWordScriptCommand.arrayOf()
                this[0x45] = EnumWordScriptCommand.arrayOf(0)                // Gamepad button symbol
                this[0x46] = EnumWordScriptCommand.arrayOf(2)                 // Display text string
                this[0x47] = EnumWordScriptCommand.arrayOf()                  // Wait for button press
                this[0x48] = EnumWordScriptCommand.arrayOf()
                this[0x49] = EnumWordScriptCommand.arrayOf()                  // Check End (Used after IFF and IFW commands)
                this[0x4A] = EnumWordScriptCommand.arrayOf(1)                 // Local Branch Number (for branching case statements)
                this[0x4B] = EnumWordScriptCommand.arrayOf(1)                  // Jump to Local Branch (for branching case statements)

                val opCodes = File("v3-command-entries.json")

                if (opCodes.exists()) {
                    Json.parse((String.serializer() to String.serializer().list).map, opCodes.readText())
                            .forEach { (opName, params) ->
                                this[opName.toIntBaseN()] = params.mapNotNull { str ->
                                    return@mapNotNull EnumWordScriptCommand.values().firstOrNull { enum -> enum.name.equals(str, true) }
                                }.toTypedArray()
                            }
                }
            }

    override val names: Array<String> =
            arrayOf(
                    "DRv3",
                    "NDRv3",
                    "V3",
                    "Danganronpa V3: Killing Harmony",
                    "New Danganronpa V3: Killing Harmony"
            )
    override val identifier: String = "drv3"

    override val colourCodes: Map<String, String> =
            mapOf(
                    "white" to "cltNORMAL",
                    "yellow" to "cltSTRONG",
                    "blue" to "cltMIND",
                    "green" to "cltSYSTEM",

                    "normal" to "cltNORMAL",

                    "bold" to "cltSTRONG",
                    "strong" to "cltSTRONG",

                    "mind" to "cltMIND",

                    "narrator" to "cltSYSTEM",
                    "system" to "cltSYSTEM"
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