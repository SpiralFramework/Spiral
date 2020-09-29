package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.common.archives.*
import info.spiralframework.formats.common.archives.srd.BaseSrdEntry
import info.spiralframework.formats.common.archives.srd.SrdArchive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.brella.kornea.annotations.ExperimentalKorneaIO
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyToOutputFlow
import dev.brella.kornea.io.common.flow.readChunked
import dev.brella.kornea.io.jvm.asOutputStream
import dev.brella.kornea.io.jvm.files.*
import dev.brella.kornea.toolkit.common.freeze
import dev.brella.kornea.toolkit.common.use
import info.spiralframework.formats.jvm.archives.ZipArchive
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipFormat : ReadableSpiralFormat<ZipArchive>, WritableSpiralFormat {
    override val name: String = "Zip"
    override val extension: String = "zip"

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
    @ExperimentalKorneaIO
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<ZipArchive> {
        if (source is SynchronousFileDataSource) {
            try {
                return withContext(Dispatchers.IO) {
                    buildFormatResult(ZipArchive(ZipFile(source.backing)), 1.0)
                }
            } catch (io: IOException) {
                return KorneaResult.WithException.of(io)
            }
        } else if (source is AsyncFileDataSource) {
            try {
                return withContext(Dispatchers.IO) {
                    buildFormatResult(ZipArchive(ZipFile(source.backing.toFile())), 1.0)
                }
            } catch (io: IOException) {
                return KorneaResult.WithException.of(io)
            }
        } else {
            var zip: ZipFile? = null
            var ioException: IOException? = null
            var tmpFile: File? = null

            try {
                source.openInputFlow().doOnSuccess { flow ->
                    if (flow is SynchronousFileInputFlow) {
                        return buildFormatResult(ZipArchive(ZipFile(flow.backingFile)), 1.0)
                    } else if (flow is AsyncFileInputFlow) {
                        return buildFormatResult(ZipArchive(ZipFile(flow.backing.toFile())), 1.0)
                    }
                    
                    withContext(Dispatchers.IO) {
                        freeze(File.createTempFile(UUID.randomUUID().toString(), ".dat")) { tmp ->
                            tmpFile = tmp
                            tmp.deleteOnExit()

                            AsyncFileOutputFlow(tmp).use { flow.copyToOutputFlow(it) }
                            zip = ZipFile(tmp)

                            source.registerCloseHandler { withContext(Dispatchers.IO) { tmp.delete() } }
                        }
                    }
                }
            } catch (io: IOException) {
                withContext(Dispatchers.IO) { tmpFile?.delete() }
                ioException = io
            }

            if (zip != null) {
                return buildFormatResult(ZipArchive(zip!!), 1.0)
            } else {
                tmpFile?.delete()
                return ioException?.let { io -> KorneaResult.WithException.of(io) } ?: KorneaResult.empty()
            }
        }
    }

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = data is AwbArchive || data is WadArchive || data is CpkArchive || data is SpcArchive || data is PakArchive || data is ZipFile

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
    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        return withContext(Dispatchers.IO) {
            asOutputStream(flow, false) { rawOut ->
                val zipOut = ZipOutputStream(rawOut)

                try {
                    when (data) {
                        is ZipFile -> data.entries().iterator().forEach { entry ->
                            zipOut.putNextEntry(entry)
                            data.getInputStream(entry).use { zipIn -> zipIn.copyTo(zipOut) }
                        }

                        is AwbArchive -> data.files.forEach { entry ->
                            zipOut.putNextEntry(ZipEntry(entry.id.toString()))
                            data.openFlow(entry).doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                        }
                        is CpkArchive -> data.files.forEach { entry ->
                            zipOut.putNextEntry(ZipEntry(entry.name))
                            data.openDecompressedFlow(context, entry)
                                    .doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                        }
                        is PakArchive -> data.files.forEach { entry ->
                            zipOut.putNextEntry(ZipEntry(entry.index.toString()))
                            data.openFlow(entry).doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                        }
                        is SpcArchive -> data.files.forEach { entry ->
                            zipOut.putNextEntry(ZipEntry(entry.name))
                            data.openDecompressedFlow(context, entry)
                                    .doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                        }
                        is SrdArchive -> data.entries.groupBy(BaseSrdEntry::classifierAsString).forEach { (_, list) ->
                            list.forEachIndexed { index, entry ->
                                zipOut.putNextEntry(ZipEntry("${entry.classifierAsString}-$index-data"))
                                entry.openMainDataFlow().doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                                zipOut.putNextEntry(ZipEntry("${entry.classifierAsString}-$index-subdata"))
                                entry.openSubDataFlow().doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                            }
                        }
                        is WadArchive -> data.files.forEach { entry ->
                            zipOut.putNextEntry(ZipEntry(entry.name))
                            data.openFlow(entry).doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                        }
                        else -> return@withContext FormatWriteResponse.WRONG_FORMAT
                    }
                } finally {
                    zipOut.finish()
                }

                return@withContext FormatWriteResponse.SUCCESS
            }
        }
    }
}