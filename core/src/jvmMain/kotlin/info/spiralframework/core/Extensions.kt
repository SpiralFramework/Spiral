package info.spiralframework.core

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.events.*
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormat
import info.spiralframework.core.common.formats.compression.CrilaylaCompressionFormat
import info.spiralframework.core.common.formats.compression.DRVitaFormat
import info.spiralframework.core.common.formats.compression.DRv3CompressionFormat
import info.spiralframework.core.common.formats.compression.SpcCompressionFormat
import info.spiralframework.core.common.formats.filterIsIdentifyFormatResultOrNull
import info.spiralframework.core.common.formats.value
import org.yaml.snakeyaml.error.YAMLException
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.math.abs

object UserAgents {
    const val DEFAULT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:64.0) Gecko/20100101 Firefox/64.0"
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

typealias ReadableCompressionFormat= ReadableSpiralFormat<DataSource<*>>

val COMPRESSION_FORMATS = arrayOf(CrilaylaCompressionFormat, DRVitaFormat, SpcCompressionFormat, DRv3CompressionFormat)

suspend fun SpiralContext.decompress(dataSource: DataSource<*>): Pair<DataSource<*>, List<ReadableCompressionFormat>?> {
    val result = COMPRESSION_FORMATS.map { format -> format.identify(source = dataSource, context = this) }
                     .filterIsIdentifyFormatResultOrNull<DataSource<*>>()
                     .maxBy(FormatResult<*, *>::confidence)
                 ?: return Pair(dataSource, null)

    val resultDataSource = result.value().getOrElseRun{
        result.format().read(source = dataSource, context = this)
            .getOrBreak { return Pair(dataSource, null) }
    }

    val (decompressed, list) = decompress(resultDataSource)

    if (list is MutableList<ReadableCompressionFormat>) {
        list.add(0, result.format())
        return Pair(decompressed, list)
    } else if (list != null) {
        val mutList = list.toMutableList()
        mutList.add(0, result.format())
        return Pair(decompressed, list)
    } else {
        return Pair(decompressed, listOf(result.format()))
    }
}

inline fun <reified T : Any> ObjectMapper.tryReadValue(src: ByteArray): T? {
    try {
        return this.readValue(src)
    } catch (jsonProcessing: JsonProcessingException) {
    } catch (jsonMapping: JsonMappingException) {
    } catch (jsonParsing: JsonParseException) {
    } catch (yamlParsing: YAMLException) {
    }

    return null
}

inline fun <reified T : Any> ObjectMapper.tryReadValue(src: InputStream): T? {
    try {
        return this.readValue(src)
    } catch (jsonProcessing: JsonProcessingException) {
    } catch (jsonMapping: JsonMappingException) {
    } catch (jsonParsing: JsonParseException) {
    } catch (yamlParsing: YAMLException) {
    }

    return null
}

inline fun <reified T : Any> ObjectMapper.tryReadValue(src: File): T? {
    try {
        return this.readValue(src)
    } catch (jsonProcessing: JsonProcessingException) {
    } catch (jsonMapping: JsonMappingException) {
    } catch (jsonParsing: JsonParseException) {
    } catch (io: FileNotFoundException) {
    } catch (yamlParsing: YAMLException) {
    }

    return null
}

suspend fun <T : CancellableSpiralEvent> SpiralEventBus.postCancellable(context: SpiralContext, event: T): Boolean {
    context.post(event)

    return event.cancelled
}

fun <T> T.identifySelf(): T = this

fun <T : SpiralEventBus> T.installLoggingSubscriber(): T {
    register("Logging", SpiralEventPriority.HIGHEST) { event: SpiralEvent -> trace("core.eventbus.logging.event", event) }
    return this
}


/**
 * Returns a list of all formats sorted against the name provided by the read context
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @sample samples.collections.Collections.Sorting.sortedBy
 */
public inline fun <T: SpiralFormat> Iterable<T>.sortedAgainst(context: SpiralProperties?): List<T> {
    return sortedWith(compareBy { format -> abs(format.extension?.compareTo(context[ISpiralProperty.FileName]?.substringAfterLast('.') ?: "") ?: -100) })
}

public inline fun <T, R: KorneaResult<*>> Iterable<T>.mapResults(transform: (T) -> R): List<R> {
    return mapResultsTo(ArrayList<R>(10), transform)
}

public inline fun <T, R: KorneaResult<*>, C : MutableCollection<in R>> Iterable<T>.mapResultsTo(destination: C, transform: (T) -> R): C {
    for (item in this) {
        val transformed = transform(item)
        destination.add(transformed)
        if (transformed is FormatResult<*, *> && transformed.confidence() >= 0.99) break
    }
    return destination
}