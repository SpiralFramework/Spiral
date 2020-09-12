package info.spiralframework.core

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.brella.kornea.errors.common.*
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.events.*
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.compression.*
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.filterIsIdentifyFormatResult
import info.spiralframework.core.formats.value
import org.yaml.snakeyaml.error.YAMLException
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

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

typealias ReadableCompressionFormat=ReadableSpiralFormat<DataSource<*>>

val COMPRESSION_FORMATS = arrayOf(CrilaylaCompressionFormat, DRVitaFormat, SpcCompressionFormat, DRv3CompressionFormat)

suspend fun SpiralContext.decompress(dataSource: DataSource<*>): Pair<DataSource<*>, List<ReadableCompressionFormat>?> {
    val result = COMPRESSION_FORMATS.map { format -> format.identify(source = dataSource, context = this) }
                     .filterIsIdentifyFormatResult<DataSource<*>>()
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