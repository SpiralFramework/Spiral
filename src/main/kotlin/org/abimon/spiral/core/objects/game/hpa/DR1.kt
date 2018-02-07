package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.objects.scripting.lin.FormatEntry
import org.abimon.spiral.core.objects.scripting.lin.TextCountEntry
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.abimon.spiral.core.utils.*

object DR1 : HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> =
            DataMapper.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("pak/dr1.json"))?.mapValues { (_, value) ->
                ((value as? List<*>)?.asIterable()
                        ?: (value as? Array<*>)?.asIterable())?.mapNotNull { str -> str as? String }?.toTypedArray()
                        ?: emptyArray()
            } ?: emptyMap()

    override val opCodes: OpCodeMap =
            OpCodeHashMap().apply {
                this[0x00] = "Text Count" to 2 and ::TextCountEntry
                this[0x01] = null to 3 and ::UnknownEntry
                this[0x02] = "Text" to 2 and ::TextEntry
                this[0x03] = "Format" to 1 and ::FormatEntry
                this[0x04] = "Filter" to 4 and ::UnknownEntry
                this[0x05] = "Movie" to 2 and ::UnknownEntry
                this[0x06] = "Animation" to 8 and ::UnknownEntry
                this[0x07] = null to -1 and ::UnknownEntry
                this[0x08] = "Voice Line" to 5 and ::UnknownEntry
                this[0x09] = arrayOf("Music", "BGM") to 3 and ::UnknownEntry
                this[0x0A] = "SFX A" to 3 and ::UnknownEntry
                this[0x0B] = "SFX B" to 2 and ::UnknownEntry
                this[0x0C] = "Truth Bullet" to 2 and ::UnknownEntry
                this[0x0D] = null to 3 and ::UnknownEntry
                this[0x0E] = null to 2 and ::UnknownEntry
                this[0x0F] = "Set Title" to 3 and ::UnknownEntry
                this[0x10] = "Set Report Info" to 3 and ::UnknownEntry
                this[0x11] = null to 4 and ::UnknownEntry
                this[0x12] = null to -1 and ::UnknownEntry
                this[0x13] = null to -1 and ::UnknownEntry
                this[0x14] = "Trial Camera" to 3 and ::UnknownEntry
                this[0x15] = "Load Map" to 3 and ::UnknownEntry
                this[0x16] = null to -1 and ::UnknownEntry
                this[0x17] = null to -1 and ::UnknownEntry
                this[0x18] = null to -1 and ::UnknownEntry
                this[0x19] = arrayOf("Script", "Load Script") to 3 and ::UnknownEntry
                this[0x1A] = arrayOf("Stop Script", "End Script") to 0 and ::UnknownEntry
                this[0x1B] = "Run Script" to 3 and ::UnknownEntry
                this[0x1C] = null to 0 and ::UnknownEntry
                this[0x1D] = null to -1 and ::UnknownEntry
                this[0x1E] = "Sprite" to 5 and ::UnknownEntry
                this[0x1F] = null to 7 and ::UnknownEntry
                this[0x20] = null to 5 and ::UnknownEntry
                this[0x21] = "Speaker" to 1 and ::UnknownEntry
                this[0x22] = null to 3 and ::UnknownEntry
                this[0x23] = null to 5 and ::UnknownEntry
                this[0x24] = null to -1 and ::UnknownEntry
                this[0x25] = "Change UI" to 2 and ::UnknownEntry
                this[0x26] = "Set Flag" to 3 and ::UnknownEntry
                this[0x27] = "CVheck Character" to 1 and ::UnknownEntry
                this[0x28] = null to -1 and ::UnknownEntry
                this[0x29] = "Check Object" to 1 and ::UnknownEntry
                this[0x2A] = "Set Label" to 2 and ::UnknownEntry
                this[0x2B] = "Choice" to 1 and ::UnknownEntry
                this[0x2C] = null to 2 and ::UnknownEntry
                this[0x2D] = null to -1 and ::UnknownEntry
                this[0x2E] = null to 2 and ::UnknownEntry
                this[0x2F] = null to 10 and ::UnknownEntry
                this[0x30] = "Show Background" to 3 and ::UnknownEntry
                this[0x31] = null to -1 and ::UnknownEntry
                this[0x32] = null to 1 and ::UnknownEntry
                this[0x33] = null to 4 and ::UnknownEntry
                this[0x34] = arrayOf("Go To Label", "Goto Label", "Goto") to 2 and ::UnknownEntry
                this[0x35] = "Check Flag A" to -1 and ::UnknownEntry
                this[0x36] = "Check Flag B" to -1 and ::UnknownEntry
                this[0x37] = null to -1 and ::UnknownEntry
                this[0x38] = null to -1 and ::UnknownEntry
                this[0x39] = null to 5 and ::UnknownEntry
                this[0x3A] = "Wait For Input" to 0 and ::UnknownEntry
                this[0x3B] = "Wait Frame" to 0 and ::UnknownEntry
                this[0x3C] = "End Flag Check" to 0 and ::UnknownEntry
            }
}