package org.abimon.spiral.core.formats

import org.abimon.visi.io.DataSource
import java.io.ByteArrayOutputStream
import java.io.OutputStream

interface SpiralFormat {
    val name: String
    val extension: String?
    val preferredConversions: Array<SpiralFormat>
        get() = Array(0, { UnknownFormat })

    fun isFormat(source: DataSource): Boolean
    fun canConvert(format: SpiralFormat): Boolean
    /**
     * Convert from this format to another
     */
    fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        if (!canConvert(format))
            throw IllegalArgumentException("Cannot convert to $format")
        if (!isFormat(source))
            throw IllegalArgumentException("${source.location} does not conform to the $name format")
    }

    fun convertFrom(format: SpiralFormat, source: DataSource, output: OutputStream) = format.convert(this, source, output)

    fun convertToBytes(format: SpiralFormat, source: DataSource): ByteArray {
        val baos = ByteArrayOutputStream()
        convert(format, source, baos)
        return baos.toByteArray()
    }

    object UnknownFormat : SpiralFormat {
        override val name = "Unknown"
        override val extension = null

        override fun isFormat(source: DataSource): Boolean = false

        override fun canConvert(format: SpiralFormat): Boolean = false

    }

    object BinaryFormat : SpiralFormat {
        override val name = "Binary"
        override val extension = null

        override fun isFormat(source: DataSource): Boolean = true
        override fun canConvert(format: SpiralFormat): Boolean = false //Can't inherently convert to other formats
    }
}