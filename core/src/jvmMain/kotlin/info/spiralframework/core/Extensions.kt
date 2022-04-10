package info.spiralframework.core
//
//import com.fasterxml.jackson.core.JsonParseException
//import com.fasterxml.jackson.core.JsonProcessingException
//import com.fasterxml.jackson.databind.JsonMappingException
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import org.yaml.snakeyaml.error.YAMLException
import dev.brella.kornea.base.common.getOrElseRun
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.events.*
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.*
import info.spiralframework.core.common.formats.compression.CrilaylaCompressionFormat
import info.spiralframework.core.common.formats.compression.DRVitaFormat
import info.spiralframework.core.common.formats.compression.DRv3CompressionFormat
import info.spiralframework.core.common.formats.compression.SpcCompressionFormat
import java.io.Closeable
import kotlin.math.abs

public object UserAgents {
    public const val DEFAULT: String =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:64.0) Gecko/20100101 Firefox/64.0"
}

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this [Closeable] resource.
 * @return the result of [block] function invoked on this resource.
 */
public inline fun <T : Closeable?, R> (() -> T).use(block: (T) -> R): R {
    var exception: Throwable? = null
    val stream = this()
    try {
        return block(stream)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            stream == null -> {
            }
            exception == null -> stream.close()
            else ->
                try {
                    stream.close()
                } catch (closeException: Throwable) {
                    exception.addSuppressed(closeException)
                }
        }
    }
}

public typealias ReadableCompressionFormat = ReadableSpiralFormat<DataSource<*>>

public val COMPRESSION_FORMATS: Array<ReadableCompressionFormat> =
    arrayOf(CrilaylaCompressionFormat, DRVitaFormat, SpcCompressionFormat, DRv3CompressionFormat)

@Suppress("UNCHECKED_CAST")
public suspend fun SpiralContext.decompress(dataSource: DataSource<*>): Pair<DataSource<*>, List<ReadableCompressionFormat>?> {
    val result = COMPRESSION_FORMATS.map { format -> format.identify(source = dataSource, context = this) }
        .filterSuccesses()
        .maxByConfidenceOrNull()
        ?: return Pair(dataSource, null)

    val format = result.format as ReadableCompressionFormat

    val resultDataSource = result.value.getOrElseRun {
        format.read(source = dataSource, context = this)
            .getOrBreak { return Pair(dataSource, null) }
            .value
    }

    val (decompressed, list) = decompress(resultDataSource)


    if (list is MutableList<ReadableCompressionFormat>) {
        list.add(0, result.format)
        return Pair(decompressed, list)
    } else if (list != null) { //compiler flags this as unnecessary which is... weird
        val mutList = list.toMutableList()
        mutList.add(0, result.format)
        return Pair(decompressed, list)
    } else {
        return Pair(decompressed, listOf(result.format))
    }
}

public inline fun <T> Iterable<KorneaResult<T>>.filterSuccesses(): List<T> =
    filterSuccessesTo(ArrayList())

public inline fun <T, C: MutableCollection<T>> Iterable<KorneaResult<T>>.filterSuccessesTo(collection: C): C {
    forEach { result -> result.doOnSuccess(collection::add) }

    return collection
}

public inline fun <T, F> Iterable<FormatSuccess<T, F>>.sortedByConfidence(): List<FormatSuccess<T, F>> =
    sortedBy(FormatSuccess<T, F>::confidence)

public inline fun <T, F> Iterable<FormatSuccess<T, F>>.maxByConfidenceOrNull(): FormatSuccess<T, F>? =
    maxByOrNull(FormatSuccess<T, F>::confidence)

//inline fun <reified T : Any> ObjectMapper.tryReadValue(src: ByteArray): T? {
//    try {
//        return this.readValue(src)
//    } catch (jsonProcessing: JsonProcessingException) {
//    } catch (jsonMapping: JsonMappingException) {
//    } catch (jsonParsing: JsonParseException) {
//    } catch (yamlParsing: YAMLException) {
//    }
//
//    return null
//}
//
//inline fun <reified T : Any> ObjectMapper.tryReadValue(src: InputStream): T? {
//    try {
//        return this.readValue(src)
//    } catch (jsonProcessing: JsonProcessingException) {
//    } catch (jsonMapping: JsonMappingException) {
//    } catch (jsonParsing: JsonParseException) {
//    } catch (yamlParsing: YAMLException) {
//    }
//
//    return null
//}
//
//inline fun <reified T : Any> ObjectMapper.tryReadValue(src: File): T? {
//    try {
//        return this.readValue(src)
//    } catch (jsonProcessing: JsonProcessingException) {
//    } catch (jsonMapping: JsonMappingException) {
//    } catch (jsonParsing: JsonParseException) {
//    } catch (io: FileNotFoundException) {
//    } catch (yamlParsing: YAMLException) {
//    }
//
//    return null
//}

public suspend fun <T : CancellableSpiralEvent> SpiralEventBus.postCancellable(
    context: SpiralContext,
    event: T
): Boolean {
    context.post(event)

    return event.cancelled
}

public fun <T> T.identifySelf(): T = this

public fun <T : SpiralEventBus> T.installLoggingSubscriber(): T {
    register("Logging", SpiralEventPriority.HIGHEST) { event: SpiralEvent ->
        trace(
            "core.eventbus.logging.event",
            event
        )
    }

    return this
}


/**
 * Returns a list of all formats sorted against the name provided by the read context
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @sample samples.collections.Collections.Sorting.sortedBy
 */
public inline fun <T : SpiralFormat> Iterable<T>.sortedAgainst(context: SpiralProperties?): List<T> {
    return sortedWith(compareBy { format ->
        abs(
            format.extension?.compareTo(
                context[ISpiralProperty.FileName]?.substringAfterLast(
                    '.'
                ) ?: ""
            ) ?: -100
        )
    })
}

public inline fun <T, U, F, R : SpiralFormatResult<U, F>> Iterable<T>.mapFormatResults(
    threshold: Double = 0.99,
    transform: (T) -> R
): List<R> {
    return mapFormatResultsTo(threshold, ArrayList<R>(10), transform)
}

public inline fun <T, U, F, R : SpiralFormatResult<U, F>, C : MutableCollection<in R>> Iterable<T>.mapFormatResultsTo(
    threshold: Double = 0.99,
    destination: C,
    transform: (T) -> R
): C {
    for (item in this) {
        val transformed = transform(item)
        destination.add(transformed)

        val confidence = transformed.getOrNull()?.confidence
        if (confidence != null && confidence >= threshold) break
    }
    return destination
}