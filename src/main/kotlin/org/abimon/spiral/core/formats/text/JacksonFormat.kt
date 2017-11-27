package org.abimon.spiral.core.formats.text

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.NonstopFormat
import org.abimon.visi.io.DataSource

abstract class JacksonFormat: SpiralFormat {
    override val conversions: Array<SpiralFormat> = emptyArray() //We should not be doing any automated conversions
    val manualConversions: Array<SpiralFormat> = arrayOf(NonstopFormat) //But we should allow manual conversions

    override fun canConvert(format: SpiralFormat): Boolean = format in manualConversions
    abstract val MAPPER: ObjectMapper

    override fun isFormat(source: DataSource): Boolean {
        try {
            source.use { stream -> MAPPER.readValue(stream, Map::class.java) }
            return true
        } catch (json: JsonParseException) {
        } catch (json: JsonMappingException) {
        }

        try {
            source.use { stream -> MAPPER.readValue(stream, List::class.java) }
            return true
        } catch (json: JsonParseException) {
        } catch (json: JsonMappingException) {
        }

        return false
    }
}