package info.spiralframework.formats.common.archives

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.NULL_TERMINATOR
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.fauxSeekFromStart
import org.abimon.kornea.io.common.flow.offsetPosition

open class UtfTableSchema(open val name: String, open val size: UInt, open val schemaOffset: UInt, open val rowsOffset: UInt, open val stringTable: String, open val stringOffset: UInt, open val dataOffset: UInt, open val columnCount: Int, open val rowWidth: Int, open val rowCount: UInt, open val schema: Array<out UtfColumnSchema>)

@ExperimentalUnsignedTypes
data class UtfTableInfo(
        override val name: String,
        override val size: UInt,
        override val schemaOffset: UInt,
        override val rowsOffset: UInt,
        override val stringTable: String,
        override val stringOffset: UInt,
        override val dataOffset: UInt,
        override val columnCount: Int,
        override val rowWidth: Int,
        override val rowCount: UInt,
        override val schema: Array<out UtfColumnInfo>,
        val dataSource: DataSource<*>
) : UtfTableSchema(name, size, schemaOffset, rowsOffset, stringTable, stringOffset, dataOffset, columnCount, rowWidth, rowCount, schema) {
    companion object {
        const val UTF_MAGIC_NUMBER_LE = 0x46545540

        const val COLUMN_STORAGE_MASK = 0xF0
        const val COLUMN_STORAGE_PERROW = 0x50
        const val COLUMN_STORAGE_CONSTANT = 0x30
        const val COLUMN_STORAGE_ZERO = 0x10

        const val COLUMN_TYPE_MASK = 0x0F
        const val COLUMN_TYPE_DATA = 0x0B
        const val COLUMN_TYPE_STRING = 0x0A
        const val COLUMN_TYPE_FLOAT = 0x08
        const val COLUMN_TYPE_8BYTE = 0x06
        const val COLUMN_TYPE_4BYTE2 = 0x05
        const val COLUMN_TYPE_4BYTE = 0x04
        const val COLUMN_TYPE_2BYTE2 = 0x03
        const val COLUMN_TYPE_2BYTE = 0x02
        const val COLUMN_TYPE_1BYTE2 = 0x01
        const val COLUMN_TYPE_1BYTE = 0x00

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): UtfTableInfo? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.utf_table.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): UtfTableInfo {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.utf_table.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val magic = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(magic == UTF_MAGIC_NUMBER_LE) { localise("formats.utf_table.invalid_magic", "0x${magic.toString(16)}", "0x${UTF_MAGIC_NUMBER_LE.toString(16)}", "0x${flow.offsetPosition().minus(4u).toString(16)}") }

                    val tableSize = requireNotNull(flow.readUInt32BE(), notEnoughData)
                    val schemaOffset = 0x20u
                    val rowsOffset = requireNotNull(flow.readUInt32BE(), notEnoughData)
                    val stringTableOffset = requireNotNull(flow.readUInt32BE(), notEnoughData)
                    val dataOffset = requireNotNull(flow.readUInt32BE(), notEnoughData)

                    val tableNameOffset = requireNotNull(flow.readInt32BE(), notEnoughData)

                    val columnCount = requireNotNull(flow.readInt16BE(), notEnoughData)
                    val rowWidth = requireNotNull(flow.readInt16BE(), notEnoughData)
                    val rowCount = requireNotNull(flow.readUInt32BE(), notEnoughData)

                    val stringTable = flow.fauxSeekFromStart((8u + stringTableOffset).toULong(), dataSource) { stringFlow ->
                        stringFlow.readString((dataOffset - stringTableOffset).toInt() + 1, encoding = TextCharsets.UTF_8)
                    }
                    requireNotNull(stringTable, notEnoughData)

                    val tableName = stringTable.substring(tableNameOffset).substringBefore(NULL_TERMINATOR)

                    var rowPosition = 0
                    val schema = Array(columnCount) {
                        val columnType = requireNotNull(flow.read(), notEnoughData)
                        val columnName = stringTable.substring(requireNotNull(flow.readInt32BE(), notEnoughData))
                                .substringBefore(NULL_TERMINATOR)

                        val columnWidth: Int

                        if (columnType and COLUMN_STORAGE_MASK == COLUMN_STORAGE_ZERO) {
                            columnWidth = 0
                        } else {
                            columnWidth = when (columnType and COLUMN_TYPE_MASK) {
                                COLUMN_TYPE_STRING -> 4
                                COLUMN_TYPE_8BYTE -> 8
                                COLUMN_TYPE_DATA -> 8
                                COLUMN_TYPE_FLOAT -> 4
                                COLUMN_TYPE_4BYTE -> 4
                                COLUMN_TYPE_4BYTE2 -> 4
                                COLUMN_TYPE_2BYTE -> 2
                                COLUMN_TYPE_2BYTE2 -> 2
                                COLUMN_TYPE_1BYTE -> 1
                                COLUMN_TYPE_1BYTE2 -> 1
                                else -> {
                                    debug("formats.cpk.unknown_column_constant", columnType)
                                    0
                                }
                            }
                        }

                        val info = if (columnType and COLUMN_STORAGE_MASK == COLUMN_STORAGE_CONSTANT) {
                            val columnConstantOffset = flow.position()
                            flow.skip(columnWidth.toULong())
                            UtfColumnInfo(columnName, columnType, columnConstantOffset, rowPosition)
                        } else {
                            UtfColumnInfo(columnName, columnType, null, rowPosition)
                        }

                        rowPosition += columnWidth
                        info
                    }

                    return UtfTableInfo(tableName, tableSize, schemaOffset, rowsOffset, stringTable, stringTableOffset, dataOffset, columnCount, rowWidth, rowCount, schema, dataSource)
                }
            }
        }
    }

    fun stringFromTable(offset: Int) = stringTable.substring(offset.coerceIn(0 until stringTable.length)).substringBefore(NULL_TERMINATOR)

    operator fun get(name: String): UtfColumnInfo? = schema.firstOrNull { column -> column.name == name }

    @ExperimentalStdlibApi
    suspend fun SpiralContext.readRowData(rowIndex: Int, columnInfo: UtfColumnInfo): UtfRowData<*>? {
        try {
            return readRowDataUnsafe(rowIndex, columnInfo)
        } catch (iae: IllegalArgumentException) {
            debug("formats.utf_table.invalid_row", dataSource, rowIndex, columnInfo.name, iae)

            return null
        }
    }

    @ExperimentalStdlibApi
    suspend fun SpiralContext.readRowDataUnsafe(rowIndex: Int, columnInfo: UtfColumnInfo): UtfRowData<*> {
        val rowDataOffset: Int

        when (val storageClass = columnInfo.type and COLUMN_STORAGE_MASK) {
            COLUMN_STORAGE_PERROW -> {
                rowDataOffset = 8 + rowsOffset.toInt() + (rowIndex * rowWidth) + columnInfo.rowPosition
            }
            COLUMN_STORAGE_CONSTANT -> {
                rowDataOffset = columnInfo.constantOffset?.toInt()
                        ?: 8 + rowsOffset.toInt() + (rowIndex * rowWidth) + columnInfo.rowPosition
            }
            COLUMN_STORAGE_ZERO -> return when (val columnType = columnInfo.type and COLUMN_TYPE_MASK) {
                COLUMN_TYPE_STRING -> UtfRowData.TypeString(columnInfo.name, rowIndex, stringFromTable(0))
                COLUMN_TYPE_DATA -> UtfRowData.TypeData(columnInfo.name, rowIndex, byteArrayOf())
                COLUMN_TYPE_8BYTE -> UtfRowData.TypeLong(columnInfo.name, rowIndex, 0)
                COLUMN_TYPE_4BYTE2 -> UtfRowData.TypeInt2(columnInfo.name, rowIndex, 0)
                COLUMN_TYPE_4BYTE -> UtfRowData.TypeInt(columnInfo.name, rowIndex, 0)
                COLUMN_TYPE_2BYTE2 -> UtfRowData.TypeShort2(columnInfo.name, rowIndex, 0)
                COLUMN_TYPE_2BYTE -> UtfRowData.TypeShort(columnInfo.name, rowIndex, 0)
                COLUMN_TYPE_FLOAT -> UtfRowData.TypeFloat(columnInfo.name, rowIndex, 0f)
                COLUMN_TYPE_1BYTE2 -> UtfRowData.TypeByte2(columnInfo.name, rowIndex, 0)
                COLUMN_TYPE_1BYTE -> UtfRowData.TypeByte(columnInfo.name, rowIndex, 0)
                else -> throw IllegalArgumentException(localise("formats.cpk.unknown_column_constant", columnType))
            }
            else -> throw IllegalArgumentException(localise("formats.cpk.unknown_storage_class", storageClass))
        }

        val flow = requireNotNull(dataSource.openInputFlow())
        use(flow) {
            flow.skip(rowDataOffset.toULong())

            return when (val columnType = columnInfo.type and COLUMN_TYPE_MASK) {
                COLUMN_TYPE_STRING -> {
                    val readOffset = requireNotNull(flow.readInt32BE())
                    val offset = readOffset.toULong() + stringOffset + 8u
                    val string = requireNotNull(flow.fauxSeekFromStart(offset, dataSource, InputFlow::readNullTerminatedUTF8String))
                    require(string == stringFromTable(readOffset)) { "This one won't do; '$string' vs '${stringFromTable(readOffset)}'" }
                    UtfRowData.TypeString(columnInfo.name, rowIndex, string)
                }
                COLUMN_TYPE_DATA -> {
                    val location = requireNotNull(flow.readInt32BE()).toULong() + dataOffset + 8u
                    val size = requireNotNull(flow.readInt32BE())

                    val data = requireNotNull(flow.fauxSeekFromStart(location, dataSource) { dataFlow ->
                        val data = ByteArray(size)

                        require(dataFlow.read(data) == size)
                        data
                    })

                    UtfRowData.TypeData(columnInfo.name, rowIndex, data)
                }
                COLUMN_TYPE_8BYTE -> UtfRowData.TypeLong(columnInfo.name, rowIndex, requireNotNull(flow.readInt64BE()))
                COLUMN_TYPE_4BYTE2 -> UtfRowData.TypeInt2(columnInfo.name, rowIndex, requireNotNull(flow.readUInt32BE()).toLong())
                COLUMN_TYPE_4BYTE -> UtfRowData.TypeInt(columnInfo.name, rowIndex, requireNotNull(flow.readInt32BE()))
                COLUMN_TYPE_2BYTE2 -> UtfRowData.TypeShort2(columnInfo.name, rowIndex, requireNotNull(flow.readUInt16BE()))
                COLUMN_TYPE_2BYTE -> UtfRowData.TypeShort(columnInfo.name, rowIndex, requireNotNull(flow.readInt16BE()))
                COLUMN_TYPE_FLOAT -> UtfRowData.TypeFloat(columnInfo.name, rowIndex, requireNotNull(flow.readFloatBE()))
                COLUMN_TYPE_1BYTE2 -> UtfRowData.TypeByte2(columnInfo.name, rowIndex, requireNotNull(flow.read()))
                COLUMN_TYPE_1BYTE -> UtfRowData.TypeByte(columnInfo.name, rowIndex, requireNotNull(flow.read()))
                else -> throw IllegalArgumentException(localise("formats.cpk.unknown_column_constant", columnType))
            }
        }
    }
}

