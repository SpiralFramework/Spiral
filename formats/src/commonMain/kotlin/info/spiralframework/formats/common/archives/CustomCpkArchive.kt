package info.spiralframework.formats.common.archives

import info.spiralframework.base.binding.now
import info.spiralframework.base.common.*
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.OutputFlow
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

        val TOC_MAGIC_NUMBER_LE = 0x20434f54
        val ETOC_MAGIC_NUMBER_LE = 0x434f5445

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

    val _files: MutableMap<String, DataSource<*>> = LinkedHashMap()
    val files: List<Map.Entry<String, DataSource<*>>>
        get() = _files.entries.toList()

    var writerVersion = 1
    var writerRevision = 12
    var writerTextVersion: String? = null

    operator fun set(name: String, source: DataSource<*>) {
        _files[name] = source
    }

    @ExperimentalStdlibApi
    suspend fun SpiralContext.compile(output: OutputFlow) {
        withFormats(this) {
            warn("formats.custom_cpk.header_warning")
            val writerTextVersion = writerTextVersion ?: "SpiralFormats v$writerVersion.$writerRevision.0"
            val filenames = _files.keys.toList()

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

                rowCount = files.size.toUInt()
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

                    append(schema.joinToString("$NULL_TERMINATOR", transform = UtfColumnSchema::name))
                    append(NULL_TERMINATOR)

                    append(filenames.joinToString("$NULL_TERMINATOR") { name -> name.substringBeforeLast('/', missingDelimiterValue = "") })
                    append(NULL_TERMINATOR)
                }

                rowCount = 1u + files.size
                columnCount = schema.size
                rowWidth = rowSizeOf(schema)
                rowsOffset = 32u + (columnCount * 5)
                stringOffset = (rowsOffset + (rowWidth * rowCount)).toUInt()

                name = "CpkEtocInfo"
                size = stringOffset + stringTable.length.toUInt()
                dataOffset = size
            }

            output.writeInt32LE(CpkArchive.MAGIC_NUMBER_LE)
            output.writeInt32LE(0xFF)
            output.writeInt32LE(header.size.toInt() + 8)
            output.writeInt32LE(0x00)

            val totalSize = _files.values.sumBy { source ->
                val size = source.dataSize!!
                size.alignmentNeededFor(2048).toInt()
            }

            //Write Table
            writeTable(output, header)
            writeTableDataSingleRow(output, header, mapOf(
                    "ContentOffset" to 2048,
                    "TocOffset" to 2048 + totalSize + totalSize.alignmentNeededFor(2048),
                    "TocSize" to toc.size + 0x10u,
                    "EtocOffset" to 2048u + totalSize + totalSize.alignmentNeededFor(2048) + toc.size + 16u + (toc.size + 16u).alignmentNeededFor(2048),
                    "EtocSize" to etoc.size + 0x10u,
                    "EnabledPackedSize" to totalSize,
                    "EnabledDataSize" to totalSize,
                    "Files" to files.size,
                    "Version" to writerVersion,
                    "Revision" to writerRevision,
                    "Align" to 2048,
                    "Sorted" to 1,
                    "Tvers" to writerTextVersion,
                    "Comment" to ""
            ))

            //Write String Table
            output.write(header.stringTable.encodeToByteArray())

            //Padding
            output.write(ByteArray((16 + header.size).alignmentNeededFor(2048)))

            //Write Data
            filenames.forEach { name ->
                val source = _files.getValue(name)
                val copied = source.useInputFlow(output::copyFrom) ?: return@forEach
                if (copied != source.dataSize?.toLong())
                    error("CPK: Was told to copy {0} bytes; copied {1} instead", source.dataSize.toString(), copied)

                output.write(ByteArray(copied.alignmentNeededFor(2048)))
            }

            //Pad
            output.write(ByteArray(totalSize.alignmentNeededFor(2048)))

            //Write Toc
            output.writeInt32LE(TOC_MAGIC_NUMBER_LE)
            output.writeInt32LE(0xFF)
            output.writeInt32LE(8 + toc.size)
            output.writeInt32LE(0x00)

            writeTable(output, toc)
            writeTableData(output, toc, mapOf(
                    "DirName" to filenames.map { name -> name.substringBeforeLast('/', missingDelimiterValue = "") },
                    "FileName" to filenames.map { name -> name.substringAfterLast('/') },
                    "FileSize" to filenames.map { name -> _files.getValue(name).dataSize!!.toLong() },
                    "ExtractSize" to filenames.map { name -> _files.getValue(name).dataSize!!.toLong() },
                    "FileOffset" to LongArray(filenames.size).apply {
                        var offset = 0L

                        for (i in 0 until this.size) {
                            this[i] = offset

                            val filesize = _files.getValue(filenames[i]).dataSize!!
                            offset += filesize + filesize.alignmentNeededFor(2048)
                        }
                    }.toList(),
                    "UserString" to Array(filenames.size) { "" }.toList()
            ))

            //Write String Table
            output.write(toc.stringTable.encodeToByteArray())

            //Pad
            output.write(ByteArray((toc.size + 16u).alignmentNeededFor(2048).toInt()))

            //Write Etoc
            val etocTime = CpkArchive.convertToEtocTime(Moment.now())

            output.writeInt32LE(ETOC_MAGIC_NUMBER_LE)
            output.writeInt32LE(0xFF)
            output.writeInt32LE(8 + etoc.size)
            output.writeInt32LE(0x00)

            writeTable(output, etoc)
            writeTableData(output, etoc, mapOf(
                    "UpdateDateTime" to LongArray(filenames.size) { etocTime }.toMutableList().apply { add(0L) },
                    "LocalDir" to filenames.map { name -> name.substringBeforeLast('/', missingDelimiterValue = "") }.toMutableList().apply { add("$NULL_TERMINATOR$NULL_TERMINATOR") }
            ))

            //Write String Table
            output.write(etoc.stringTable.encodeToByteArray())
        }
    }

    suspend fun SpiralContext.writeTable(output: OutputFlow, init: UtfTableSchemaBuilder.() -> Unit) = writeTable(output, utfTableSchema(init))
    suspend fun SpiralContext.writeTable(output: OutputFlow, table: UtfTableSchema) {
        //Size = 32 + stringTable.length
        output.writeInt32LE(UtfTableInfo.UTF_MAGIC_NUMBER_LE)

        output.writeInt32BE(table.size.toInt())
        output.writeUInt32BE(table.rowsOffset.toInt() - 8) //So weird
        output.writeUInt32BE(table.stringOffset.toInt() - 8) //This is still so weird
        output.writeUInt32BE(table.dataOffset.toInt())

        val stringTable = table.stringTable

        output.writeInt32BE(stringTable.indexOf(table.name))

        output.writeInt16BE(table.columnCount)
        output.writeInt16BE(table.rowWidth)
        output.writeInt32BE(table.rowCount.toInt())

        val stringTableRange = 0 until table.stringTable.length
        table.schema.forEach { column ->
            output.write(column.type)
            output.writeInt32BE(table.stringTable.indexOf(column.name).coerceIn(stringTableRange))
        }
    }

    suspend fun SpiralContext.writeTableDataSingleRow(output: OutputFlow, table: UtfTableSchema, dataMap: Map<String, Any?>) = writeTableData(output, table, dataMap.mapValues { (_, value) -> listOf(value) })
    suspend fun SpiralContext.writeTableData(output: OutputFlow, table: UtfTableSchema, dataMap: Map<String, List<Any?>>) {
        val stringTableRange = 0 until table.stringTable.length
        for (i in 0 until table.rowCount.toInt()) {
            table.schema.forEach { schema ->
                if (schema.type and UtfTableInfo.COLUMN_STORAGE_MASK == UtfTableInfo.COLUMN_STORAGE_ZERO)
                    return@forEach

                val data = dataMap[schema.name]?.getOrNull(i)

                when (schema.type and UtfTableInfo.COLUMN_TYPE_MASK) {
                    UtfTableInfo.COLUMN_TYPE_STRING -> {
                        val offset = (data?.toString()?.let { str -> table.stringTable.indexOf(str + NULL_TERMINATOR) }
                                ?: (table.stringTable.lastIndexOf(0x00.toChar()) - 1)).coerceIn(stringTableRange)
                        output.writeInt32BE(offset)
                    }
                    UtfTableInfo.COLUMN_TYPE_DATA -> {
                        val dataPair = data as? Pair<*, *>

                        if (dataPair == null) {
                            output.writeInt64BE(0x00)
                        } else {
                            output.writeInt32BE(dataPair.first as? Int ?: 0)
                            output.writeInt32BE(dataPair.second as? Int ?: 0)
                        }
                    }
                    UtfTableInfo.COLUMN_TYPE_8BYTE -> output.writeInt64BE(data as? Number ?: 0L)
                    UtfTableInfo.COLUMN_TYPE_4BYTE2 -> output.writeInt32BE(data as? Number ?: 0)
                    UtfTableInfo.COLUMN_TYPE_4BYTE -> output.writeInt32BE(data as? Number ?: 0)
                    UtfTableInfo.COLUMN_TYPE_2BYTE2 -> output.writeInt16BE(data as? Number ?: 0)
                    UtfTableInfo.COLUMN_TYPE_2BYTE -> output.writeInt16BE(data as? Number ?: 0)
                    UtfTableInfo.COLUMN_TYPE_FLOAT -> output.writeFloatBE((data as? Number ?: 0).toFloat())
                    UtfTableInfo.COLUMN_TYPE_1BYTE2 -> output.write((data as? Number ?: 0).toInt() and 0xFF)
                    UtfTableInfo.COLUMN_TYPE_1BYTE -> output.write((data as? Number ?: 0).toInt() and 0xFF)
                    else -> throw IllegalArgumentException(localise("formats.custom_cpk.unknown_column_constant", schema.type))
                }
            }
        }
    }
}

@ExperimentalStdlibApi
suspend fun CustomCpkArchive.compile(context: SpiralContext, output: OutputFlow) = context.compile(output)