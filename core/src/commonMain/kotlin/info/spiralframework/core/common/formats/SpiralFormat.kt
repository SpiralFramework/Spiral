package info.spiralframework.core.common.formats

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.SpiralPropertyProvider
import info.spiralframework.base.common.properties.defaultEquals
import info.spiralframework.base.common.properties.defaultHashCode
import info.spiralframework.base.common.properties.get

interface SpiralFormat {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    val name: String

    /**
     * The usual extension for this format. Some formats don't have a proper extension, so this can be nullable
     */
    val extension: String?

    companion object : ISpiralProperty.PropertyKey<SpiralFormat>, KorneaTypeChecker<SpiralFormat> by KorneaTypeChecker.ClassBased() {
        const val DEFAULT_EXTENSION = "dat"

        override val name: String = "SpiralFormat"

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }
}

/**
 * A Spiral format that supports reading from a source
 */
interface ReadableSpiralFormat<out T> : SpiralFormat {
    companion object : ISpiralProperty.PropertyKey<ReadableSpiralFormat<*>>, KorneaTypeChecker<ReadableSpiralFormat<*>> by KorneaTypeChecker.ClassBased() {
        override val name: String = "SpiralFormat"

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }

    fun requiredPropertiesForConversionSelection(context: SpiralContext, properties: SpiralProperties?): List<ISpiralProperty.PropertyKey<*>> = emptyList()

    /**
     * Specifies a preferred conversion format for files that match this format.
     * This is used primarily for Danganronpa formats to specify we should convert to a nicer, more usable format.
     * It should **not** be used in contexts where there is ambiguity about what format may be desired; thus, it should not be defined for regular formats to Danganronpa formats in most cases.
     */
    fun preferredConversionFormat(context: SpiralContext, properties: SpiralProperties?): WritableSpiralFormat? = null

    /**
     * Should we attempt to automatically identify this file?
     * Return false for text based formats in particular
     */
    fun shouldAutoIdentify(): Boolean = true

    fun requiredPropertiesForRead(context: SpiralContext, readContext: SpiralProperties?, data: Any): List<ISpiralProperty.PropertyKey<*>> = emptyList()

    /**
     * Attempts to identify the data source as an instance of [T]
     *
     * Formats are recommended to override this where possible.
     *
     * @param readContext Reading context for this data
     * @param source A function that returns an input stream
     *
     * @return A FormatResult containing either an optional with the value [T] or null, if the stream does not seem to match an object of type [T]
     */
    suspend fun identify(context: SpiralContext, readContext: SpiralProperties? = null, source: DataSource<*>): KorneaResult<Optional<T>> =
        read(context, readContext, source).map<T, Optional<T>>(::Optional).buildFormatResult(1.0)

    /**
     * Attempts to read the data source as [T]
     *
     * @param readContext Reading context for this data
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    suspend fun read(context: SpiralContext, readContext: SpiralProperties? = null, source: DataSource<*>): KorneaResult<T>

    fun <R> KorneaResult<R>.buildFormatResult(confidence: Double): KorneaResult<R> =
        if (this is FormatResult<R, *>) this
        else flatMap { value -> KorneaResult.formatResult(value, this@ReadableSpiralFormat, confidence) }

    fun <R> KorneaResult<R>.buildFormatResult(confidence: (value: R) -> Double): KorneaResult<R> =
        if (this is FormatResult<R, *>) this
        else flatMap { value -> KorneaResult.formatResult(value, this@ReadableSpiralFormat, confidence(value)) }
}

inline fun <F, R> ReadableSpiralFormat<F>.buildFormatResult(value: R, confidence: Double): KorneaResult<R> =
    KorneaResult.formatResult(value, this, confidence)

/**
 * A Spiral format that supports writing to a stream
 */
interface WritableSpiralFormat : SpiralFormat {
    /**
     * Does this format support writing [data]?
     *
     * @return If we are able to write [data] as this format
     */
    fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean

    fun requiredPropertiesForWrite(context: SpiralContext, writeContext: SpiralProperties?, data: Any): List<ISpiralProperty.PropertyKey<*>> = emptyList()

    /**
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param dataContext Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse
}

interface WritableSpiralFormatBridge {
    companion object : ISpiralProperty.PropertyKey<List<WritableSpiralFormatBridge>>, KorneaTypeChecker<List<WritableSpiralFormatBridge>> by KorneaTypeChecker.ClassBased() {
        override val name: String = "WritableSpiralFormatBridge"

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }

    /**
     * Does this format support writing [data]?
     *
     * @return If we are able to write [data] as this format
     */
    fun supportsWritingAs(context: SpiralContext, writeContext: SpiralProperties?, format: WritableSpiralFormat, data: Any): Boolean

    fun requiredPropertiesForWritingAs(context: SpiralContext, writeContext: SpiralProperties?, format: WritableSpiralFormat, data: Any): List<ISpiralProperty.PropertyKey<*>> = emptyList()

    /**
     * Writes [data] to [stream] as [format]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param dataContext Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    suspend fun writeAs(context: SpiralContext, writeContext: SpiralProperties?, format: WritableSpiralFormat, data: Any, flow: OutputFlow): FormatWriteResponse
}

inline fun SpiralContext.bridgeFor(format: WritableSpiralFormat, writeContext: SpiralProperties?, data: Any): WritableSpiralFormatBridge? =
    writeContext[WritableSpiralFormatBridge]?.firstOrNull { bridge -> bridge.supportsWritingAs(this, writeContext, format, data) }

suspend inline fun <C> C.populateForConversionSelection(readingFormat: ReadableSpiralFormat<*>, context: SpiralProperties?): SpiralProperties? where C : SpiralPropertyProvider, C : SpiralContext =
    readingFormat.requiredPropertiesForConversionSelection(this, context)
        .takeIf(List<*>::isNotEmpty)
        ?.let { populate(context, null, it) }

sealed class FormatWriteResponse {
    object SUCCESS : FormatWriteResponse()
    object WRONG_FORMAT : FormatWriteResponse()

    data class MISSING_PROPERTY(val property: ISpiralProperty.PropertyKey<*>) : FormatWriteResponse()

    //TODO: Replace this with a result
    class FAIL(val reason: Throwable) : FormatWriteResponse() {
        constructor(context: SpiralContext) : this(Throwable(context.localise("gurren.errors.no_reason")))
    }
}

public inline fun <reified R> Iterable<*>.filterIsFormatResult(): List<FormatResult<R, R>> =
    filterIsInstance<FormatResult<R, R>>()

public inline fun <reified R> Iterable<*>.filterIsIdentifyFormatResultOrNull(): List<FormatResult<Optional<R>, R>> =
    filterIsInstance<FormatResult<Optional<R>, R>>()

@Suppress("UNCHECKED_CAST")
public inline fun <reified R> KorneaResult<*>.filterIsFormatResult(): KorneaResult<FormatResult<R, R>> =
    if (this is FormatResult<*, *>) KorneaResult.success(this as FormatResult<R, R>, null) else KorneaResult.empty()

@Suppress("UNCHECKED_CAST")
public inline fun <reified R> KorneaResult<*>.filterIsIdentifyFormatResultOrNull(): FormatResult<Optional<R>, R>? =
    if (this is FormatResult<*, *>) this as FormatResult<Optional<R>, R> else null

@Suppress("UNCHECKED_CAST")
public inline fun <reified R> KorneaResult<*>.filterIsIdentifyFormatResult(): Pair<FormatResult<Optional<R>, R>?, KorneaResult<*>?> =
    if (this is FormatResult<*, *>) Pair(this as FormatResult<Optional<R>, R>, null) else Pair(null, this)