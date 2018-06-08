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
                this[0x00] = "Set Flag" to 2 and ::UnknownEntry
                this[0x01] = null to -1 and ::UnknownEntry
                this[0x02] = "Check Flag" to 3 and ::UnknownEntry
                this[0x03] = null to 3 and ::UnknownEntry
                this[0x04] = null to 1 and ::UnknownEntry
                this[0x05] = null to 1 and ::UnknownEntry
                this[0x06] = null to 3 and ::UnknownEntry
                this[0x07] = null to -1 and ::UnknownEntry
                this[0x08] = null to 4 and ::UnknownEntry
                this[0x09] = null to 1 and ::UnknownEntry
                this[0x0A] = null to 1 and ::UnknownEntry
                this[0x0B] = null to 2 and ::UnknownEntry
                this[0x0C] = null to -1 and ::UnknownEntry
                this[0x0D] = null to -1 and ::UnknownEntry
                this[0x0E] = null to 5 and ::UnknownEntry
                this[0x0F] = null to -1 and ::UnknownEntry

                this[0x10] = arrayOf("Script", "Load Script") to 2 and ::UnknownEntry
                this[0x11] = arrayOf("Stop Script", "End Script") to 0 and ::UnknownEntry
                this[0x12] = "Run Script" to 2 and ::UnknownEntry
                this[0x13] = null to 0 and ::UnknownEntry
                this[0x14] = "Label" to 1 and ::UnknownEntry
                this[0x15] = null to 1 and ::UnknownEntry
                this[0x16] = null to -1 and ::UnknownEntry
                this[0x17] = "Animation" to 4 and ::UnknownEntry
                this[0x18] = null to 6 and ::UnknownEntry
                this[0x19] = "Voice" to 2 and ::UnknownEntry
                this[0x1A] = "Music" to 3 and ::UnknownEntry
                this[0x1B] = null to 2 and ::UnknownEntry
                this[0x1C] = null to -1 and ::UnknownEntry
                this[0x1D] = "Speaker" to 1 and ::UnknownEntry
                this[0x1E] = null to 3 and ::UnknownEntry
                this[0x1F] = null  to 3 and ::UnknownEntry
                this[0x20] = null to -1 and ::UnknownEntry
                this[0x21] = null to 3 and ::UnknownEntry
                this[0x22] = null to 5 and ::UnknownEntry
                this[0x23] = null to 4 and ::UnknownEntry
                this[0x24] = null to 2 and ::UnknownEntry
                this[0x25] = null to 5 and ::UnknownEntry
                this[0x26] = null to -1 and ::UnknownEntry
                this[0x27] = null to 3 and ::UnknownEntry
                this[0x28] = null to 3 and ::UnknownEntry
                this[0x29] = null to 8 and ::UnknownEntry //Unsure, that's a lot of variables
                this[0x2A] = null to -1 and ::UnknownEntry
                this[0x2B] = null to 5 and ::UnknownEntry
                this[0x2C] = null to 1 and ::UnknownEntry
                this[0x2D] = null to 3 and ::UnknownEntry
                this[0x2E] = null to -1 and ::UnknownEntry
                this[0x2F] = null to 4 and ::UnknownEntry
                this[0x30] = null to -1 and ::UnknownEntry
                this[0x31] = null to -1 and ::UnknownEntry
                this[0x32] = null to 5 and ::UnknownEntry
                this[0x33] = null to 5 and ::UnknownEntry
                this[0x34] = null to 2 and ::UnknownEntry
                this[0x35] = null to 5 and ::UnknownEntry
                this[0x36] = null to 0 and ::UnknownEntry
                this[0x37] = null to 1 and ::UnknownEntry
                this[0x38] = null to 10 and ::UnknownEntry //Unsure, that is an awful lot of variables
                this[0x39] = null to 4 and ::UnknownEntry
                this[0x3A] = null to 1 and ::UnknownEntry
                this[0x3B] = null to 1 and ::UnknownEntry
                this[0x3C] = null to 1 and ::UnknownEntry
                this[0x3D] = null to -1 and ::UnknownEntry
                this[0x3E] = null to 5 and ::UnknownEntry
                this[0x3F] = null to -1 and ::UnknownEntry
                this[0x40] = null to 5 and ::UnknownEntry
                this[0x41] = null to -1 and ::UnknownEntry
                this[0x42] = null to -1 and ::UnknownEntry
                this[0x43] = null to -1 and ::UnknownEntry
                this[0x44] = null to -1 and ::UnknownEntry
                this[0x45] = null to -1 and ::UnknownEntry
                this[0x46] = "Test" to 1 and ::UnknownEntry
                this[0x47] = "Wait For Input" to 0 and ::UnknownEntry
                this[0x48] = null to -1 and ::UnknownEntry
                this[0x49] = null to 0 and ::UnknownEntry //Possibly Wait Frame
                this[0x4A] = null to 1 and ::UnknownEntry
                this[0x4B] = null to 1 and ::UnknownEntry
                this[0x4C] = null to -1 and ::UnknownEntry
                this[0x4D] = null to -1 and ::UnknownEntry
                this[0x4E] = null to -1 and ::UnknownEntry
                this[0x4F] = null to -1 and ::UnknownEntry
                this[0x50] = null to -1 and ::UnknownEntry
                this[0x51] = null to -1 and ::UnknownEntry
                this[0x52] = null to -1 and ::UnknownEntry
                this[0x53] = "Speaker" to 1 and ::UnknownEntry

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
}