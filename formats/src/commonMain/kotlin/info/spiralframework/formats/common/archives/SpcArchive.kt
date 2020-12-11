package info.spiralframework.formats.common.archives

import com.soywiz.krypto.sha256
import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.*
import info.spiralframework.base.common.io.readString
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.compression.decompressSpcData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readUInt32LE
import dev.brella.kornea.toolkit.common.closeAfter
import dev.brella.kornea.toolkit.common.useAndFlatMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

@ExperimentalUnsignedTypes
class SpcArchive(val unknownFlag: Int, val files: Array<SpcFileEntry>, val dataSource: DataSource<*>): SpiralArchive {
    companion object {
        /** .SPC */
        const val SPC_MAGIC_NUMBER_LE = 0x2E535043

        /** CPS. */
        const val SPC_MAGIC_NUMBER_BE = 0x4350532E

        /** Root */
        const val TABLE_MAGIC_NUMBER_LE = 0x746f6f52
        const val TABLE_MAGIC_NUMBER_BE = 0x526f6f74

        const val INVALID_MAGIC_NUMBER = 0x0000
        const val INVALID_TABLE_MAGIC_NUMBER = 0x0010

        const val INVALID_MAGIC_NUMBER_KEY = "formats.spc.invalid_magic_number"
        const val INVALID_TABLE_MAGIC_NUMBER_KEY = "formats.spc.invalid_table_magic_number"
        const val NOT_ENOUGH_DATA_KEY = "formats.spc.not_enough_data"

        const val COMPRESSED_FLAG = 0x02

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<SpcArchive> =
            withFormats(context) {
                val flow = requireNotNull(dataSource.openInputFlow())
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != SPC_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_MAGIC_NUMBER, localise(INVALID_MAGIC_NUMBER_KEY, "0x${magic.toString(16)}", "0x${SPC_MAGIC_NUMBER_LE.toString(16)}"))
                    }

                    flow.skip(0x24u)

                    val fileCount = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unknown = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    flow.skip(0x10u)

                    val tableMagic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (tableMagic != TABLE_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_TABLE_MAGIC_NUMBER, localise("formats.spc.invalid_table_magic", "0x${tableMagic.toString(16)}", "0x${TABLE_MAGIC_NUMBER_LE.toString(16)}"))
                    }

                    flow.skip(0x0Cu)

                    val files = Array(fileCount) {
                        val compressionFlag = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val unknownFlag = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        val compressedSize = flow.readUInt32LE()?.toLong()
                                             ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val decompressedSize = flow.readUInt32LE()?.toLong()
                                               ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val nameLength = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        //require(compressionFlag in COMPRESSION_FLAG_ARRAY)

                        flow.skip(0x10u)

                        val namePadding = (nameLength + 1) alignmentNeededFor 0x10
                        val dataPadding = compressedSize alignmentNeededFor 0x10

                        val name = flow.readString(nameLength, encoding = TextCharsets.UTF_8)
                                   ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        flow.skip(namePadding.toULong() + 1u)

                        val position = flow.position()

                        flow.skip((compressedSize + dataPadding).toULong())

                        SpcFileEntry(name, compressionFlag, unknownFlag, compressedSize, decompressedSize, position)
                    }

                    return@closeAfter KorneaResult.success(SpcArchive(unknown, files, dataSource))
                }
            }
    }

    override val fileCount: Int
        get() = files.size

    operator fun get(name: String): SpcFileEntry? = files.firstOrNull { entry -> entry.name == name }

    suspend fun SpiralContext.openRawSource(file: SpcFileEntry): DataSource<InputFlow> = WindowedDataSource(dataSource, file.offset, file.compressedSize.toULong(), closeParent = false)
    suspend fun SpiralContext.openRawFlow(file: SpcFileEntry): KorneaResult<InputFlow> = dataSource.openInputFlow()
        .map { parent -> WindowedInputFlow(parent, file.offset, file.compressedSize.toULong()) }

    suspend fun SpiralContext.openDecompressedSource(file: SpcFileEntry): KorneaResult<DataSource<InputFlow>> {
        if (file.compressionFlag == COMPRESSED_FLAG) {
            val flow = openRawFlow(file).getOrBreak { return it.cast() }
            val compressedData = flow.readAndClose()
            val cache = cacheShortTerm(compressedData.sha256().toHexString())

            return cache.openOutputFlow()
                .useAndFlatMap { output ->
                    decompressSpcData(compressedData, file.decompressedSize.toInt()).map { data ->
                        output.write(data)
                        cache
                    }.doOnFailure {
                        cache.close()
                    }
                }.switchIfFailure { failure ->
                    cache.close()

                    decompressSpcData(compressedData, file.decompressedSize.toInt())
                        .map(::BinaryDataSource)
                }
        } else {
            return KorneaResult.success(openRawSource(file))
        }
    }

    suspend fun SpiralContext.openDecompressedFlow(file: SpcFileEntry): KorneaResult<InputFlow> {
        if (file.compressionFlag == COMPRESSED_FLAG) {
            val source = openDecompressedSource(file).getOrBreak { return it.cast() }
            return source.openInputFlow()
                .doOnSuccess { input -> input.registerCloseHandler { source.close() } }
        } else {
            return openRawFlow(file)
        }
    }

    override suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>> =
        files.asFlow().mapNotNull { file ->
            SpiralArchiveSubfile(file.name, openDecompressedSource(file).getOrBreak { failure ->
                error("Cpk sub file {0} did not decompress properly: {1}", file.name, failure)
                return@mapNotNull null
            })
        }
}

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openRawSource(context: SpiralContext, file: SpcFileEntry) = context.openRawSource(file)

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openRawFlow(context: SpiralContext, file: SpcFileEntry) = context.openRawFlow(file)

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openDecompressedSource(context: SpiralContext, file: SpcFileEntry) = context.openDecompressedSource(file)

@ExperimentalUnsignedTypes
suspend fun SpcArchive.openDecompressedFlow(context: SpiralContext, file: SpcFileEntry) = context.openDecompressedFlow(file)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.SpcArchive(dataSource: DataSource<*>) = SpcArchive(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeSpcArchive(dataSource: DataSource<*>) = SpcArchive(this, dataSource).get()