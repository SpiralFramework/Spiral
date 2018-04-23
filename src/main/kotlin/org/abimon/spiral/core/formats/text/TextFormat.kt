package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.LINFormat
import org.abimon.spiral.core.formats.scripting.WRDFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.io.InputStream

object TextFormat: SpiralFormat {
    override val name: String = "Text"
    override val extension: String = "txt"
    override val conversions: Array<SpiralFormat> = emptyArray() //We should not be doing any automated conversions
    val manualConversions: Array<SpiralFormat> = arrayOf(ScriptTextFormat, SpiralTextFormat, LINFormat, WRDFormat, STXTFormat) //But we should allow manual conversions

    override fun canConvert(game: DRGame?, format: SpiralFormat): Boolean = format in manualConversions
    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean = true
}