package info.spiralframework.formats.archives

import info.spiralframework.base.common.NULL_TERMINATOR
import info.spiralframework.base.jvm.io.CountingInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.with
import info.spiralframework.base.util.*
import info.spiralframework.formats.common.SPIRAL_FORMATS_MODULE
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.DataSource
import java.io.InputStream
import java.time.LocalDateTime

@ExperimentalUnsignedTypes
class CPK private constructor(context: SpiralContext, val dataSource: () -> InputStream) : IArchive {
    companion object {
        val MAGIC_NUMBER = 0x204b5043
        val UTF_MAGIC_NUMBER = 0x46545540
        val TOC_MAGIC_NUMBER = 0x20434f54
        val ETOC_MAGIC_NUMBER = 0x434f5445

        val COLUMN_STORAGE_MASK = 0xF0
        val COLUMN_STORAGE_PERROW = 0x50
        val COLUMN_STORAGE_CONSTANT = 0x30
        val COLUMN_STORAGE_ZERO = 0x10

        val COLUMN_TYPE_MASK = 0x0F
        val COLUMN_TYPE_DATA = 0x0B
        val COLUMN_TYPE_STRING = 0x0A
        val COLUMN_TYPE_FLOAT = 0x08
        val COLUMN_TYPE_8BYTE = 0x06
        val COLUMN_TYPE_4BYTE2 = 0x05
        val COLUMN_TYPE_4BYTE = 0x04
        val COLUMN_TYPE_2BYTE2 = 0x03
        val COLUMN_TYPE_2BYTE = 0x02
        val COLUMN_TYPE_1BYTE2 = 0x01
        val COLUMN_TYPE_1BYTE = 0x00

        val TOC_OFFSET_COLUMN = "TocOffset"
        val ETOC_OFFSET_COLUMN = "EtocOffset"
        val CONTENT_OFFSET_COLUMN = "ContentOffset"
        val FILES_COLUMN = "Files"

        val FILENAME_COLUMN = "FileName"
        val DIRECTORY_NAME_COLUMN = "DirName"
        val FILE_SIZE_COLUMN = "FileSize"
        val EXTRACT_SIZE_COLUMN = "ExtractSize"
        val FILE_OFFSET_COLUMN = "FileOffset"

        fun sanitiseStringTable(table: String, index: Int): String {
            if (index > 0 && index < table.length)
                return table.substring(index).substringBefore(NULL_TERMINATOR)
            else {
                return table.substringBefore(NULL_TERMINATOR)
            }
        }

        fun SpiralContext.readTable(offset: Long, dataSource: () -> InputStream): UTFTableInfo =
                with(SPIRAL_FORMATS_MODULE) {
                    CountingInputStream(dataSource()).use { stream ->
                        stream.skip(offset)

                        val magic = stream.readInt32LE()
                        assert(magic == UTF_MAGIC_NUMBER)

                        val info = UTFTableInfo()

                        info.tableOffset = offset
                        info.tableSize = stream.readUInt32BE().toLong()
                        info.schemaOffset = 0x20
                        info.rowsOffset = stream.readUInt32BE().toLong()
                        info.stringTableOffset = stream.readUInt32BE().toLong()
                        info.dataOffset = stream.readUInt32BE().toLong()

                        val tableNameString = stream.readInt32BE()

                        info.columns = stream.readInt16BE()
                        info.rowWidth = stream.readInt16BE()
                        info.rows = stream.readUInt32BE().toLong()

                        info.stringTable = dataSource().use str@{ stringStream ->
                            stringStream.skip(8 + info.stringTableOffset + offset)
                            return@str stringStream.readString((info.dataOffset - info.stringTableOffset).toInt() + 1)
                        }

                        info.tableName = info.stringTable.substring(tableNameString).substringBefore(NULL_TERMINATOR)

                        info.schema = Array(info.columns) {
                            val column = UTFColumnInfo()

                            column.type = stream.read()
                            column.columnName = info.stringTable.substring(stream.readInt32BE()).substringBefore(NULL_TERMINATOR)

                            if (column.type and COLUMN_STORAGE_MASK == COLUMN_STORAGE_CONSTANT) {
                                column.constantOffset = stream.streamOffset

                                when (column.type and COLUMN_TYPE_MASK) {
                                    COLUMN_TYPE_STRING -> stream.skip(4)
                                    COLUMN_TYPE_8BYTE -> stream.skip(8)
                                    COLUMN_TYPE_DATA -> stream.skip(8)
                                    COLUMN_TYPE_FLOAT -> stream.skip(4)
                                    COLUMN_TYPE_4BYTE -> stream.skip(4)
                                    COLUMN_TYPE_4BYTE2 -> stream.skip(4)
                                    COLUMN_TYPE_2BYTE -> stream.skip(2)
                                    COLUMN_TYPE_2BYTE2 -> stream.skip(2)
                                    COLUMN_TYPE_1BYTE -> stream.read()
                                    COLUMN_TYPE_1BYTE2 -> stream.read()
                                    else -> debug("formats.cpk.unknown_column_constant", column.type)
                                }
                            }

                            return@Array column
                        }

                        return@use info
                    }
                }

        val VALID_MONTHS = 1L..12
        val VALID_DAYS = 1L..31
        val VALID_HOURS = 0L until 24
        val VALID_MINUTES = 0L until 60
        val VALID_SECONDS = 0L until 60

        fun convertFromEtocTime(time: Long): LocalDateTime {
            val year = ((time shr 48)).toString().padStart(4, '0')
            val month = ((time shr 40) and 0xFF).coerceIn(VALID_MONTHS).toString().padStart(2, '0')
            val day = ((time shr 32) and 0xFF).coerceIn(VALID_DAYS).toString().padStart(2, '0')
            val hour = ((time shr 24) and 0xFF).coerceIn(VALID_HOURS).toString().padStart(2, '0')
            val minute = ((time shr 16) and 0xFF).coerceIn(VALID_MINUTES).toString().padStart(2, '0')
            val second = ((time shr 8) and 0xFF).coerceIn(VALID_SECONDS).toString().padStart(2, '0')

            return LocalDateTime.parse("${year}-${month}-${day}T${hour}:${minute}:${second}")
        }

        fun convertToEtocTime(time: LocalDateTime): Long {
            val year = time.year.toLong()
            val month = time.monthValue.toLong()
            val day = time.dayOfMonth.toLong()
            val hour = time.hour.toLong()
            val minute = time.minute.toLong()
            val second = time.second.toLong()

            return (year shl 48) or (month shl 40) or (day shl 32) or (hour shl 24) or (minute shl 16) or (second shl 8)
        }

        operator fun invoke(context: SpiralContext, dataSource: DataSource): CPK? {
            withFormats(context) {
                try {
                    return CPK(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.cpk.invalid", dataSource, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, dataSource: DataSource): CPK = withFormats(context) { CPK(this, dataSource) }
    }

    val headerInfo: UTFTableInfo
    val tocHeader: UTFTableInfo
    val etocHeader: UTFTableInfo?

    val files: Array<CPKFileEntry>

    init {
        with(context) {
            val stream = dataSource()

            try {
                val magic = stream.readInt32LE()
                require(magic == MAGIC_NUMBER) { localise("formats.cpk.invalid_magic", magic, MAGIC_NUMBER) }

                headerInfo = readTable(0x10, dataSource)
                require(headerInfo.rows == 1L) { localise("formats.cpk.invalid_header_row_count", headerInfo.rows) }

                val tocOffset = requireNotNull((headerInfo.getRow(this, dataSource, 0, TOC_OFFSET_COLUMN)?.second as? Number)?.toLong())
                val etocOffset = (headerInfo.getRow(this, dataSource, 0, ETOC_OFFSET_COLUMN)?.second as? Number)?.toLong()
                val contentOffset = requireNotNull((headerInfo.getRow(this, dataSource, 0, CONTENT_OFFSET_COLUMN)?.second as? Number)?.toLong())
                val fileCount = requireNotNull((headerInfo.getRow(this, dataSource, 0, FILES_COLUMN)?.second as? Number)?.toLong())

                tocHeader = readTable(tocOffset + 0x10, dataSource)
                require(tocHeader.rows == fileCount) { localise("formats.cpk.invalid_toc_header_row_count", tocHeader.rows, fileCount) }

                etocHeader = etocOffset?.let { offset -> readTable(offset + 0x10, dataSource) }

                files = Array(tocHeader.rows.toInt()) { i ->
                    val filename = tocHeader.getRow(this, dataSource, i, FILENAME_COLUMN)?.second as? String
                            ?: tocHeader.stringTable.substringBefore(NULL_TERMINATOR)
                    val dirname = tocHeader.getRow(this, dataSource, i, DIRECTORY_NAME_COLUMN)?.second as? String
                            ?: tocHeader.stringTable.substringBefore(NULL_TERMINATOR)
                    val fileSize = (tocHeader.getRow(this, dataSource, i, FILE_SIZE_COLUMN)?.second as? Number ?: -1).toLong()
                    val extractSize = (tocHeader.getRow(this, dataSource, i, EXTRACT_SIZE_COLUMN)?.second as? Number
                            ?: -1).toLong()
                    val fileOffset = (tocHeader.getRow(this, dataSource, i, FILE_OFFSET_COLUMN)?.second as? Number
                            ?: -1).toLong() + minOf(contentOffset, tocOffset)

                    val isCompressed = extractSize > fileSize

                    return@Array CPKFileEntry(this, filename, dirname, fileSize, extractSize, fileOffset, isCompressed, this@CPK)
                }
            } finally {
                stream.close()
            }
        }
    }
}