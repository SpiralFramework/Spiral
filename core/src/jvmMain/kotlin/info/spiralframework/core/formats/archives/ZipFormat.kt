package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.FlowOutputStream
import info.spiralframework.base.common.io.asOutputStream
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.io.readChunked
import info.spiralframework.base.common.useAndFlatMap
import info.spiralframework.base.common.useAndMap
import info.spiralframework.base.jvm.crypto.sha512Hash
import info.spiralframework.core.formats.*
import info.spiralframework.formats.common.archives.*
import info.spiralframework.formats.common.archives.srd.BaseSrdEntry
import info.spiralframework.formats.common.archives.srd.SrdArchive
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.erorrs.common.doOnSuccess
import org.abimon.kornea.erorrs.common.flatMap
import org.abimon.kornea.erorrs.common.map
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.addCloseHandler
import org.abimon.kornea.io.common.copyToOutputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.use
import org.abimon.kornea.io.jvm.JVMOutputFlow
import org.abimon.kornea.io.jvm.files.FileDataSource
import org.abimon.kornea.io.jvm.files.FileOutputFlow
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipFormat : ReadableSpiralFormat<ZipFile>, WritableSpiralFormat {
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
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<ZipFile> {
        if (source is FileDataSource) {
            try {
                return withContext(Dispatchers.IO) {
                    FormatResult.Success(this@ZipFormat, ZipFile(source.backing), 1.0)
                }
            } catch (io: IOException) {
                return FormatResult.Fail(this, 1.0, KorneaResult.Thrown(io))
            }
        } else {
            var zip: ZipFile? = null
            var ioException: IOException? = null

            val tmpFile = withContext(Dispatchers.IO) { File.createTempFile(UUID.randomUUID().toString(), ".dat") }
            tmpFile.deleteOnExit()

            try {
                source.openInputFlow().doOnSuccess { flow ->
                    withContext(Dispatchers.IO) {
                        FileOutputFlow(tmpFile).use { flow.copyToOutputFlow(it) }
                        zip = ZipFile(tmpFile)
                    }

                    source.addCloseHandler { withContext(Dispatchers.IO) { tmpFile.delete() } }
                }
            } catch (io: IOException) {
                withContext(Dispatchers.IO) { tmpFile.delete() }
                ioException = io
            }

            if (zip != null) {
                return FormatResult.Success(this, zip!!, 1.0)
            } else {
                tmpFile.delete()
                return FormatResult.Fail(this, 1.0, KorneaResult.Thrown(ioException))
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