package info.spiralframework.formats.common.games

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.OpcodeCommandTypeMap
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.data.EnumWordScriptCommand
import info.spiralframework.formats.common.data.buildOpcodeCommandTypes
import info.spiralframework.formats.common.data.buildScriptOpcodes
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.scripting.wrd.UnknownWrdEntry
import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry
import info.spiralframework.formats.common.withFormats
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
open class DRv3(
        override val wrdCharacterNames: Map<String, String>,
        override val wrdCharacterIdentifiers: Map<String, String>,
        override val wrdColourCodes: Map<String, String>,
        override val wrdItemNames: Array<String>,
        customOpcodes: List<JsonOpcode>
) : DrGame, DrGame.WordScriptable, DrGame.ScriptOpcodeFactory<Array<WordScriptValue>, WrdEntry> {
    companion object {
        @Serializable
        data class DRv3GameJson(val character_names: Map<String, String>, val character_identifiers: Map<String, String>, val colour_codes: Map<String, String>, val item_names: Array<String>)

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext): DRv3? {
            try {
                return unsafe(context)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.game.invalid", iae) }
                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext): DRv3 {
            withFormats(context) {
                //                if (isCachedShortTerm("games/drv3.json"))
                val gameSource = requireNotNull(loadResource("games/drv3.json", Dr2::class))
                val gameJson = Json.parse(DRv3GameJson.serializer(), requireNotNull(gameSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())

                val customOpcodeSource = loadResource("opcodes/drv3.json", Dr1::class)
                val customOpcodes: List<JsonOpcode>
                if (customOpcodeSource != null) {
                    customOpcodes = Json.parse(JsonOpcode.serializer().list, requireNotNull(customOpcodeSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())
                } else {
                    customOpcodes = emptyList()
                }

//                val customOpcodeSource = loadResource("opcodes/dr2.json", Dr2::class)
//                val customOpcodes: List<JsonOpcode>
//                if (customOpcodeSource != null) {
//                    customOpcodes = Json.parse(JsonOpcode.serializer().list, requireNotNull(customOpcodeSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())
//                } else {
//                    customOpcodes = emptyList()
//                }

                return DRv3(gameJson.character_names, gameJson.character_identifiers, gameJson.colour_codes, gameJson.item_names, customOpcodes)
            }
        }
    }

    override val names: Array<String> = arrayOf("DRv3", "NDRv3", "V3", "Danganronpa V3: Killing Harmony", "New Danganronpa V3: Killing Harmony")
    override val identifier: String = "drv3"
    override val steamID: String = "567640"

    override val wrdOpcodeMap: OpcodeMap<Array<WordScriptValue>, WrdEntry> = buildScriptOpcodes {
        opcode(0x00, names = arrayOf("Set Flag", "FLG"), argumentCount = 2) //FLG
        flagCheck(0x01, names = arrayOf("Check Flag", "If Flag", "IFF"), flagGroupLength = 3, endFlagCheckOpcode = 0x49) //IFF
        opcode(0x02, names = arrayOf("Set Game Parameter", "WAK"), argumentCount = 3) //WAK
        flagCheck(0x03, names = arrayOf("Check Game Parameter", "If Game Parameter", "IFW"), flagGroupLength = 3, endFlagCheckOpcode = 0x49) //IFW
        opcode(0x04, names = arrayOf("Switch", "When", "SWI"), argumentCount = 1) //SWI
        opcode(0x05, names = arrayOf("Case", "CAS"), argumentCount = 1) //CAS
        opcode(0x06, names = arrayOf("MPF"), argumentCount = 3) //MPF, Map Flag?
        opcode(0x07, names = arrayOf("SWP"), argumentCount = -1) //SPW
        opcode(0x08, names = arrayOf("Set Game Mode", "MOD"), argumentCount = 4)
        opcode(0x09, names = arrayOf("HUM"), argumentCount = 1) //HUM
        opcode(0x0A, names = arrayOf("Check for Variable", "CHK"), argumentCount = 1) //CHK, seems to check if a given flag/variable exists?
        opcode(0x0B, names = arrayOf("Truth Bullet", "KTB"), argumentCount = 2) //KTB
        opcode(0x0C, names = arrayOf("CLR"), argumentCount = -1) //CLR
        opcode(0x0D, names = arrayOf("RET"), argumentCount = -1) //RET
        opcode(0x0E, names = arrayOf("Camera Look", "KNM"), argumentCount = 5) //KNM
        opcode(0x0F, names = arrayOf("CAP"), argumentCount = -1) //CAP
        opcode(0x10, names = arrayOf("Script", "Load Script", "FIL"), argumentCount = 2) //FIL
        opcode(0x11, names = arrayOf("Stop Script", "End Script", "END"), argumentCount = 0) //END
        opcode(0x12, names = arrayOf("Run Subroutine Script", "SUB"), argumentCount = 2) //SUB
        opcode(0x13, names = arrayOf("Return", "RTN"), argumentCount = 0) //RTN
        opcode(0x14, names = arrayOf("Mark Label", "Set Label", "Label", "LAB"), argumentCount = 1) //LAB
        opcode(0x15, names = arrayOf("Go to Label", "Jump to Label", "Goto", "JMP"), argumentCount = 1) //JMP
        opcode(0x16, names = arrayOf("Movie", "MOV"), argumentCount = 2) //MOV
        opcode(0x17, names = arrayOf("Animation", "FLS"), argumentCount = 4) //FLS
        opcode(0x18, names = arrayOf("Animation Effect", "FLM"), argumentCount = 6) //FLM
        opcode(0x19, names = arrayOf("Voice", "VOI"), argumentCount = 2) //VOI
        opcode(0x1A, names = arrayOf("Music", "BGM"), argumentCount = 3) //BGM
        opcode(0x1B, names = arrayOf("Sound Effect", "SFX", "SE_"), argumentCount = 2) //SE_
        opcode(0x1C, names = arrayOf("Jingle", "JIN"), argumentCount = -1) //JIN
        opcode(0x1D, names = arrayOf("Speaker", "CHN"), argumentCount = 1) //CHN
        opcode(0x1E, names = arrayOf("Camera Vibration", "VIB"), argumentCount = 3) //VIB
        opcode(0x1F, names = arrayOf("Fade Screen", "FDS"), argumentCount = 3) //FDS
        opcode(0x20, names = arrayOf("FLA"), argumentCount = -1) //FLA
        opcode(0x21, names = arrayOf("Set Lighting Parameter", "LIG"), argumentCount = 3) //LIG
        opcode(0x22, names = arrayOf("Set Character Parameter", "CHR"), argumentCount = 5) //CHR
        opcode(0x23, names = arrayOf("Set Background Parameter", "BGD"), argumentCount = 4) //BGD
        opcode(0x24, names = arrayOf("Cutin", "CUT"), argumentCount = 2) //CUT, used for stuff like "Truth Bullet Get!" popups
        opcode(0x25, names = arrayOf("Character Vibration", "ADF"), argumentCount = 5) //ADF
        opcode(0x26, names = arrayOf("PAL"), argumentCount = -1) //PAL
        opcode(0x27, names = arrayOf("Load Map", "MAP"), argumentCount = 3) //MAP
        opcode(0x28, names = arrayOf("Load Object", "OBJ"), argumentCount = 3) //OBJ
        opcode(0x29, names = arrayOf("BUL"), argumentCount = 8) //BUL; unsure: that's a lot of variables
        opcode(0x2A, names = arrayOf("Cross Fade", "CRF"), argumentCount = -1) //CRF
        opcode(0x2B, names = arrayOf("Camera Command", "CAM"), argumentCount = 5) //CAM
        opcode(0x2C, names = arrayOf("Split Screen Mode", "KWM"), argumentCount = 1) //KWM
        opcode(0x2D, names = arrayOf("ARE"), argumentCount = 3) //ARE; area parameters?
        opcode(0x2E, names = arrayOf("Set Key Item", "KEY"), argumentCount = -1) //KEY, enables/disables "key" items
        opcode(0x2F, names = arrayOf("Set Text Window Parameter", "WIN"), argumentCount = 4) //WIN
        opcode(0x30, names = arrayOf("MSC"), argumentCount = -1) //MSC
        opcode(0x31, names = arrayOf("CSM"), argumentCount = -1) //CSM
        opcode(0x32, names = arrayOf("Post-Processing", "PST"), argumentCount = 5) //PST
        opcode(0x33, names = arrayOf("KNS"), argumentCount = 5) //KNS; numeric parmeters related to camera movement? (raw camera coords???)
        opcode(0x34, names = arrayOf("Set Font", "FON"), argumentCount = 2) //FON
        opcode(0x35, names = arrayOf("Load Background Object", "BGO"), argumentCount = 5) //BGO
        opcode(0x36, names = arrayOf("LOG"), argumentCount = 0) //LOG, edits text backlog
        opcode(0x37, names = arrayOf("SPT"), argumentCount = 1) //SPT, only used in Class Trials, always passed "non"?
        opcode(0x38, names = arrayOf("CDV"), argumentCount = 10) //CDV; unsure, that is an awful lot of variables
        opcode(0x39, names = arrayOf("Size Modifier", "SZM"), argumentCount = 4) //SZM, only used in Class Trials?
        opcode(0x3A, names = arrayOf("PVI"), argumentCount = 1) //PVI, Class Trial Chapter? Pre-trial intermissions?
        opcode(0x3B, names = arrayOf("Give EXP", "EXP"), argumentCount = 1) //EXP
        opcode(0x3C, names = arrayOf("MTA"), argumentCount = 1) //MTA, only used in Class Trials (), usually passed "non"?
        opcode(0x3D, names = arrayOf("Move Object to Position", "MVP"), argumentCount = -1) //MVP
        opcode(0x3E, names = arrayOf("Create Position", "POS"), argumentCount = 5) //POS
        opcode(0x3F, names = arrayOf("Program World Character Icon"), argumentCount = -1) //ICO
        opcode(0x40, names = arrayOf("Set Exisal AI Parameters", "EAI"), argumentCount = 10) //EAI
        opcode(0x41, names = arrayOf("Set Object Collision", "COL"), argumentCount = -1) //COL
        opcode(0x42, names = arrayOf("Camera Follow Path", "CFP"), argumentCount = -1) //CFP
        opcode(0x43, names = arrayOf("Set Text Color", "CLT="), argumentCount = -1) //CLT=; never used as an opcode?
        opcode(0x44, names = arrayOf("R="), argumentCount = -1) //R=; never used as an opcode?
        opcode(0x45, names = arrayOf("Display Gamepad Symbol", "PAD="), argumentCount = -1) //PAD=; never used as an opcode?
        opcode(0x46, names = arrayOf("Text", "LOC"), argumentCount = 1) //LOC
        opcode(0x47, names = arrayOf("Wait for Input", "BTN"), argumentCount = 0)
        opcode(0x48, names = arrayOf("ENT"), argumentCount = -1) //ENT
        opcode(0x49, names = arrayOf("End Flag Check", "End If", "CED"), argumentCount = 0) //CED, called immediately after all variable checks
        opcode(0x4A, names = arrayOf("Mark Sublabel", "Sublabel", "LBN"), argumentCount = 1) //LBN
        opcode(0x4B, names = arrayOf("Jump to Sublabel", "Goto Sublabel", "JMN"), argumentCount = 1) //JMN

        fromList(customOpcodes)
    }
    override val wrdOpcodeCommandType: OpcodeCommandTypeMap<EnumWordScriptCommand> = buildOpcodeCommandTypes {
        //Regex to parse Captain's table:
        //\{(0x[0-9A-F]{2}), ".{0,3}=?", \{\}, \{([0-3]?(?: ?, ?[0-3])*)\}\},?
        //this[$1] = intArrayOf($2)
        opcode(0x00, types = *intArrayOf(0, 0))              // Set Flag
        opcode(0x01) { EnumWordScriptCommand.PARAMETER }     // If Flag
        opcode(0x02, types = *intArrayOf(0, 0, 0))           // Wake? Work? (Seems to be used to configure game engine parameters)
        opcode(0x03) { EnumWordScriptCommand.PARAMETER }     // If WAK
        opcode(0x04, types = *intArrayOf(0))                 // Begin switch statement
        opcode(0x05, types = *intArrayOf(1))                 // Switch Case
        opcode(0x06, types = *intArrayOf(0, 0, 0))           // Map Flag?
        opcode(0x07, types = *intArrayOf())
        opcode(0x08, types = *intArrayOf(0, 0, 0, 0))        // Set Modifier (Also used to configure game engine parameters)
        opcode(0x09, types = *intArrayOf(0))                 // Human? Seems to be used to initialize "interactable" objects in a map?
        opcode(0x0A, types = *intArrayOf(0))                 // Check?
        opcode(0x0B, types = *intArrayOf(0, 0))              // Kotodama?
        opcode(0x0C, types = *intArrayOf())                  // Clear?
        opcode(0x0D, types = *intArrayOf())                  // Return? There's another command later which is definitely return, though...
        opcode(0x0E, types = *intArrayOf(0, 0, 0, 0, 0))     // Kinematics (camera movement)
        opcode(0x0F, types = *intArrayOf())                  // Camera Parameters?
        opcode(0x10, types = *intArrayOf(0, 0))              // Load Script File & jump to label
        opcode(0x11, types = *intArrayOf())                  // End of script or switch case
        opcode(0x12, types = *intArrayOf(0, 0))              // Jump to subroutine
        opcode(0x13, types = *intArrayOf())                  // Return (called inside subroutine)
        opcode(0x14, types = *intArrayOf(3))                 // Label number
        opcode(0x15, types = *intArrayOf(0))                 // Jump to label
        opcode(0x16, types = *intArrayOf(0, 0))              // Movie
        opcode(0x17, types = *intArrayOf(0, 0, 0, 0))        // Flash
        opcode(0x18, types = *intArrayOf(0, 0, 0, 0, 0, 0))  // Flash Modifier?
        opcode(0x19, types = *intArrayOf(0, 0))              // Play voice clip
        opcode(0x1A, types = *intArrayOf(0, 0, 0))           // Play BGM
        opcode(0x1B, types = *intArrayOf(0, 0))              // Play sound effect
        opcode(0x1C, types = *intArrayOf(0, 0))              // Play jingle
        opcode(0x1D, types = *intArrayOf(0))                 // Set active character ID (current person speaking)
        opcode(0x1E, types = *intArrayOf(0, 0, 0))           // Camera Vibration
        opcode(0x1F, types = *intArrayOf(0, 0, 0))           // Fade Screen
        opcode(0x20, types = *intArrayOf())
        opcode(0x21, types = *intArrayOf(0, 1, 0))           // Lighting Parameters
        opcode(0x22, types = *intArrayOf(0, 0, 0, 0, 0))     // Character Parameters
        opcode(0x23, types = *intArrayOf(0, 0, 0, 0))        // Background Parameters
        opcode(0x24, types = *intArrayOf(0, 0))              // Cutin (display image for things like Truth Bullets, etc.)
        opcode(0x25, types = *intArrayOf(0, 0, 0, 0, 0))     // Character Vibration?
        opcode(0x26, types = *intArrayOf())
        opcode(0x27, types = *intArrayOf(0, 0, 0))           // Load Map
        opcode(0x28, types = *intArrayOf(0, 0, 0))           // Load Object
        opcode(0x29, types = *intArrayOf(0, 0, 0, 0, 0, 0, 0, 0))
        opcode(0x2A, types = *intArrayOf(0, 0, 0, 0, 0, 0, 0))     // Cross Fade
        opcode(0x2B, types = *intArrayOf(0, 0, 0, 0, 0))     // Camera command
        opcode(0x2C, types = *intArrayOf(0))                 // Game/UI Mode
        opcode(0x2D, types = *intArrayOf(0, 0, 0))
        opcode(0x2E, types = *intArrayOf(0, 0))              // Enable/disable "key" items for unlocking areas
        opcode(0x2F, types = *intArrayOf(0, 0, 0, 0))        // Window parameters
        opcode(0x30, types = *intArrayOf())
        opcode(0x31, types = *intArrayOf())
        opcode(0x32, types = *intArrayOf(0, 0, 0, 0, 0))     // Post-Processing
        opcode(0x33, types = *intArrayOf(0, 1, 1, 1, 1))     // Kinematics Numeric parameters?
        opcode(0x34, types = *intArrayOf(1, 1))              // Set Font
        opcode(0x35, types = *intArrayOf(0, 0, 0, 0, 0))     // Load Background Object
        opcode(0x36, types = *intArrayOf())                  // Add next text to log (only used in class trials during nonstop debates)
        opcode(0x37, types = *intArrayOf(0))                 // Used only in Class Trial? Always set to "non"?
        opcode(0x38, types = *intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
        opcode(0x39, types = *intArrayOf(0, 0, 0, 0))        // Stand Position (Class Trial) (posX, posY, speed) (can be negative and floats)
        opcode(0x3A, types = *intArrayOf(0))                 // Class Trial Chapter? Pre-trial intermission?
        opcode(0x3B, types = *intArrayOf(0))                 // Give EXP
        opcode(0x3C, types = *intArrayOf(0))                 // Used only in Class Trial? Usually set to "non"?
        opcode(0x3D, types = *intArrayOf(0, 0, 0))           // Move object to its designated position?
        opcode(0x3E, types = *intArrayOf(0, 0, 0, 0, 0))     // Object/Exisal position
        opcode(0x3F, types = *intArrayOf(0, 0, 0, 0))           // Display a Program World character portrait
        opcode(0x40, types = *intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))  // Exisal AI
        opcode(0x41, types = *intArrayOf(0, 0, 0))           // Set object collision
        opcode(0x42, types = *intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)) // Camera Follow Path? Seems to make the camera move in some way
        opcode(0x43, types = *intArrayOf(0))                // Text modifier command
        opcode(0x44, types = *intArrayOf())
        opcode(0x45, types = *intArrayOf(0))                // Gamepad button symbol
        opcode(0x46, types = *intArrayOf(2))                 // Display text string
        opcode(0x47, types = *intArrayOf())                  // Wait for button press
        opcode(0x48, types = *intArrayOf())
        opcode(0x49, types = *intArrayOf())                  // Check End (Used after IFF and IFW commands)
        opcode(0x4A, types = *intArrayOf(1))                 // Local Branch Number (for branching case statements)
        opcode(0x4B, types = *intArrayOf(1))                  // Jump to Local Branch (for branching case statements)
    }

    override fun entryFor(opcode: Int, rawArguments: Array<WordScriptValue>): WrdEntry = when (opcode) {
        else -> UnknownWrdEntry(opcode, rawArguments, this)
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.DRv3() = DRv3(this)
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.UnsafeDRv3() = DRv3.unsafe(this)