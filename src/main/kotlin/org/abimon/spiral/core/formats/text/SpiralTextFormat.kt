package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.SpiralDrill
import org.abimon.spiral.core.drills.DrillHead
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.LINFormat
import org.abimon.spiral.core.objects.scripting.CustomLin
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.make
import java.io.OutputStream

object SpiralTextFormat : SpiralFormat {
    override val name = "SPIRAL Text"
    override val extension = "stxt"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat)

    override fun isFormat(source: DataSource): Boolean {
        val result = SpiralDrill.stxtRunner.run(String(source.data, Charsets.UTF_8))

        return !result.hasErrors() && !result.valueStack.isEmpty
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        when (format) {
            is LINFormat -> {
                val lin = make<CustomLin> {
                    SpiralDrill.stxtRunner.run(String(source.data, Charsets.UTF_8)).valueStack.forEach { value ->
                        if (value is List<*>) (value[0] as DrillHead).formScripts(value.subList(1, value.size).filterNotNull().toTypedArray()).forEach { scriptEntry -> entry(scriptEntry) }
                    }
                }

                lin.compile(output)
            }
        }

        return true
    }
}