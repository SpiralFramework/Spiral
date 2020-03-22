package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.core.formats.*
import info.spiralframework.formats.common.archives.*
import org.abimon.kornea.io.common.BinaryDataSource
import org.abimon.kornea.io.common.DataPool
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.copyTo
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.jvm.JVMInputFlow
import java.io.InputStream
import java.util.*
import java.util.zip.ZipFile
import kotlin.collections.ArrayList

object PakArchiveFormat : ReadableSpiralFormat<PakArchive>, WritableSpiralFormat {
    override val name: String = "Pak"
    override val extension: String = "pak"

    override fun preferredConversionFormat(): WritableSpiralFormat? = ZipFormat

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<PakArchive>> {
        val fileName = readContext?.name?.substringAfterLast('/')
        if (fileName != null && fileName.contains('.')) {
            if (!fileName.substringAfterLast('.').equals(extension, true)) {
                return FormatResult.Fail(0.9)
            } else {
                return super.identify(context, readContext, source).weight { 1.0 }
            }
        }

        return super.identify(context, readContext, source)
    }

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param dataContext Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<PakArchive> {
        val pak = PakArchive(context = context, dataSource = source) ?: return FormatResult.Fail(this, 1.0)

        if (pak.files.size == 1)
            return FormatResult.Success(this, pak, 0.75)

        return FormatResult(this, pak, pak.files.isNotEmpty(), 0.8) //Not positive on this one chief but we're going with it
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
     * @param dataContext Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        val customPak = CustomPakArchive()
        val caches: MutableList<DataPool<*, *>> = ArrayList()

        when (data) {
            is AwbArchive -> data.files.forEach { entry -> customPak[entry.id] = data.openSource(entry) }
            is CpkArchive -> data.files.forEach { entry ->
                customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] =
                        data.openDecompressedSource(context, entry) ?: return@forEach
            }
            is PakArchive -> data.files.forEach { entry -> customPak[entry.index] = data.openSource(entry) }
            is SpcArchive -> data.files.forEach { entry ->
                customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] =
                        data.openDecompressedSource(context, entry) ?: return@forEach
            }
            is WadArchive -> data.files.forEach { entry ->
                customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] =
                        data.openSource(entry)
            }
            is ZipFile -> data.entries().iterator().forEach { entry ->
                val cache = context.cacheShortTerm(context, "zip:${entry.name}")

                val output = cache.openOutputFlow()
                if (output == null) {
                    //Cache has failed; store in memory
                    cache.close()
                    customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] =
                            BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
                } else {
                    data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
                    customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] =
                            cache
                    caches.add(cache)
                }
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }

        customPak.compile(flow)
        caches.suspendForEach(DataPool<*, *>::close)
        return FormatWriteResponse.SUCCESS
    }
}