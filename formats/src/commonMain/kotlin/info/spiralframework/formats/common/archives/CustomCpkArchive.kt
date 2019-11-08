package info.spiralframework.formats.common.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.base.common.io.writeInt32LE
import info.spiralframework.formats.common.NULL_TERMINATOR
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
open class CustomCpkArchive {
    companion object {
        val STORAGE_STRING = UtfTableInfo.COLUMN_STORAGE_PERROW or UtfTableInfo.COLUMN_TYPE_STRING
        val STORAGE_LONG = UtfTableInfo.COLUMN_STORAGE_PERROW or UtfTableInfo.COLUMN_TYPE_8BYTE
        val STORAGE_INT = UtfTableInfo.COLUMN_STORAGE_PERROW or UtfTableInfo.COLUMN_TYPE_4BYTE
        val STORAGE_SHORT = UtfTableInfo.COLUMN_STORAGE_PERROW or UtfTableInfo.COLUMN_TYPE_2BYTE

        val ZERO_STORAGE_LONG = UtfTableInfo.COLUMN_STORAGE_ZERO or UtfTableInfo.COLUMN_TYPE_8BYTE
        val ZERO_STORAGE_INT = UtfTableInfo.COLUMN_STORAGE_ZERO or UtfTableInfo.COLUMN_TYPE_4BYTE

        fun rowSizeOf(schema: Array<out UtfColumnSchema>): Int = schema.sumBy { column ->
            if (column.type and UtfTableInfo.COLUMN_STORAGE_MASK == UtfTableInfo.COLUMN_STORAGE_ZERO)
                0
            else
                when (column.type and UtfTableInfo.COLUMN_TYPE_MASK) {
                    UtfTableInfo.COLUMN_TYPE_STRING -> 4
                    UtfTableInfo.COLUMN_TYPE_8BYTE -> 8
                    UtfTableInfo.COLUMN_TYPE_DATA -> 8
                    UtfTableInfo.COLUMN_TYPE_FLOAT -> 4
                    UtfTableInfo.COLUMN_TYPE_4BYTE -> 4
                    UtfTableInfo.COLUMN_TYPE_4BYTE2 -> 4
                    UtfTableInfo.COLUMN_TYPE_2BYTE -> 2
                    UtfTableInfo.COLUMN_TYPE_2BYTE2 -> 2
                    UtfTableInfo.COLUMN_TYPE_1BYTE -> 1
                    UtfTableInfo.COLUMN_TYPE_1BYTE2 -> 1
                    else -> 0
                }
        }
    }

    var writerVersion = 1
    var writerRevision = 12
    var writerTextVersion: String? = null

