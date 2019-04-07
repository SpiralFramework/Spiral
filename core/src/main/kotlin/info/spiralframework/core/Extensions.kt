package info.spiralframework.core

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.properties.CachedFileReadOnlyProperty
import info.spiralframework.core.eventbus.FunctionSubscriber
import info.spiralframework.core.eventbus.LoggingSubscriber
import info.spiralframework.core.formats.compression.*
import info.spiralframework.core.plugins.events.CancellableSpiralEvent
import info.spiralframework.formats.utils.DataSource
import org.greenrobot.eventbus.EventBus
import org.slf4j.Logger
import org.yaml.snakeyaml.error.YAMLException
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

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

val COMPRESSION_FORMATS = arrayOf(CRILAYLAFormat, DRVitaFormat, SPCCompressionFormat, V3CompressionFormat)

fun decompress(dataSource: DataSource): Pair<DataSource, List<CompressionFormat<*>>> {
    val (format, result) = COMPRESSION_FORMATS.map { format -> format to format.read(source = dataSource) }
            .filter { pair -> pair.second.didSucceed }
            .sortedBy { pair -> pair.second.chance }
            .firstOrNull() ?: return dataSource to emptyList()

    val (decompressed, list) = decompress(result.obj)

    return decompressed to mutableListOf(format).apply { addAll(list) }
}

fun Request.userAgent(ua: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:64.0) Gecko/20100101 Firefox/64.0"): Request
        = this.header("User-Agent", ua)

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

inline fun <reified T: Any> cacheJson(file: File): CachedFileReadOnlyProperty<Any, T> = CachedFileReadOnlyProperty(file, SpiralSerialisation.JSON_MAPPER::readValue)
inline fun <reified T: Any> cacheNullableJson(file: File): CachedFileReadOnlyProperty<Any, T?> = CachedFileReadOnlyProperty(file, SpiralSerialisation.JSON_MAPPER::tryReadValue)
inline fun <reified T: Any> cacheYaml(file: File): CachedFileReadOnlyProperty<Any, T> = CachedFileReadOnlyProperty(file, SpiralSerialisation.YAML_MAPPER::readValue)
inline fun <reified T: Any> cacheNullableYaml(file: File): CachedFileReadOnlyProperty<Any, T?> = CachedFileReadOnlyProperty(file, SpiralSerialisation.YAML_MAPPER::tryReadValue)

fun <T: Any> ResponseResultOf<T>.takeResponseIfSuccessful(): Response? {
    val (_, response, result) = this

    if (response.isSuccessful)
        return response
    return null
}

fun <T: Any> ResponseResultOf<T>.takeIfSuccessful(): T? {
    val (_, response, result) = this

    if (response.isSuccessful)
        return result.get()
    return null
}

fun <T: CancellableSpiralEvent> EventBus.cancel(t: T) {
    t.isCanceled = true
    cancelEventDelivery(t)
}

fun <T: CancellableSpiralEvent> EventBus.postCancellable(t: T): Boolean {
    post(t)

    return t.isCanceled
}

inline fun <reified T: Any> EventBus.registerFunction(noinline func: (T) -> Unit): Any {
    val obj = FunctionSubscriber(func)

    register(obj)
    return obj
}

fun <T> T.identifySelf(): T = this

fun <T> EventBus.postback(t: T): T {
    post(t)
    return t
}

fun EventBus.installLoggingSubscriber(logger: Logger): EventBus {
    LoggingSubscriber(this, logger)
    return this
}