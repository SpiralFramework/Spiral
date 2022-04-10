package info.spiralframework.core.common.formats

import dev.brella.kornea.base.common.*
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.*

public interface SpiralFormat {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    public val name: String

    /**
     * The usual extension for this format. Some formats don't have a proper extension, so this can be nullable
     */
    public val extension: String?

    public companion object : ISpiralProperty.PropertyKey<SpiralFormat>,
        KorneaTypeChecker<SpiralFormat> by KorneaTypeChecker.ClassBased() {
        public const val DEFAULT_EXTENSION: String = "dat"

        override val name: String = "SpiralFormat"

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }
}

/**
 * A Spiral format that supports reading from a source
 */
public interface ReadableSpiralFormat<out T> : SpiralFormat {
    public companion object : ISpiralProperty.PropertyKey<ReadableSpiralFormat<*>>,
        KorneaTypeChecker<ReadableSpiralFormat<*>> by KorneaTypeChecker.ClassBased() {
        override val name: String = "SpiralFormat"

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }

    public fun requiredPropertiesForConversionSelection(
        context: SpiralContext,
        properties: SpiralProperties?
    ): List<ISpiralProperty.PropertyKey<*>> = emptyList()

    /**
     * Specifies a preferred conversion format for files that match this format.
     * This is used primarily for Danganronpa formats to specify we should convert to a nicer, more usable format.
     * It should **not** be used in contexts where there is ambiguity about what format may be desired; thus, it should not be defined for regular formats to Danganronpa formats in most cases.
     */
    public fun preferredConversionFormat(context: SpiralContext, properties: SpiralProperties?): WritableSpiralFormat<*>? =
        null

    /**
     * Should we attempt to automatically identify this file?
     * Return false for text based formats in particular
     */
    public fun shouldAutoIdentify(): Boolean = true

    public fun requiredPropertiesForRead(
        context: SpiralContext,
        readContext: SpiralProperties?,
        data: Any
    ): List<ISpiralProperty.PropertyKey<*>> = emptyList()

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
    public suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties? = null,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<T> =
        read(context, readContext, source)
            .mapFormat(::Optional)

    /**
     * Attempts to read the data source as [T]
     *
     * @param readContext Reading context for this data
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    public suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties? = null,
        source: DataSource<*>
    ): SpiralFormatReturnResult<T>

    @Suppress("UNCHECKED_CAST")
    public fun <R> KorneaResult<R>.ensureFormatSuccess(confidence: Double): SpiralFormatResult<R, T> =
        map { value ->
            if (value is FormatSuccess<*, *>) {
                value as FormatSuccess<R, T>
            } else {
                FormatSuccess(value, this@ReadableSpiralFormat, confidence)
            }
        }

    @Suppress("UNCHECKED_CAST")
    public fun <R> KorneaResult<R>.ensureFormatSuccess(confidence: (R) -> Double): SpiralFormatResult<R, T> =
        map { value ->
            if (value is FormatSuccess<*, *>) {
                value as FormatSuccess<R, T>
            } else {
                FormatSuccess(value, this@ReadableSpiralFormat, confidence(value))
            }
        }

    @Suppress("UNCHECKED_CAST")
    public fun <R> KorneaResult<Optional<R>>.ensureOptionalFormatSuccess(confidence: (R) -> Double): SpiralFormatResult<Optional<R>, T> =
        map { value: Optional<R> ->
            value.doOnPresent { inner ->
                if (inner is FormatSuccess<*, *>)
                    return@map inner.map { Optional.of(it) } as FormatSuccess<Optional<R>, T>
            }

            FormatSuccess(value, this@ReadableSpiralFormat, value.map(confidence).getOrDefault(0.0))
        }

    @Suppress("UNCHECKED_CAST")
    public fun <R> KorneaResult<Optional<R>>.ensureOptionalFormatSuccess(ifMissing: Double, confidence: (R) -> Double): SpiralFormatResult<Optional<R>, T> =
        map { value: Optional<R> ->
            value.doOnPresent { inner ->
                if (inner is FormatSuccess<*, *>)
                    return@map inner.map { Optional.of(it) } as FormatSuccess<Optional<R>, T>
            }

            FormatSuccess(value, this@ReadableSpiralFormat, value.map(confidence).getOrDefault(ifMissing))
        }
}

public inline fun <F, R> ReadableSpiralFormat<F>.buildFormatSuccess(
    value: R,
    confidence: Double
): SpiralFormatResult<R, F> =
    KorneaResult.success(FormatSuccess(value, this, confidence))

/**
 * A Spiral format that supports writing to a stream
 */
public interface WritableSpiralFormat<out T> : SpiralFormat {
    /**
     * Does this format support writing [data]?
     *
     * @return If we are able to write [data] as this format
     */
    public fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean

    public fun requiredPropertiesForWrite(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any
    ): List<ISpiralProperty.PropertyKey<*>> = emptyList()

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
    public suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<T>
}

public interface WritableSpiralFormatBridge<out T> {
    public companion object : ISpiralProperty.PropertyKey<List<WritableSpiralFormatBridge<*>>>,
        KorneaTypeChecker<List<WritableSpiralFormatBridge<*>>> by KorneaTypeChecker.ClassBased() {
        override val name: String = "WritableSpiralFormatBridge"

        override fun hashCode(): Int = defaultHashCode()
        override fun equals(other: Any?): Boolean = defaultEquals(other)
    }

    /**
     * Does this format support writing [data]?
     *
     * @return If we are able to write [data] as this format
     */
    public fun supportsWritingAs(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        format: WritableSpiralFormat<*>,
        data: Any
    ): Boolean

    public fun requiredPropertiesForWritingAs(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        format: WritableSpiralFormat<*>,
        data: Any
    ): List<ISpiralProperty.PropertyKey<*>> = emptyList()

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
    public suspend fun writeAs(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        format: WritableSpiralFormat<*>,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<T>
}

public inline fun SpiralContext.bridgeFor(
    format: WritableSpiralFormat<*>,
    writeContext: SpiralProperties?,
    data: Any
): WritableSpiralFormatBridge<*>? =
    writeContext[WritableSpiralFormatBridge]?.firstOrNull { bridge ->
        bridge.supportsWritingAs(
            this,
            writeContext,
            format,
            data
        )
    }

public suspend inline fun <C> C.populateForConversionSelection(
    readingFormat: ReadableSpiralFormat<*>,
    context: SpiralProperties?
): SpiralProperties? where C : SpiralPropertyProvider, C : SpiralContext =
    readingFormat.requiredPropertiesForConversionSelection(this, context)
        .takeIf(List<*>::isNotEmpty)
        ?.let { populate(context, null, it) }