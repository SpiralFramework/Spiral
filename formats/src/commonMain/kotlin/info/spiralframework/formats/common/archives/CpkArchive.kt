package info.spiralframework.formats.common.archives

import com.soywiz.krypto.sha256
import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.OffsetDataSource
import dev.brella.kornea.io.common.WindowedDataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.readAndClose
import info.spiralframework.base.common.Moment
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.compression.decompressCrilayla
import info.spiralframework.formats.common.withFormats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull

public class CpkArchive(
    public val header: UtfTableInfo,
    public val tocHeader: UtfTableInfo,
    public val etocHeader: KorneaResult<UtfTableInfo>,
    public val itocHeader: KorneaResult<UtfTableInfo>,
    public val gtocHeader: KorneaResult<UtfTableInfo>,
    public val files: Array<CpkFileEntry>,
    public val dataSource: DataSource<*>
) : SpiralArchive {
    public companion object {
        public const val MAGIC_NUMBER_LE: Int = 0x204b5043

        public const val INVALID_CPK_MAGIC: Int = 0x0000
        public const val INVALID_HEADER_TABLE: Int = 0x0010
        public const val INVALID_HEADER_ROW_COUNT: Int = 0x0011
        public const val INVALID_TOC_TABLE: Int = 0x0020
        public const val INVALID_TOC_ROW_COUNT: Int = 0x0021
        public const val INVALID_FILE_ROW: Int = 0x22

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.cpk.not_enough_data"
        public const val INVALID_MAGIC_KEY: String = "formats.cpk.invalid_magic"
        public const val INVALID_HEADER_TABLE_KEY: String = "formats.cpk.invalid_header_table"
        public const val INVALID_HEADER_ROW_COUNT_KEY: String = "formats.cpk.invalid_header_row_count"
        public const val INVALID_TOC_TABLE_KEY: String = "formats.cpk.invalid_toc_table"
        public const val INVALID_TOC_ROW_COUNT_KEY: String = "formats.cpk.invalid_toc_header_row_count"
        public const val INVALID_FILE_ROW_KEY: String = "formats.cpk.invalid_file_row"

        public const val TOC_OFFSET_COLUMN: String = "TocOffset"
        public const val ETOC_OFFSET_COLUMN: String = "EtocOffset"
        public const val ITOC_OFFSET_COLUMN: String = "ItocOffset"
        public const val GTOC_OFFSET_COLUMN: String = "GtocOffset"
        public const val CONTENT_OFFSET_COLUMN: String = "ContentOffset"
        public const val FILES_COLUMN: String = "Files"

        public const val FILENAME_COLUMN: String = "FileName"
        public const val DIRECTORY_NAME_COLUMN: String = "DirName"
        public const val FILE_SIZE_COLUMN: String = "FileSize"
        public const val EXTRACT_SIZE_COLUMN: String = "ExtractSize"
        public const val FILE_OFFSET_COLUMN: String = "FileOffset"

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<CpkArchive> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_CPK_MAGIC,
                            localise(INVALID_MAGIC_KEY, "0x${magic.toString(16)}", "0x${MAGIC_NUMBER_LE.toString(16)}")
                        )
                    }

                    val headerInfo = UtfTableInfo(this, OffsetDataSource(dataSource, 0x10u))
                        .getOrBreak { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_HEADER_TABLE, localise(INVALID_HEADER_TABLE_KEY, it)) }

                    if (headerInfo.rowCount != 1u) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_HEADER_ROW_COUNT, localise(INVALID_HEADER_ROW_COUNT_KEY, headerInfo.rowCount))
                    }

                    val tocOffset = headerInfo.readRowData(this, 0, TOC_OFFSET_COLUMN)
                        .filterToInstance<UtfRowData.TypeLong>()
                        .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_HEADER_TABLE, localise(INVALID_HEADER_TABLE_KEY), it) }
                        .data

                    val etocOffset = headerInfo.readRowData(this, 0, ETOC_OFFSET_COLUMN)
                        .filterToInstance<UtfRowData.TypeLong>()
                        .filter { value -> value.data != 0L }
                    val itocOffset = headerInfo.readRowData(this, 0, ITOC_OFFSET_COLUMN)
                        .filterToInstance<UtfRowData.TypeLong>()
                        .filter { value -> value.data != 0L }
                    val gtocOffset = headerInfo.readRowData(this, 0, GTOC_OFFSET_COLUMN)
                        .filterToInstance<UtfRowData.TypeLong>()
                        .filter { value -> value.data != 0L }

                    val contentOffset = headerInfo.readRowData(this, 0, CONTENT_OFFSET_COLUMN)
                        .filterToInstance<UtfRowData.TypeLong>()
                        .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_HEADER_TABLE, localise(INVALID_HEADER_TABLE_KEY), it) }
                        .data

                    val fileCount = headerInfo.readRowData(this, 0, FILES_COLUMN)
                        .filterToInstance<UtfRowData.TypeInt>()
                        .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_HEADER_TABLE, localise(INVALID_HEADER_TABLE_KEY), it) }
                        .data

                    val tocHeader = UtfTableInfo(this, OffsetDataSource(dataSource, tocOffset.toULong() + 0x10u))
                        .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_TOC_TABLE, localise(INVALID_TOC_TABLE_KEY, it)) }

                    if (tocHeader.rowCount != fileCount.toUInt()) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_TOC_ROW_COUNT, localise(INVALID_TOC_ROW_COUNT_KEY, tocHeader.rowCount, fileCount))
                    }

                    val etocHeader = etocOffset.flatMap { offset -> UtfTableInfo(this, OffsetDataSource(dataSource, offset.data.toULong() + 0x10u)) }
                    val itocHeader = itocOffset.flatMap { offset -> UtfTableInfo(this, OffsetDataSource(dataSource, offset.data.toULong() + 0x10u)) }
                    val gtocHeader = gtocOffset.flatMap { offset -> UtfTableInfo(this, OffsetDataSource(dataSource, offset.data.toULong() + 0x10u)) }

                    val files = Array(tocHeader.rowCount.toInt()) { index ->
                        val fileName = tocHeader.readRowData(this, index, FILENAME_COLUMN)
                            .filterToInstance<UtfRowData.TypeString>()
                            .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_ROW, INVALID_FILE_ROW_KEY, it) }
                            .data

                        val directoryName = tocHeader.readRowData(this, index, DIRECTORY_NAME_COLUMN)
                            .filterToInstance<UtfRowData.TypeString>()
                            .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_ROW, INVALID_FILE_ROW_KEY, it) }
                            .data

                        val fileSize = tocHeader.readRowData(this, index, FILE_SIZE_COLUMN)
                            .filterToInstance<UtfRowData.TypeInt>()
                            .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_ROW, INVALID_FILE_ROW_KEY, it) }
                            .data

                        val extractSize = tocHeader.readRowData(this, index, EXTRACT_SIZE_COLUMN)
                            .filterToInstance<UtfRowData.TypeInt>()
                            .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_ROW, INVALID_FILE_ROW_KEY, it) }
                            .data

                        val fileOffset = tocHeader.readRowData(this, index, FILE_OFFSET_COLUMN)
                                             .filterToInstance<UtfRowData.TypeLong>()
                                             .getOrBreakWithFailure { return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FILE_ROW, INVALID_FILE_ROW_KEY, it) }
                                             .data + minOf(contentOffset, tocOffset)

                        CpkFileEntry(fileName, directoryName, fileSize, extractSize, fileOffset)
                    }

                    return@closeAfter KorneaResult.success(CpkArchive(headerInfo, tocHeader, etocHeader, itocHeader, gtocHeader, files, dataSource))
                }
            }

        public val VALID_MONTHS: IntRange = 1..12
        public val VALID_DAYS: IntRange = 1..31
        public val VALID_HOURS: IntRange = 0 until 24
        public val VALID_MINUTES: IntRange = 0 until 60
        public val VALID_SECONDS: IntRange = 0 until 60

        public fun convertFromEtocTime(time: Long): Moment {
            val year = ((time shr 48)).toInt()
            val month = ((time shr 40) and 0xFF).toInt().coerceIn(VALID_MONTHS)
            val day = ((time shr 32) and 0xFF).toInt().coerceIn(VALID_DAYS)
            val hour = ((time shr 24) and 0xFF).toInt().coerceIn(VALID_HOURS)
            val minute = ((time shr 16) and 0xFF).toInt().coerceIn(VALID_MINUTES)
            val second = ((time shr 8) and 0xFF).toInt().coerceIn(VALID_SECONDS)

            return Moment(year, month, day, hour, minute, second, 0)
        }

        public fun convertToEtocTime(moment: Moment): Long {
            val year = moment.year.toLong()
            val month = moment.month.toLong()
            val day = moment.day.toLong()
            val hour = moment.hour.toLong()
            val minute = moment.minute.toLong()
            val second = moment.second.toLong()

            return (year shl 48) or (month shl 40) or (day shl 32) or (hour shl 24) or (minute shl 16) or (second shl 8)
        }
    }

    override val fileCount: Int
        get() = files.size

    public operator fun get(name: String): CpkFileEntry? = files.firstOrNull { entry -> entry.name == name }

    public fun openRawSource(file: CpkFileEntry): DataSource<InputFlow> = WindowedDataSource(dataSource, file.fileOffset.toULong(), file.fileSize.toULong(), closeParent = false)
    public suspend fun openRawFlow(file: CpkFileEntry): KorneaResult<InputFlow> =
        dataSource.openInputFlow()
            .map { parent -> WindowedInputFlow(parent, file.fileOffset.toULong(), file.fileSize.toULong()) }

    public suspend fun SpiralContext.openDecompressedSource(file: CpkFileEntry): KorneaResult<DataSource<InputFlow>> {
        if (file.isCompressed) {
            val flow = openRawFlow(file).getOrBreak { return it.cast() }
            val compressedData = flow.readAndClose()
            val cache = cacheShortTerm(compressedData.sha256().hexLower)
            val output = cache.openOutputFlow()
                .getOrBreak {
                    cache.close()
                    return decompressCrilayla(compressedData).map { BinaryDataSource(it) }
                }

            decompressCrilayla(compressedData).map { output.write(it) }

            return KorneaResult.success(cache)
        } else {
            return KorneaResult.success(openRawSource(file))
        }
    }

    public suspend fun SpiralContext.openDecompressedFlow(file: CpkFileEntry): KorneaResult<InputFlow> {
        if (file.isCompressed) {
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


public suspend fun CpkArchive.openDecompressedSource(context: SpiralContext, file: CpkFileEntry): KorneaResult<DataSource<InputFlow>> = context.openDecompressedSource(file)

public suspend fun CpkArchive.openDecompressedFlow(context: SpiralContext, file: CpkFileEntry): KorneaResult<InputFlow> = context.openDecompressedFlow(file)

@Suppress("FunctionName")
public suspend fun SpiralContext.CpkArchive(dataSource: DataSource<*>): KorneaResult<CpkArchive> = CpkArchive(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeCpkArchive(dataSource: DataSource<*>): CpkArchive = CpkArchive(this, dataSource).getOrThrow()