package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.objects.utfTable
import org.abimon.spiral.core.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.util.*

class CustomCPK() {
    companion object {
        val WRITER_VERSION = 1
        val WRITER_REVISION = 10
        val WRITER_TVERSION = "SpiralFormats v$WRITER_VERSION.$WRITER_REVISION.0"

        val STORAGE_STRING = CPK.COLUMN_STORAGE_PERROW or CPK.COLUMN_TYPE_STRING
        val STORAGE_LONG = CPK.COLUMN_STORAGE_PERROW or CPK.COLUMN_TYPE_8BYTE
        val STORAGE_INT = CPK.COLUMN_STORAGE_PERROW or CPK.COLUMN_TYPE_4BYTE
        val STORAGE_SHORT = CPK.COLUMN_STORAGE_PERROW or CPK.COLUMN_TYPE_2BYTE

        val ZERO_STORAGE_LONG = CPK.COLUMN_STORAGE_ZERO or CPK.COLUMN_TYPE_8BYTE
        val ZERO_STORAGE_INT = CPK.COLUMN_STORAGE_ZERO or CPK.COLUMN_TYPE_4BYTE

        val NULL_TERMINATOR = 0x00.toChar()

        fun rowSizeOf(schema: Array<UTFColumnInfo>): Int = schema.fold(0) { size, column ->
            return@fold size + if (column.type and CPK.COLUMN_STORAGE_MASK == CPK.COLUMN_STORAGE_ZERO) 0 else when (column.type and CPK.COLUMN_TYPE_MASK) {
                CPK.COLUMN_TYPE_STRING -> 4
                CPK.COLUMN_TYPE_8BYTE -> 8
                CPK.COLUMN_TYPE_DATA -> 8
                CPK.COLUMN_TYPE_FLOAT -> 4
                CPK.COLUMN_TYPE_4BYTE -> 4
                CPK.COLUMN_TYPE_4BYTE2 -> 4
                CPK.COLUMN_TYPE_2BYTE -> 2
                CPK.COLUMN_TYPE_2BYTE2 -> 2
                CPK.COLUMN_TYPE_1BYTE -> 1
                CPK.COLUMN_TYPE_1BYTE2 -> 1
                else -> 0
            }
        }
    }

    val files: MutableMap<String, Pair<Long, () -> InputStream>> = HashMap()

    fun add(name: String, data: File) = add(name, data.length()) { FileInputStream(data) }
    fun add(name: String, size: Long, supplier: () -> InputStream) {
        assert(files.put(name.replace(File.separator, "/"), size to supplier) == null)
    }

