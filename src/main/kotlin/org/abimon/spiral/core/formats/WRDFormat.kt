package org.abimon.spiral.core.formats

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.objects.WRD
import org.abimon.spiral.core.println
import org.abimon.visi.io.DataSource
import java.io.OutputStream

object WRDFormat : SpiralFormat {
    override val name = "WRD"
    override val extension = "wrd"
    override val conversions: Array<SpiralFormat> = arrayOf(TXTFormat, SpiralTextFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return WRD(source).entries.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is TXTFormat || format is SpiralTextFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        WRD(source).entries.forEach { entry ->
//            if (entry is TextEntry)
//                output.println("${SpiralData.drv3OpCodes[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.text.replace("\n", "\\n")}")
//            else
            output.println("${SpiralData.drv3OpCodes[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.getRawArguments().joinToString()}")
        }

        return true
    }
}