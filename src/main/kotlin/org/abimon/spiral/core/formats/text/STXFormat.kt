package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.OpenSpiralLanguageFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.text.STXT
import org.abimon.spiral.core.println
import java.io.InputStream
import java.io.OutputStream

object STXFormat : SpiralFormat {
    override val name = "STX"
    override val extension = "stx"
    override val conversions: Array<SpiralFormat> = arrayOf(OpenSpiralLanguageFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            return STXT(dataSource).strings.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        }
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true

        val stx = STXT(dataSource)

        output.println("OSL Script")
        output.println("Context: STX")
        output.println("Language: ${stx.lang.name}")

        stx.strings.toSortedMap().forEach { id, str -> output.println("Word String (ID is $id): ${str.replace("\n", "\\n")}") }

        return true
    }
}