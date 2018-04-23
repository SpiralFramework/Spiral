package org.abimon.spiral.core.formats

import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.utils.and
import java.io.InputStream
import java.io.OutputStream

interface SpiralFormat {
    val name: String
    val extension: String?
    val conversions: Array<SpiralFormat>

    fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean
    fun isFormatWithConfidence(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Pair<Boolean, Double> = isFormat(game, name, context, dataSource) to 1.0
    fun canConvert(game: DRGame?, format: SpiralFormat): Boolean = format in conversions || canConvertViaOverride(game, format)
    fun canConvertViaOverride(game: DRGame?, format: SpiralFormat): Boolean = OVERRIDING_CONVERSIONS.containsKey(game to this and format)
    /**
     * Convert from this format to another
     */
    fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (!isFormat(game, name, context, dataSource))
            throw IllegalArgumentException("$name does not conform to the ${this.name} format")

        if (canConvertViaOverride(game, format)) {
            for(conversion in SpiralFormat[game to this and format]) {
                if (conversion.invoke(game, this, format, name, context, dataSource, output, params))
                    return true
            }
        }

        if (!canConvert(game, format))
            throw IllegalArgumentException("Cannot convert to $format")

        return false
    }

    fun convertFrom(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean = format.convert(game, this, name, context, dataSource, output, params)

    object UnknownFormat : SpiralFormat {
        override val name = "Unknown"
        override val extension = null
        override val conversions: Array<SpiralFormat> = emptyArray()

        override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean = false

    }

    object BinaryFormat : SpiralFormat {
        override val name = "Binary"
        override val extension = null
        override val conversions: Array<SpiralFormat> = emptyArray()

        override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean = true
    }

    companion object {
        val OVERRIDING_CONVERSIONS: MutableMap<Triple<DRGame?, SpiralFormat, SpiralFormat>, MutableList<(DRGame?, SpiralFormat, SpiralFormat, String?, (String) -> (() -> InputStream)?, () -> InputStream, OutputStream, Map<String, Any?>) -> Boolean>> = HashMap()

        operator fun get(triple: Triple<DRGame?, SpiralFormat, SpiralFormat>): List<(DRGame?, SpiralFormat, SpiralFormat, String?, (String) -> (() -> InputStream)?, () -> InputStream, OutputStream, Map<String, Any?>) -> Boolean> = OVERRIDING_CONVERSIONS[triple] ?: emptyList()
        operator fun set(triple: Triple<DRGame?, SpiralFormat, SpiralFormat>, func: (DRGame?, SpiralFormat, SpiralFormat, String?, (String) -> (() -> InputStream)?, () -> InputStream, OutputStream, Map<String, Any?>) -> Boolean) {
            if(!OVERRIDING_CONVERSIONS.containsKey(triple))
                OVERRIDING_CONVERSIONS[triple] = ArrayList()

            OVERRIDING_CONVERSIONS[triple]!!.add(func)
        }
    }
}