    fun compile(output: OutputStream) {
        System.err.println("WARNING: If compilation doesn't work with a game, see if adding the Info and UserString headers to TOC works")
        val filenames = files.keys.sorted()
        println(filenames.size)

        val header = utfTable {
            schema = arrayOf(
                    UTFColumnInfo(ZERO_STORAGE_LONG, "UpdateDateTime"),
                    UTFColumnInfo(ZERO_STORAGE_LONG, "FileSize"),
                    UTFColumnInfo(STORAGE_LONG, "ContentOffset"),
                    UTFColumnInfo(ZERO_STORAGE_LONG, "ContentSize"),
                    UTFColumnInfo(STORAGE_LONG, "TocOffset"),
                    UTFColumnInfo(STORAGE_LONG, "TocSize"),
                    UTFColumnInfo(STORAGE_LONG, "EtocOffset"),
                    UTFColumnInfo(STORAGE_LONG, "EtocSize"),
                    UTFColumnInfo(ZERO_STORAGE_LONG, "ItocOffset"),
                    UTFColumnInfo(ZERO_STORAGE_LONG, "ItocSize"),
                    UTFColumnInfo(STORAGE_LONG, "EnabledPackedSize"),
                    UTFColumnInfo(STORAGE_LONG, "EnabledDataSize"),
                    UTFColumnInfo(ZERO_STORAGE_LONG, "TotalDataSize"),
                    UTFColumnInfo(ZERO_STORAGE_INT, "Tocs"),
                    UTFColumnInfo(STORAGE_INT, "Files"),
                    UTFColumnInfo(ZERO_STORAGE_INT, "TotalFiles"),
                    UTFColumnInfo(ZERO_STORAGE_INT, "Directories"),
                    UTFColumnInfo(ZERO_STORAGE_INT, "Updates"),
                    UTFColumnInfo(STORAGE_SHORT, "Version"),
                    UTFColumnInfo(STORAGE_SHORT, "Revision"),
                    UTFColumnInfo(STORAGE_SHORT, "Align"),
                    UTFColumnInfo(STORAGE_SHORT, "Sorted"),
                    UTFColumnInfo(STORAGE_STRING, "Tvers"),
                    UTFColumnInfo(STORAGE_STRING, "Comment")
            )

            stringTable = buildString {
                append("<NULL>")
                append(NULL_TERMINATOR)

                append("CpkHeader")
                append(NULL_TERMINATOR)

                append(schema.joinToString("$NULL_TERMINATOR", transform = UTFColumnInfo::columnName))
                append(NULL_TERMINATOR)

                append(WRITER_TVERSION)
                append(NULL_TERMINATOR)
            }

            rows = 1
            columns = schema.size
            rowWidth = rowSizeOf(schema)
            rowsOffset = 32L + (columns * 5)
            stringTableOffset = rowsOffset + rowWidth

            tableName = "CpkHeader"
            tableSize = stringTableOffset + stringTable.length
            dataOffset = tableSize
        }

        val toc = utfTable {
            schema = arrayOf(
                    UTFColumnInfo(STORAGE_STRING, "DirName"),
                    UTFColumnInfo(STORAGE_STRING, "FileName"),
                    UTFColumnInfo(STORAGE_INT, "FileSize"),
                    UTFColumnInfo(STORAGE_INT, "ExtractSize"),
                    UTFColumnInfo(STORAGE_LONG, "FileOffset"),
                    UTFColumnInfo(ZERO_STORAGE_INT, "Info"),
                    UTFColumnInfo(STORAGE_STRING, "UserString")
            )

            stringTable = buildString {
                append("<NULL>")
                append(NULL_TERMINATOR)

                append("CpkTocInfo")
                append(NULL_TERMINATOR)

                append(schema.joinToString("$NULL_TERMINATOR", transform = UTFColumnInfo::columnName))
                append(NULL_TERMINATOR)

                append(filenames.joinToString("$NULL_TERMINATOR") { name -> "${name.substringBeforeLast('/', missingDelimiterValue = "")}$NULL_TERMINATOR${name.substringAfterLast('/')}" })
                append(NULL_TERMINATOR)
            }

            rows = files.size.toLong()
            columns = schema.size
            rowWidth = rowSizeOf(schema)
            rowsOffset = 32L + (columns * 5)
            stringTableOffset = rowsOffset + (rowWidth * rows)

            tableName = "CpkTocInfo"
            tableSize = stringTableOffset + stringTable.length
            dataOffset = tableSize
        }

        val etoc = utfTable {
            schema = arrayOf(
                    UTFColumnInfo(STORAGE_LONG, "UpdateDateTime"),
                    UTFColumnInfo(STORAGE_STRING, "LocalDir")
            )

            stringTable = buildString {
                append("<NULL>")
                append(NULL_TERMINATOR)

                append("CpkEtocInfo")
                append(NULL_TERMINATOR)

                append(schema.joinToString("$NULL_TERMINATOR", transform = UTFColumnInfo::columnName))
                append(NULL_TERMINATOR)

                append(filenames.joinToString("$NULL_TERMINATOR") { name -> name.substringBeforeLast('/', missingDelimiterValue = "") })
                append(NULL_TERMINATOR)
            }

            rows = files.size + 1L
            columns = schema.size
            rowWidth = rowSizeOf(schema)
            rowsOffset = 32L + (columns * 5)
            stringTableOffset = rowsOffset + (rowWidth * rows)

            tableName = "CpkEtocInfo"
            tableSize = stringTableOffset + stringTable.length
            dataOffset = tableSize
        }

        output.writeInt32LE(CPK.MAGIC_NUMBER)
        output.writeInt32LE(0xFF)
        output.writeInt32LE(header.tableSize + 8)
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

    fun writeTable(output: OutputStream, init: UTFTableInfo.() -> Unit) = writeTable(output, UTFTableInfo().apply(init))
    fun writeTable(output: OutputStream, table: UTFTableInfo) {
        //Size = 32 + stringTable.length
        output.writeInt32LE(CPK.UTF_MAGIC_NUMBER)

        output.writeInt32BE(table.tableSize)
        output.writeUInt32BE(table.rowsOffset - 8) //So weird
        output.writeUInt32BE(table.stringTableOffset - 8) //This is still so weird
        output.writeUInt32BE(table.dataOffset)

        val stringTable = table.stringTable

        output.writeInt32BE(stringTable.indexOf(table.tableName))

        output.writeInt16BE(table.columns)
        output.writeInt16BE(table.rowWidth)
        output.writeInt32BE(table.rows)

        val stringTableRange = 0 until table.stringTable.length
        table.schema.forEach { column ->
            output.write(column.type)
            output.writeInt32BE(table.stringTable.indexOf(column.columnName).coerceIn(stringTableRange))
        }
    }

    fun writeTableDataSingleRow(output: OutputStream, table: UTFTableInfo, dataMap: Map<String, Any?>) = writeTableData(output, table, dataMap.mapValues { (_, value) -> listOf(value) })
    fun writeTableData(output: OutputStream, table: UTFTableInfo, dataMap: Map<String, List<Any?>>) {
        val stringTableRange = 0 until table.stringTable.length
        for (i in 0 until table.rows.toInt()) {
            table.schema.forEach { (type, name) ->
                if (type and CPK.COLUMN_STORAGE_MASK == CPK.COLUMN_STORAGE_ZERO)
                    return@forEach

                val data = dataMap[name]?.let { list -> if (i in list.indices) list[i] else null }

                when (CPKColumnType.getForMask(type)) {
                    CPKColumnType.TYPE_STRING -> {
                        val offset = (data?.toString()?.let { str -> table.stringTable.indexOf(str + NULL_TERMINATOR) }
                                ?: (table.stringTable.lastIndexOf(0x00.toChar()) - 1)).coerceIn(stringTableRange)
                        output.writeInt32BE(offset)
                    }
                    CPKColumnType.TYPE_DATA -> {
                        val dataPair = data as? Pair<*, *>

                        if (dataPair == null) {
                            output.writeInt64BE(0x00)
                        } else {
                            output.writeInt32BE(dataPair.first as? Int ?: 0)
                            output.writeInt32BE(dataPair.second as? Int ?: 0)
                        }
                    }
                    CPKColumnType.TYPE_8BYTE -> output.writeInt64BE(data as? Number ?: 0L)
                    CPKColumnType.TYPE_4BYTE2 -> output.writeInt32BE(data as? Number ?: 0)
                    CPKColumnType.TYPE_4BYTE -> output.writeInt32BE(data as? Number ?: 0)
                    CPKColumnType.TYPE_2BYTE2 -> output.writeInt16BE(data as? Number ?: 0)
                    CPKColumnType.TYPE_2BYTE -> output.writeInt16BE(data as? Number ?: 0)
                    CPKColumnType.TYPE_FLOAT -> output.writeFloatBE((data as? Number ?: 0).toFloat())
                    CPKColumnType.TYPE_1BYTE2 -> output.write((data as? Number ?: 0).toInt() and 0xFF)
                    CPKColumnType.TYPE_1BYTE -> output.write((data as? Number ?: 0).toInt() and 0xFF)
                    else -> throw IllegalArgumentException("Unknown column type provided for compilation!")
                }
            }
        }
    }
}