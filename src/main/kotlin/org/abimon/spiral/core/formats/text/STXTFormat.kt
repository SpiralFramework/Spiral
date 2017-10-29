package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.text.STXT
import org.abimon.spiral.core.println
import org.abimon.visi.io.DataSource
import java.io.OutputStream

object STXTFormat : SpiralFormat {
    override val name = "STXT"
    override val extension = "stx"
    override val conversions: Array<SpiralFormat> = arrayOf(TXTFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return STXT(source).strings.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is TXTFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val stxt = STXT(source)

        output.println("Language: ${stxt.lang}")
        stxt.strings.toSortedMap().forEach { id, str -> output.println("$id|$str") }

        return true
    }
}