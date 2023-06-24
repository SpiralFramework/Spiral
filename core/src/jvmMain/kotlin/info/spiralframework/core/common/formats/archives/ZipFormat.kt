package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.base.common.use
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyTo
import dev.brella.kornea.io.common.flow.extensions.copyToOutputFlow
import dev.brella.kornea.io.common.flow.readChunked
import dev.brella.kornea.io.jvm.JVMInputFlow
import dev.brella.kornea.io.jvm.asOutputStream
import dev.brella.kornea.io.jvm.files.*
import dev.brella.kornea.toolkit.common.freeze
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.archives.*
import info.spiralframework.formats.common.archives.srd.BaseSrdEntry
import info.spiralframework.formats.common.archives.srd.SrdArchive
import info.spiralframework.formats.common.archives.srd.openMainDataFlow
import info.spiralframework.formats.common.archives.srd.openSubDataFlow
import info.spiralframework.formats.jvm.archives.ZipArchive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

public actual object ZipFormat : ReadableSpiralFormat<ZipArchive>, WritableSpiralFormat<Unit>, WritableSpiralFormatBridge<Unit> {
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
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): SpiralFormatReturnResult<ZipArchive> {
        if (source is SynchronousFileDataSource) {
            try {
                return withContext(Dispatchers.IO) {
                    buildFormatSuccess(ZipArchive(ZipFile(source.backing)), 1.0)
                }
            } catch (io: IOException) {
                return KorneaResult.thrown(io)
            }
        } else if (source is AsyncFileDataSource) {
            try {
                return withContext(Dispatchers.IO) {
                    buildFormatSuccess(ZipArchive(ZipFile(source.backing.toFile())), 1.0)
                }
            } catch (io: IOException) {
                return KorneaResult.thrown(io)
            }
        } else {
            var zip: ZipFile? = null
            var ioException: IOException? = null
            var tmpFile: File? = null

            try {
                source.openInputFlow().doOnSuccess { flow ->
                    if (flow is SynchronousFileInputFlow) {
                        return buildFormatSuccess(ZipArchive(ZipFile(flow.backingFile)), 1.0)
                    } else if (flow is AsyncFileInputFlow) {
                        return buildFormatSuccess(ZipArchive(ZipFile(flow.backing.toFile())), 1.0)
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
                return buildFormatSuccess(ZipArchive(zip!!), 1.0)
            } else {
                tmpFile?.delete()
                return ioException?.let { io -> KorneaResult.thrown(io) } ?: KorneaResult.empty()
            }
        }
    }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = data is AwbArchive || data is WadArchive || data is CpkArchive || data is SpcArchive || data is PakArchive || data is ZipFile

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): KorneaResult<Unit> {
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
                                entry.openMainDataFlow(data.dataSource).doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                                zipOut.putNextEntry(ZipEntry("${entry.classifierAsString}-$index-subdata"))
                                entry.openSubDataFlow(data.dataSource).doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                            }
                        }
                        is WadArchive -> data.files.forEach { entry ->
                            zipOut.putNextEntry(ZipEntry(entry.name))
                            data.openFlow(entry).doOnSuccess { it.readChunked { buffer, offset, length -> zipOut.write(buffer, offset, length) } }
                        }
                        else -> return@asOutputStream KorneaResult.spiralWrongFormat()
                    }
                } finally {
                    zipOut.finish()
                }

                return@asOutputStream KorneaResult.success()
            }
        }
    }

    override fun supportsWritingAs(context: SpiralContext, writeContext: SpiralProperties?, format: WritableSpiralFormat<*>, data: Any): Boolean =
        data is ZipFile && (
                format === CpkArchiveFormat ||
                format === PakArchiveFormat ||
                format === SpcArchiveFormat ||
                format === WadArchiveFormat
                           )

    override suspend fun writeAs(context: SpiralContext, writeContext: SpiralProperties?, format: WritableSpiralFormat<*>, data: Any, flow: OutputFlow): KorneaResult<Unit> =
        if (data is ZipFile) {
            when (format) {
                CpkArchiveFormat -> {
                    val customCpk = CustomCpkArchive()
                    val caches: MutableList<DataPool<*, *>> = ArrayList()

                    data.entries().iterator().forEach { entry ->
                        val cache = context.cacheShortTerm(context, "zip:${entry.name}")

                        cache.openOutputFlow()
                            .map { output ->
                                data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
                                customCpk[entry.name] = cache
                                caches.add(cache)
                            }.switchIfFailure {
                                cache.close()
                                customCpk[entry.name] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))

                                KorneaResult.empty()
                            }
                    }

                    customCpk.compile(context, flow)
                    caches.suspendForEach(DataPool<*, *>::close)

                    KorneaResult.success()
                }
                PakArchiveFormat -> {
                    val customPak = CustomPakArchive()
                    val caches: MutableList<DataPool<*, *>> = ArrayList()

                    data.entries().iterator().forEach { entry ->
                        val cache = context.cacheShortTerm(context, "zip:${entry.name}")
                        val index = entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()

                        @Suppress("DEPRECATION")
                        cache.openOutputFlow()
                            .map { output ->
                                data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
                                customPak[index] = cache
                                caches.add(cache)
                            }.doOnFailure {
                                cache.close()
                                customPak[index] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
                            }
                    }

                    customPak.compile(flow)
                    caches.suspendForEach(DataPool<*, *>::close)

                    KorneaResult.success()
                }
                SpcArchiveFormat -> {
                    val customSpc = CustomSpcArchive()
                    val caches: MutableList<DataPool<*, *>> = ArrayList()

                    data.entries().iterator().forEach { entry ->
                        val cache = context.cacheShortTerm(context, "zip:${entry.name}")

                        cache.openOutputFlow()
                            .map { output ->
                                data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
                                customSpc[entry.name] = cache
                                caches.add(cache)
                            }.doOnFailure {
                                cache.close()
                                customSpc[entry.name] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
                            }
                    }

                    customSpc.compile(flow)
                    caches.suspendForEach(DataPool<*, *>::close)

                    KorneaResult.success()
                }
                WadArchiveFormat -> {
                    val customWad = CustomWadArchive()
                    val caches: MutableList<DataPool<*, *>> = ArrayList()

                    data.entries().iterator().forEach { entry ->
                        val cache = context.cacheShortTerm(context, "zip:${entry.name}")

                        cache.openOutputFlow()
                            .map { output ->
                                data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
                                customWad[entry.name] = cache
                                caches.add(cache)
                            }.doOnFailure {
                                cache.close()
                                customWad[entry.name] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
                            }
                    }

                    customWad.compile(flow)
                    caches.suspendForEach(DataPool<*, *>::close)

                    KorneaResult.success()
                }
                else -> KorneaResult.spiralWrongFormat()
            }
        } else {
            KorneaResult.spiralWrongFormat()
        }
}