@ExperimentalUnsignedTypes
class UtfTableSchemaBuilder {
    var name: String = ""
    @ExperimentalUnsignedTypes
    var size: UInt = 1u
    var schemaOffset: UInt = 0u
    var rowsOffset: UInt = 0u
    var stringTable: String = ""
    var stringOffset: UInt = 0u
    var dataOffset: UInt = 0u
    var columnCount: Int = 0
    var rowWidth: Int = 0
    var rowCount: UInt = 0u
    var schema: Array<out UtfColumnSchema> = emptyArray()

    fun schema(vararg columns: UtfColumnSchema) {
        this.schema = columns
    }

    fun build(): UtfTableSchema = UtfTableSchema(name, size, schemaOffset, rowsOffset, stringTable, stringOffset, dataOffset, columnCount, rowWidth, rowCount, schema)
}

fun utfTableSchema(init: UtfTableSchemaBuilder.() -> Unit): UtfTableSchema {
    val builder = UtfTableSchemaBuilder()
    builder.init()
    return builder.build()
}

fun UtfTableInfo.getColumn(name: String): UtfColumnInfo = schema.first { utfColumnInfo -> utfColumnInfo.name == name }

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowData(context: SpiralContext, rowIndex: Int, columnInfo: UtfColumnInfo): UtfRowData<*>? = context.readRowData(rowIndex, columnInfo)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowDataUnsafe(context: SpiralContext, rowIndex: Int, columnInfo: UtfColumnInfo): UtfRowData<*> = context.readRowDataUnsafe(rowIndex, columnInfo)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowData(context: SpiralContext, rowIndex: Int, columnName: String): UtfRowData<*>? = get(columnName)?.let { columnInfo -> context.readRowData(rowIndex, columnInfo) }

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowDataUnsafe(context: SpiralContext, rowIndex: Int, columnName: String): UtfRowData<*> = context.readRowDataUnsafe(rowIndex, getColumn(columnName))