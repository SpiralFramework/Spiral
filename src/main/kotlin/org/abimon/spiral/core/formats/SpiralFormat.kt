package org.abimon.spiral.core.formats

import org.abimon.visi.io.DataSource
import java.io.ByteArrayOutputStream
import java.io.OutputStream

interface SpiralFormat {
    val name: String
    val extension: String?
    val conversions: Array<SpiralFormat>

    fun isFormat(source: DataSource): Boolean
    fun canConvert(format: SpiralFormat): Boolean = format in conversions || canConvertViaOverride(format)
    fun canConvertViaOverride(format: SpiralFormat): Boolean = OVERRIDING_CONVERSIONS.containsKey(this to format)
    /**
     * Convert from this format to another
     */
    fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        if (!canConvert(format))
            throw IllegalArgumentException("Cannot convert to $format")
        if (!isFormat(source))
            throw IllegalArgumentException("${source.location} does not conform to the $name format")

        if (canConvertViaOverride(format)) {
            if (OVERRIDING_CONVERSIONS[this to format]?.invoke(this, format, source, output, params) == true)
                return
        }
    }

    fun convertFrom(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) = format.convert(this, source, output, params)

    fun convertToBytes(format: SpiralFormat, source: DataSource, params: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        convert(format, source, baos, params)
        return baos.toByteArray()
    }

    object UnknownFormat : SpiralFormat {
        override val name = "Unknown"
        override val extension = null
        override val conversions: Array<SpiralFormat> = emptyArray()

        override fun isFormat(source: DataSource): Boolean = false

    }

    object BinaryFormat : SpiralFormat {
        override val name = "Binary"
        override val extension = null
        override val conversions: Array<SpiralFormat> = emptyArray()

        override fun isFormat(source: DataSource): Boolean = true
    }

    companion object {
        val OVERRIDING_CONVERSIONS: MutableMap<Pair<SpiralFormat, SpiralFormat>, (SpiralFormat, SpiralFormat, DataSource, OutputStream, Map<String, Any?>) -> Boolean> = HashMap()
    }
}