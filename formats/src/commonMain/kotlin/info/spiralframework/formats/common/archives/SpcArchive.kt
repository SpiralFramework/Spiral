package info.spiralframework.formats.common.archives

import com.soywiz.krypto.sha256
import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.WindowedDataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readString
import dev.brella.kornea.io.common.flow.extensions.readUInt32LE
import dev.brella.kornea.io.common.flow.readAndClose
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.compression.decompressSpcData
import info.spiralframework.formats.common.withFormats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull

public class SpcArchive(public val unknownFlag: Int, public val files: Array<SpcFileEntry>, public val dataSource: DataSource<*>): SpiralArchive {
    public companion object {
        /** .SPC */
        public const val SPC_MAGIC_NUMBER_LE: Int = 0x2E535043

        /** CPS. */
        public const val SPC_MAGIC_NUMBER_BE: Int = 0x4350532E

        /** Root */
        public const val TABLE_MAGIC_NUMBER_LE: Int = 0x746f6f52
        public const val TABLE_MAGIC_NUMBER_BE: Int = 0x526f6f74

        public const val INVALID_MAGIC_NUMBER: Int = 0x0000
        public const val INVALID_TABLE_MAGIC_NUMBER: Int = 0x0010

        public const val INVALID_MAGIC_NUMBER_KEY: String = "formats.spc.invalid_magic_number"
        public const val INVALID_TABLE_MAGIC_NUMBER_KEY: String = "formats.spc.invalid_table_magic_number"
        public const val NOT_ENOUGH_DATA_KEY: String = "formats.spc.not_enough_data"

        public const val COMPRESSED_FLAG: Int = 0x02

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<SpcArchive> =
            withFormats(context) {
                val flow = requireNotNull(dataSource.openInputFlow())
                    .getOrBreak { return it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != SPC_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_MAGIC_NUMBER,
                            localise(
                                INVALID_MAGIC_NUMBER_KEY,
                                "0x${magic.toString(16)}",
                                "0x${SPC_MAGIC_NUMBER_LE.toString(16)}"
                            )
                        )
                    }

                    flow.skip(0x24u)

                    val fileCount = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unknown = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    flow.skip(0x10u)

                    val tableMagic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (tableMagic != TABLE_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_TABLE_MAGIC_NUMBER,
                            localise(
                                "formats.spc.invalid_table_magic",
                                "0x${tableMagic.toString(16)}",
                                "0x${TABLE_MAGIC_NUMBER_LE.toString(16)}"
                            )
                        )
                    }

                    flow.skip(0x0Cu)

                    val files = Array(fileCount) {
                        val compressionFlag =
                            flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val unknownFlag =
                            flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        val compressedSize = flow.readUInt32LE()?.toLong()
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val decompressedSize = flow.readUInt32LE()?.toLong()
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val nameLength =
                            flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

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

    public operator fun get(name: String): SpcFileEntry? = files.firstOrNull { entry -> entry.name == name }

    public fun openRawSource(file: SpcFileEntry): DataSource<InputFlow> = WindowedDataSource(dataSource, file.offset, file.compressedSize.toULong(), closeParent = false)
    public suspend fun openRawFlow(file: SpcFileEntry): KorneaResult<InputFlow> = dataSource.openInputFlow()
        .map { parent -> WindowedInputFlow(parent, file.offset, file.compressedSize.toULong()) }

    public suspend fun SpiralContext.openDecompressedSource(file: SpcFileEntry): KorneaResult<DataSource<InputFlow>> {
        if (file.compressionFlag == COMPRESSED_FLAG) {
            val flow = openRawFlow(file).getOrBreak { return it.cast() }
            val compressedData = flow.readAndClose()
            val cache = cacheShortTerm(compressedData.sha256().hexLower)

            return cache.openOutputFlow()
                .useAndFlatMap { output ->
                    decompressSpcData(compressedData, file.decompressedSize.toInt()).map { data ->
                        output.write(data)
                        cache
                    }.doOnFailure {
                        cache.close()
                    }
                }.switchIfFailure {
                    cache.close()

                    decompressSpcData(compressedData, file.decompressedSize.toInt())
                        .map(::BinaryDataSource)
                }
        } else {
            return KorneaResult.success(openRawSource(file))
        }
    }

    public suspend fun SpiralContext.openDecompressedFlow(file: SpcFileEntry): KorneaResult<InputFlow> {
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
                error("Spc sub file {0} did not decompress properly: {1}", file.name, failure)
                return@mapNotNull null
            })
        }
}

public suspend fun SpcArchive.openDecompressedSource(context: SpiralContext, file: SpcFileEntry): KorneaResult<DataSource<InputFlow>> = context.openDecompressedSource(file)
public suspend fun SpcArchive.openDecompressedFlow(context: SpiralContext, file: SpcFileEntry): KorneaResult<InputFlow> = context.openDecompressedFlow(file)

@Suppress("FunctionName")
public suspend fun SpiralContext.SpcArchive(dataSource: DataSource<*>): KorneaResult<SpcArchive> = SpcArchive(this, dataSource)
@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeSpcArchive(dataSource: DataSource<*>): SpcArchive = SpcArchive(this, dataSource).getOrThrow()