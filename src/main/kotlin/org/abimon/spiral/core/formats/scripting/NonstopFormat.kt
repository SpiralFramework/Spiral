package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.text.JacksonFormat
import org.abimon.spiral.core.objects.scripting.NonstopDebate
import org.abimon.visi.io.DataSource
import java.io.OutputStream

object NonstopFormat: SpiralFormat {
    override val name: String = "Nonstop Debate"
    override val extension: String = "dat"
    override val conversions: Array<SpiralFormat> = arrayOf(JacksonFormat.YAML, JacksonFormat.JSON)

    override fun isFormat(source: DataSource): Boolean {
//        try {
//            return NonstopDebate(source).sections.isNotEmpty()
//        } catch (iea: IllegalArgumentException) {
//        }

        return false
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val debate = NonstopDebate(source)

        when(format) {
            is JacksonFormat -> {
                val debateMap: MutableMap<String, Any> = HashMap()

                debateMap["duration"] = debate.secondsForDebate
                debateMap["sections"] = debate.sections.map { section -> section.data.mapIndexed { index, data -> (if(index in SpiralData.nonstopOpCodes) SpiralData.nonstopOpCodes[index] else "0x${index.toString(16)}") to data }.toMap() }

                format.MAPPER.writeValue(output, debateMap)
            }
        }

        return true
    }
}