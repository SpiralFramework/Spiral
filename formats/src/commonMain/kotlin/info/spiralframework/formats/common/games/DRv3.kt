package info.spiralframework.formats.common.games

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.flow.readBytes
import info.spiralframework.base.common.io.useInputFlow
import info.spiralframework.formats.common.OpcodeCommandTypeMap
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.data.EnumWordScriptCommand
import info.spiralframework.formats.common.data.buildOpcodeCommandTypes
import info.spiralframework.formats.common.data.buildScriptOpcodes
import info.spiralframework.formats.common.scripting.wrd.WrdEntry
import info.spiralframework.formats.common.withFormats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

open class DRv3(
        override val wrdCharacterNames: Map<String, String>,
        override val wrdCharacterIdentifiers: Map<String, String>,
        override val wrdColourCodes: Map<String, String>,
        override val wrdItemNames: Array<String>
) : DrGame, DrGame.WordScriptable {
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

//                val customOpcodeSource = loadResource("opcodes/dr2.json", Dr2::class)
//                val customOpcodes: List<JsonOpcode>
//                if (customOpcodeSource != null) {
//                    customOpcodes = Json.parse(JsonOpcode.serializer().list, requireNotNull(customOpcodeSource.useInputFlow { flow -> flow.readBytes() }).decodeToString())
//                } else {
//                    customOpcodes = emptyList()
//                }

                return DRv3(gameJson.character_names, gameJson.character_identifiers, gameJson.colour_codes, gameJson.item_names)
            }
        }
    }
    override val names: Array<String> = arrayOf("DRv3", "NDRv3", "V3", "Danganronpa V3: Killing Harmony", "New Danganronpa V3: Killing Harmony")
    override val identifier: String = "drv3"
    override val steamID: String = "567640"

    override val wrdOpcodeMap: OpcodeMap<WrdEntry> = buildScriptOpcodes {

    }
    override val wrdOpcodeCommandType: OpcodeCommandTypeMap<EnumWordScriptCommand> = buildOpcodeCommandTypes {

    }
}