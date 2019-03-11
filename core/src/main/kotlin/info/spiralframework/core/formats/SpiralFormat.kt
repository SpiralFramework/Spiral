package info.spiralframework.core.formats

import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.BLANK_DATA_CONTEXT
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import java.io.OutputStream
import java.util.*

interface SpiralFormat {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    val name: String
    //val extension: String?
}

/**
 * A Spiral format that supports reading from a source
 */
interface ReadableSpiralFormat<T>: SpiralFormat {
    /**
     * Specifies a preferred conversion format for files that match this format.
     * This is used primarily for Danganronpa formats to specify we should convert to a nicer, more usable format.
     * It should **not** be used in contexts where there is ambiguity about what format may be desired; thus, it should not be defined for regular formats to Danganronpa formats in mots cases.
     */
    fun preferredConversionFormat(): WritableSpiralFormat? = null

    /**
     * Attempts to identify the data source as an instance of [T]
     *
     * Formats are recommended to override this where possible.
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return A FormatResult containing either an optional with the value [T] or null, if the stream does not seem to match an object of type [T]
     */
    fun identify(name: String? = null, game: DRGame? = null, context: DataContext = BLANK_DATA_CONTEXT, source: DataSource): FormatResult<Optional<T>>
        = read(name, game, context, source).map { Optional.of(it) }

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    fun read(name: String? = null, game: DRGame? = null, context: DataContext = BLANK_DATA_CONTEXT, source: DataSource): FormatResult<T>
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
    fun write(name: String? = null, game: DRGame? = null, context: DataContext = BLANK_DATA_CONTEXT, data: Any, stream: OutputStream): FormatWriteResponse
}

sealed class FormatWriteResponse {
    object SUCCESS: FormatWriteResponse()
    object WRONG_FORMAT: FormatWriteResponse()
    class FAIL(val reason: Throwable? = null): FormatWriteResponse()
}

fun <T, F: FormatResult<T>> F.withFormat(format: SpiralFormat?): F {
    this.nullableFormat = format
    return this
}