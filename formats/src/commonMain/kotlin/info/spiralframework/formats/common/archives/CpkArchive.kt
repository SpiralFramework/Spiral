package info.spiralframework.formats.common.archives

import com.soywiz.krypto.sha256
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.WindowedInputFlow
import info.spiralframework.base.common.io.flow.readBytes
import info.spiralframework.base.common.io.flow.setCloseHandler
import info.spiralframework.base.common.toHexString
import info.spiralframework.formats.common.compression.decompressCrilayla
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
class CpkArchive(val header: UtfTableInfo, val tocHeader: UtfTableInfo, val etocHeader: UtfTableInfo?, val itocHeader: UtfTableInfo?, val gtocHeader: UtfTableInfo?, val files: Array<CpkFileEntry>, val dataSource: DataSource<*>) {
    companion object {
        const val MAGIC_NUMBER_LE = 0x204b5043

        const val TOC_OFFSET_COLUMN = "TocOffset"
        const val ETOC_OFFSET_COLUMN = "EtocOffset"
        const val ITOC_OFFSET_COLUMN = "ItocOffset"
        const val GTOC_OFFSET_COLUMN = "GtocOffset"
        const val CONTENT_OFFSET_COLUMN = "ContentOffset"
        const val FILES_COLUMN = "Files"

        const val FILENAME_COLUMN = "FileName"
        const val DIRECTORY_NAME_COLUMN = "DirName"
        const val FILE_SIZE_COLUMN = "FileSize"
        const val EXTRACT_SIZE_COLUMN = "ExtractSize"
        const val FILE_OFFSET_COLUMN = "FileOffset"

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): CpkArchive? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.cpk.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): CpkArchive {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.cpk.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val magic = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(magic == MAGIC_NUMBER_LE) { localise("formats.cpk.invalid_magic", "0x${magic.toString(16)}", "0x${MAGIC_NUMBER_LE.toString(16)}") }

                    val headerInfo = UtfTableInfo.unsafe(this, OffsetDataSource(dataSource, 0x10u))
                    require(headerInfo.rowCount == 1u) { localise("formats.cpk.invalid_header_row_count", headerInfo.rowCount) }

                    val tocOffset = (headerInfo.readRowDataUnsafe(this, 0, TOC_OFFSET_COLUMN) as UtfRowData.TypeLong).data
                    val etocOffset = (headerInfo.readRowData(this, 0, ETOC_OFFSET_COLUMN) as? UtfRowData.TypeLong)?.data
                    val itocOffset = (headerInfo.readRowData(this, 0, ITOC_OFFSET_COLUMN) as? UtfRowData.TypeLong)?.data
                    val gtocOffset = (headerInfo.readRowData(this, 0, GTOC_OFFSET_COLUMN) as? UtfRowData.TypeLong)?.data

                    val contentOffset = (headerInfo.readRowDataUnsafe(this, 0, CONTENT_OFFSET_COLUMN) as UtfRowData.TypeLong).data
                    val fileCount = (headerInfo.readRowDataUnsafe(this, 0, FILES_COLUMN) as UtfRowData.TypeInt).data

                    val tocHeader = UtfTableInfo.unsafe(this, OffsetDataSource(dataSource, tocOffset.toULong() + 0x10u))
                    require(tocHeader.rowCount == fileCount.toUInt()) { localise("formats.cpk.invalid_toc_header_row_count", tocHeader.rowCount, fileCount) }

                    val etocHeader = if (etocOffset != null && etocOffset != 0L) UtfTableInfo.unsafe(this, OffsetDataSource(dataSource, etocOffset.toULong() + 0x10u)) else null
                    val itocHeader = if (itocOffset != null && itocOffset != 0L) UtfTableInfo.unsafe(this, OffsetDataSource(dataSource, itocOffset.toULong() + 0x10u)) else null
                    val gtocHeader = if (gtocOffset != null && gtocOffset != 0L) UtfTableInfo.unsafe(this, OffsetDataSource(dataSource, gtocOffset.toULong() + 0x10u)) else null

                    val files = Array(tocHeader.rowCount.toInt()) { index ->
                        val fileName = (tocHeader.readRowDataUnsafe(this, index, FILENAME_COLUMN) as UtfRowData.TypeString).data
                        val directoryName = (tocHeader.readRowDataUnsafe(this, index, DIRECTORY_NAME_COLUMN) as UtfRowData.TypeString).data
                        val fileSize = (tocHeader.readRowDataUnsafe(this, index, FILE_SIZE_COLUMN) as UtfRowData.TypeInt).data
                        val extractSize = (tocHeader.readRowDataUnsafe(this, index, EXTRACT_SIZE_COLUMN) as UtfRowData.TypeInt).data
                        val fileOffset = (tocHeader.readRowDataUnsafe(this, index, FILE_OFFSET_COLUMN) as UtfRowData.TypeLong).data + minOf(contentOffset, tocOffset)

                        CpkFileEntry(fileName, directoryName, fileSize, extractSize, fileOffset)
                    }

                    return CpkArchive(headerInfo, tocHeader, etocHeader, itocHeader, gtocHeader, files, dataSource)
                }
            }
        }

        @ExperimentalStdlibApi
        suspend fun SpiralContext.dumpTable(table: UtfTableInfo, indentLevel: Int = 0) {
            val indents = buildString { repeat(indentLevel) { append("\t") } }
            println("$indents==[${table.name}]==")
            table.schema.forEach { column ->
                val data = table.readRowDataUnsafe(this, 0, column)
                if (data is UtfRowData.TypeData) {
//                    println("Data: ${data.data.joinToString(" ") { it.toInt().and(0xFF).toString(16).padStart(2, '0').toUpperCase() }}")
                    dumpTable(UtfTableInfo.unsafe(this, BinaryDataSource(data.data)), indentLevel + 1)
                } else {
                    println("$indents${column.name}: $data")
                }
            }
            println("$indents==[/${table.name}]==")
        }
    }

    operator fun get(name: String): CpkFileEntry? = files.firstOrNull { entry -> entry.name == name }

    suspend fun SpiralContext.openRawSource(file: CpkFileEntry): DataSource<out InputFlow> = WindowedDataSource(dataSource, file.fileOffset.toULong(), file.fileSize.toULong())
    suspend fun SpiralContext.openRawFlow(file: CpkFileEntry): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, file.fileOffset.toULong(), file.fileSize.toULong())
    }

    suspend fun SpiralContext.openDecompressedSource(file: CpkFileEntry): DataSource<out InputFlow>? {
        if (file.isCompressed) {
            val flow = openRawFlow(file) ?: return null
            val compressedData = use(flow) { flow.readBytes() }
            val cache = cacheShortTerm(compressedData.sha256().toHexString())
            val output = cache.openOutputFlow()
            if (output == null) {
                cache.close()
                return null
            }

            output.write(decompressCrilayla(compressedData))

            return cache
        } else {
            return openRawSource(file)
        }
    }

    suspend fun SpiralContext.openDecompressedFlow(file: CpkFileEntry): InputFlow? {
        if (file.isCompressed) {
            val source = openDecompressedSource(file) ?: return null
            val input = source.openInputFlow()
            if (input == null) {
                source.close()
                return null
            }

            input.setCloseHandler { source.close() }
            return input
        } else {
            return openRawFlow(file)
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun CpkArchive.openRawSource(context: SpiralContext, file: CpkFileEntry) = context.openRawSource(file)

@ExperimentalUnsignedTypes
suspend fun CpkArchive.openRawFlow(context: SpiralContext, file: CpkFileEntry) = context.openRawFlow(file)

@ExperimentalUnsignedTypes
suspend fun CpkArchive.openDecompressedSource(context: SpiralContext, file: CpkFileEntry) = context.openDecompressedSource(file)

@ExperimentalUnsignedTypes
suspend fun CpkArchive.openDecompressedFlow(context: SpiralContext, file: CpkFileEntry) = context.openDecompressedFlow(file)