    suspend fun SpiralContext.compile(output: OutputFlow) {
        withFormats(this) {
            warn("formats.custom_cpk.header_warning")
            val writerTextVersion = writerTextVersion ?: "SpiralFormats v$writerVersion.$writerRevision.0"
            val filenames = files.keys.toList()

            val header = utfTableSchema {
                schema(
                        UtfColumnSchema("UpdateDateTime", ZERO_STORAGE_LONG),
                        UtfColumnSchema("FileSize", ZERO_STORAGE_LONG),
                        UtfColumnSchema("ContentOffset", STORAGE_LONG),
                        UtfColumnSchema("ContentSize", ZERO_STORAGE_LONG),
                        UtfColumnSchema("TocOffset", STORAGE_LONG),
                        UtfColumnSchema("TocSize", STORAGE_LONG),
                        UtfColumnSchema("EtocOffset", STORAGE_LONG),
                        UtfColumnSchema("EtocSize", STORAGE_LONG),
                        UtfColumnSchema("ItocOffset", ZERO_STORAGE_LONG),
                        UtfColumnSchema("ItocSize", ZERO_STORAGE_LONG),
                        UtfColumnSchema("EnabledPackedSize", STORAGE_LONG),
                        UtfColumnSchema("EnabledDataSize", STORAGE_LONG),
                        UtfColumnSchema("TotalDataSize", ZERO_STORAGE_LONG),
                        UtfColumnSchema("Tocs", ZERO_STORAGE_INT),
                        UtfColumnSchema("Files", STORAGE_INT),
                        UtfColumnSchema("TotalFiles", ZERO_STORAGE_INT),
                        UtfColumnSchema("Directories", ZERO_STORAGE_INT),
                        UtfColumnSchema("Updates", ZERO_STORAGE_INT),
                        UtfColumnSchema("Version", STORAGE_SHORT),
                        UtfColumnSchema("Revision", STORAGE_SHORT),
                        UtfColumnSchema("Align", STORAGE_SHORT),
                        UtfColumnSchema("Sorted", STORAGE_SHORT),
                        UtfColumnSchema("Tvers", STORAGE_STRING),
                        UtfColumnSchema("Comment", STORAGE_STRING)
                )

                stringTable = buildString {
                    append("<NULL>")
                    append(NULL_TERMINATOR)

                    append("CpkHeader")
                    append(NULL_TERMINATOR)

                    append(schema.joinToString("$NULL_TERMINATOR", transform = UtfColumnSchema::name))
                    append(NULL_TERMINATOR)

                    append(writerTextVersion)
                    append(NULL_TERMINATOR)
                }

                rowCount = 1u
                columnCount = schema.size
                rowWidth = rowSizeOf(schema)
                rowsOffset = (32 + (columnCount * 5)).toUInt()
                stringOffset = rowsOffset + rowWidth.toUInt()

                name = "CpkHeader"
                size = stringOffset + stringTable.length.toUInt()
                dataOffset = size
            }

            val toc = utfTableSchema {
                schema(
                        UtfColumnSchema("DirName", STORAGE_STRING),
                        UtfColumnSchema("FileName", STORAGE_STRING),
                        UtfColumnSchema("FileSize", STORAGE_INT),
                        UtfColumnSchema("ExtractSize", STORAGE_INT),
                        UtfColumnSchema("FileOffset", STORAGE_LONG),
                        UtfColumnSchema("Info", ZERO_STORAGE_INT),
                        UtfColumnSchema("UserString", STORAGE_STRING)
                )

                stringTable = buildString {
                    append("<NULL>")
                    append(NULL_TERMINATOR)

                    append("CpkTocInfo")
                    append(NULL_TERMINATOR)

                    append(schema.joinToString("$NULL_TERMINATOR", transform = UtfColumnSchema::name))
                    append(NULL_TERMINATOR)

                    append(filenames.joinToString("$NULL_TERMINATOR") { name -> "${name.substringBeforeLast('/', missingDelimiterValue = "")}$NULL_TERMINATOR${name.substringAfterLast('/')}" })
                    append(NULL_TERMINATOR)
                }

                rowCount = files.size.toLong()
                columnCount = schema.size
                rowWidth = rowSizeOf(schema)
                rowsOffset = (32L + (columnCount * 5)).toUInt()
                stringOffset = rowsOffset + (rowWidth.toUInt() * rowCount)

                name = "CpkTocInfo"
                size = stringOffset + stringTable.length.toUInt()
                dataOffset = size
            }

            val etoc = utfTableSchema {
                schema(
                        UtfColumnSchema("UpdateDateTime", STORAGE_LONG),
                        UtfColumnSchema("LocalDir", STORAGE_STRING)
                )

                stringTable = buildString {
                    append("<NULL>")
                    append(NULL_TERMINATOR)

                    append("CpkEtocInfo")
                    append(NULL_TERMINATOR)

                    append(schema.joinToString("$NULL_TERMINATOR", transform = UtfColumnSchema:name))
                    append(NULL_TERMINATOR)

                    append(filenames.joinToString("$NULL_TERMINATOR") { name -> name.substringBeforeLast('/', missingDelimiterValue = "") })
                    append(NULL_TERMINATOR)
                }

                rowCount = files.size + 1L
                columnCount = schema.size
                rowWidth = rowSizeOf(schema)
                rowsOffset = (32L + (columnCount * 5)).toUInt()
                stringOffset = rowsOffset + (rowWidth.toUInt() * rowCount)

                name = "CpkEtocInfo"
                size = stringOffset + stringTable.length.toUInt()
                dataOffset = size
            }

            output.writeInt32LE(CpkArchive.MAGIC_NUMBER_LE)
            output.writeInt32LE(0xFF)
            output.writeInt32LE(header.size.toInt() + 8)
            output.writeInt32LE(0x00)

            val totalSize = files.values.fold(0L) { count, (size) -> count + size + size.align(2048) }

            //Write Table
            writeTable(output, header)
            writeTableDataSingleRow(output, header, mapOf(
                    "ContentOffset" to 2048,
                    "TocOffset" to 2048 + totalSize + totalSize.align(2048),
                    "TocSize" to toc.tableSize + 0x10,
                    "EtocOffset" to 2048 + totalSize + totalSize.align(2048) + toc.tableSize + 16 + (toc.tableSize + 16).align(2048),
                    "EtocSize" to etoc.tableSize + 0x10,
                    "EnabledPackedSize" to totalSize,
                    "EnabledDataSize" to totalSize,
                    "Files" to files.size,
                    "Version" to WRITER_VERSION,
                    "Revision" to WRITER_REVISION,
                    "Align" to 2048,
                    "Sorted" to 1,
                    "Tvers" to WRITER_TVERSION,
                    "Comment" to ""
            ))

            //Write String Table
            output.write(header.stringTable.toByteArray())

            //Padding
            output.write(ByteArray((16 + header.tableSize).align(2048)))

            //Write Data
            filenames.forEach { name ->
                val (size, source) = files[name] ?: return@forEach
                val copied = source.use { stream -> stream.copyTo(output) }
                if (copied != size)
                    System.err.println("ERR: Compiled $name and copied $copied")

                output.write(ByteArray(copied.align(2048)))
            }

            //Pad
            output.write(ByteArray(totalSize.align(2048)))

            //Write Toc
            output.writeInt32LE(CPK.TOC_MAGIC_NUMBER)
            output.writeInt32LE(0xFF)
            output.writeInt32LE(toc.tableSize + 8)
            output.writeInt32LE(0x00)

            writeTable(output, toc)
            writeTableData(output, toc, mapOf(
                    "DirName" to filenames.map { name -> name.substringBeforeLast('/', missingDelimiterValue = "") },
                    "FileName" to filenames.map { name -> name.substringAfterLast('/') },
                    "FileSize" to filenames.map { name -> files[name]?.first ?: 0L },
                    "ExtractSize" to filenames.map { name -> files[name]?.first ?: 0L },
                    "FileOffset" to LongArray(filenames.size).apply {
                        var offset = 0L

                        for (i in 0 until this.size) {
                            this[i] = offset

                            val filesize = files[filenames[i]]?.first ?: 0L
                            offset += filesize + filesize.align(2048)
                        }
                    }.toList(),
                    "UserString" to Array(filenames.size) { "" }.toList()
            ))

            //Write String Table
            output.write(toc.stringTable.toByteArray())

            //Pad
            output.write(ByteArray((toc.tableSize + 16).align(2048)))

            //Write Etoc
            val etocTime = CPK.convertToEtocTime(LocalDateTime.now())

            output.writeInt32LE(CPK.ETOC_MAGIC_NUMBER)
            output.writeInt32LE(0xFF)
            output.writeInt32LE(etoc.tableSize + 8)
            output.writeInt32LE(0x00)

            writeTable(output, etoc)
            writeTableData(output, etoc, mapOf(
                    "UpdateDateTime" to LongArray(filenames.size) { etocTime }.toMutableList().apply { add(0L) },
                    "LocalDir" to filenames.map { name -> name.substringBeforeLast('/', missingDelimiterValue = "") }.toMutableList().apply { add("$NULL_TERMINATOR$NULL_TERMINATOR") }
            ))

            //Write String Table
            output.write(etoc.stringTable.toByteArray())
        }
    }
}