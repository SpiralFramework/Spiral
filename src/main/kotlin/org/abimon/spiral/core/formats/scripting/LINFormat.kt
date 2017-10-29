package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.text.SpiralTextFormat
import org.abimon.spiral.core.formats.text.TXTFormat
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.core.objects.scripting.Lin
import org.abimon.spiral.core.println
import org.abimon.visi.io.DataSource
import java.io.OutputStream

object LINFormat : SpiralFormat {
    override val name = "LIN"
    override val extension = "lin"
    override val conversions: Array<SpiralFormat> = arrayOf(TXTFormat, SpiralTextFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return Lin(source).entries.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        } catch(negative: NegativeArraySizeException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is TXTFormat || format is SpiralTextFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val dr1 = "${params["lin:dr1"] ?: true}".toBoolean()
        Lin(source, dr1).entries.forEach { entry ->
            if (entry is TextEntry)
                output.println("${(if (dr1) SpiralData.dr1OpCodes else SpiralData.dr2OpCodes)[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.text.replace("\n", "\\n")}")
            else
                output.println("${(if (dr1) SpiralData.dr1OpCodes else SpiralData.dr2OpCodes)[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.getRawArguments().joinToString()}")
        }

        return true
    }
}