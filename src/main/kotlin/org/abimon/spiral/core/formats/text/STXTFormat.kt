package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.text.STXT
import org.abimon.spiral.core.println
import org.abimon.spiral.util.InputStreamFuncDataSource
import java.io.InputStream
import java.io.OutputStream

object STXTFormat : SpiralFormat {
    override val name = "STXT"
    override val extension = "stx"
    override val conversions: Array<SpiralFormat> = arrayOf(TextFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            return STXT(InputStreamFuncDataSource(dataSource)).strings.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        }
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true

        val stxt = STXT(InputStreamFuncDataSource(dataSource))

        output.println("Language: ${stxt.lang}")
        stxt.strings.toSortedMap().forEach { id, str -> output.println("$id|$str") }

        return true
    }
}