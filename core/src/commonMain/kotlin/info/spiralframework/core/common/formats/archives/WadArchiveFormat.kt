package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.archives.*

public object WadArchiveFormat : ReadableSpiralFormat<WadArchive>, WritableSpiralFormat<CustomWadArchive> {
    override val name: String = "Wad"
    override val extension: String = "wad"

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): SpiralFormatReturnResult<WadArchive> =
        WadArchive(context, source)
            .filter { wad -> wad.files.isNotEmpty() }
            .ensureFormatSuccess { wad -> if (wad.files.size == 1) 0.75 else 1.0 }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is AwbArchive
        || data is WadArchive
        || data is CpkArchive
        || data is SpcArchive
        || data is PakArchive
//        || data is ZipFile

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): KorneaResult<CustomWadArchive> {
        val customWad = CustomWadArchive()
        val caches: MutableList<DataPool<*, *>> = ArrayList()

        when (data) {
            is AwbArchive -> data.files.forEach { entry -> customWad[entry.id.toString()] = data.openSource(entry) }
            is CpkArchive -> data.files.forEach { entry ->
                data.openDecompressedSource(context, entry).doOnSuccess { customWad[entry.name] = it }
            }
            is PakArchive -> data.files.forEach { entry -> customWad[entry.index.toString()] = data.openSource(entry) }
            is SpcArchive -> data.files.forEach { entry ->
                data.openDecompressedSource(context, entry).doOnSuccess { customWad[entry.name] = it }
            }
            is WadArchive -> data.files.forEach { entry -> customWad[entry.name] = data.openSource(entry) }
//            is ZipFile -> data.entries().iterator().forEach { entry ->
//                val cache = context.cacheShortTerm(context, "zip:${entry.name}")
//
//                cache.openOutputFlow()
//                        .map { output ->
//                            data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
//                            customWad[entry.name] = cache
//                            caches.add(cache)
//                        }.doOnFailure {
//                            cache.close()
//                            customWad[entry.name] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
//                        }
//            }
            else -> return KorneaResult.spiralWrongFormat()
        }

        customWad.compile(flow)
        caches.suspendForEach(DataPool<*, *>::close)
        return KorneaResult.success(customWad)
    }
}