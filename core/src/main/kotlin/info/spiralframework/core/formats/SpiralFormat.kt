package info.spiralframework.core.formats

import info.spiralframework.core.BLANK_DATA_CONTEXT
import info.spiralframework.core.DataContext
import info.spiralframework.core.DataSource
import info.spiralframework.core.FormatChance
import org.abimon.spiral.core.objects.game.DRGame
import java.io.OutputStream

interface SpiralFormat

/**
 * A Spiral format that supports reading from a source
 */
interface ReadableSpiralFormat<T>: SpiralFormat {
    /**
     * Check if [source] matches this format.
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a pair for if
     */
    fun isFormat(name: String? = null, game: DRGame? = null, context: DataContext = BLANK_DATA_CONTEXT, source: DataSource): FormatChance

    /**
     * Reads the data source as an object of type [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return an object of type [T], or null if the stream isn't a valid instance of this format
     */
    fun read(name: String? = null, game: DRGame? = null, context: DataContext = BLANK_DATA_CONTEXT, source: DataSource): T?
}

/**
 * A Spiral format that supports writing to a stream
 */
interface WritableSpiralFormat: SpiralFormat {
    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    fun supportsWriting(data: Any): Boolean

    /**
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    fun write(name: String? = null, game: DRGame? = null, context: DataContext = BLANK_DATA_CONTEXT, data: Any, stream: OutputStream): EnumFormatWriteResponse
}

enum class EnumFormatWriteResponse {
    SUCCESS,
    WRONG_FORMAT,
    FAIL
}