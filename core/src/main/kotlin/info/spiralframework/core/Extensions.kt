package info.spiralframework.core

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.compression.*
import info.spiralframework.formats.utils.DataSource
import java.io.Closeable

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
            stream == null -> {}
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