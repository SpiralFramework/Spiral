package info.spiralframework.formats.common.archives

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.NULL_TERMINATOR
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.errors.common.*
import org.abimon.kornea.errors.common.KorneaResult.Success
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

        const val INVALID_UTF_MAGIC_NUMBER = 0x0000
        const val UNKNOWN_COLUMN_CONSTANT = 0x0001
        const val UNKNOWN_STORAGE_CLASS = 0x0002
        const val NO_COLUMN_WITH_NAME = 0x0010

        const val NOT_ENOUGH_DATA_KEY = "formats.utf_table.not_enough_data"
        const val NO_COLUMN_WITH_NAME_KEY = "formats.utf_table.no_column_with_name"

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
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<UtfTableInfo> =
            withFormats(context) {
                val flow = dataSource.openInputFlow().getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != UTF_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                                INVALID_UTF_MAGIC_NUMBER,
                                localise("formats.utf_table.invalid_magic", "0x${magic.toString(16)}", "0x${UTF_MAGIC_NUMBER_LE.toString(16)}", "0x${flow.offsetPosition().minus(4u).toString(16)}")
                        )
                    }

                    val tableSize = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val schemaOffset = 0x20u
                    val rowsOffset = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val stringTableOffset = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val dataOffset = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val tableNameOffset = flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val columnCount = flow.readInt16BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val rowWidth = flow.readInt16BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val rowCount = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val stringTable = flow.fauxSeekFromStart((8u + stringTableOffset).toULong(), dataSource) { stringFlow ->
                        stringFlow.readString((dataOffset - stringTableOffset).toInt(), encoding = TextCharsets.UTF_8)
                    }.getOrBreak {
                        return@closeAfter it.cast()
                    } ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val tableName = stringTable.substring(tableNameOffset).substringBefore(NULL_TERMINATOR)

                    var rowPosition = 0
                    val schema = Array(columnCount) {
                        val columnType = flow.read() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val columnName = stringTable
                                .substring(flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
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
                                else -> return@closeAfter KorneaResult.errorAsIllegalArgument(UNKNOWN_COLUMN_CONSTANT, localise("formats.utf_table.unknown_column_constant", columnType))
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

                    return@closeAfter KorneaResult.success(UtfTableInfo(tableName, tableSize, schemaOffset, rowsOffset, stringTable, stringTableOffset, dataOffset, columnCount, rowWidth, rowCount, schema, dataSource))
                }
            }
    }

    fun stringFromTable(offset: Int) = stringTable.substring(offset.coerceIn(0 until stringTable.length)).substringBefore(NULL_TERMINATOR)

    operator fun get(name: String): UtfColumnInfo? = schema.firstOrNull { column -> column.name == name }

    @ExperimentalStdlibApi
    suspend fun SpiralContext.readRowData(rowIndex: Int, columnInfo: UtfColumnInfo): KorneaResult<UtfRowData<*>> {
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
                COLUMN_TYPE_STRING -> KorneaResult.success(UtfRowData.TypeString(columnInfo.name, rowIndex, stringFromTable(0)))
                COLUMN_TYPE_DATA -> KorneaResult.success(UtfRowData.TypeData(columnInfo.name, rowIndex, byteArrayOf()))
                COLUMN_TYPE_8BYTE -> KorneaResult.success(UtfRowData.TypeLong(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_4BYTE2 -> KorneaResult.success(UtfRowData.TypeInt2(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_4BYTE -> KorneaResult.success(UtfRowData.TypeInt(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_2BYTE2 -> KorneaResult.success(UtfRowData.TypeShort2(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_2BYTE -> KorneaResult.success(UtfRowData.TypeShort(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_FLOAT -> KorneaResult.success(UtfRowData.TypeFloat(columnInfo.name, rowIndex, 0f))
                COLUMN_TYPE_1BYTE2 -> KorneaResult.success(UtfRowData.TypeByte2(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_1BYTE -> KorneaResult.success(UtfRowData.TypeByte(columnInfo.name, rowIndex, 0))
                else -> KorneaResult.errorAsIllegalArgument(UNKNOWN_COLUMN_CONSTANT, localise("formats.cpk.unknown_column_constant", columnType))
            }
            else -> return KorneaResult.errorAsIllegalArgument(UNKNOWN_STORAGE_CLASS, localise("formats.cpk.unknown_storage_class", storageClass))
        }

        val flow = dataSource.openInputFlow().getOrBreak { return it.cast() }
        return closeAfter(flow) {
            flow.skip(rowDataOffset.toULong())

            when (val columnType = columnInfo.type and COLUMN_TYPE_MASK) {
                COLUMN_TYPE_STRING -> {
                    val readOffset = flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val offset = readOffset.toULong() + stringOffset + 8u
                    flow.fauxSeekFromStart(offset, dataSource) { flow -> flow.readNullTerminatedUTF8String() }
                            .filterTo { string -> if (string != stringFromTable(readOffset)) KorneaResult.errorAsIllegalArgument(-1, "Mismatching strings: [$string] vs [${stringFromTable(readOffset)}]") else null }
                            .map { string -> UtfRowData.TypeString(columnInfo.name, rowIndex, string) }
                }
                COLUMN_TYPE_DATA -> {
                    val location = requireNotNull(flow.readInt32BE()).toULong() + dataOffset + 8u
                    val size = requireNotNull(flow.readInt32BE())

                    flow.fauxSeekFromStart(location, dataSource) { dataFlow ->
                        val data = ByteArray(size)

                        require(dataFlow.read(data) == size)
                        data
                    }.map { data ->
                        UtfRowData.TypeData(columnInfo.name, rowIndex, data)
                    }
                }
                COLUMN_TYPE_8BYTE -> KorneaResult.success(UtfRowData.TypeLong(columnInfo.name, rowIndex, requireNotNull(flow.readInt64BE())))
                COLUMN_TYPE_4BYTE2 -> KorneaResult.success(UtfRowData.TypeInt2(columnInfo.name, rowIndex, requireNotNull(flow.readUInt32BE()).toLong()))
                COLUMN_TYPE_4BYTE -> KorneaResult.success(UtfRowData.TypeInt(columnInfo.name, rowIndex, requireNotNull(flow.readInt32BE())))
                COLUMN_TYPE_2BYTE2 -> KorneaResult.success(UtfRowData.TypeShort2(columnInfo.name, rowIndex, requireNotNull(flow.readUInt16BE())))
                COLUMN_TYPE_2BYTE -> KorneaResult.success(UtfRowData.TypeShort(columnInfo.name, rowIndex, requireNotNull(flow.readInt16BE())))
                COLUMN_TYPE_FLOAT -> KorneaResult.success(UtfRowData.TypeFloat(columnInfo.name, rowIndex, requireNotNull(flow.readFloatBE())))
                COLUMN_TYPE_1BYTE2 -> KorneaResult.success(UtfRowData.TypeByte2(columnInfo.name, rowIndex, requireNotNull(flow.read())))
                COLUMN_TYPE_1BYTE -> KorneaResult.success(UtfRowData.TypeByte(columnInfo.name, rowIndex, requireNotNull(flow.read())))
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

fun UtfTableInfo.getColumnUnsafe(name: String): UtfColumnInfo = schema.first { utfColumnInfo -> utfColumnInfo.name == name }

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowData(context: SpiralContext, rowIndex: Int, columnInfo: UtfColumnInfo): KorneaResult<UtfRowData<*>> = context.readRowData(rowIndex, columnInfo)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowDataUnsafe(context: SpiralContext, rowIndex: Int, columnInfo: UtfColumnInfo): UtfRowData<*> = context.readRowData(rowIndex, columnInfo).get()

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowData(context: SpiralContext, rowIndex: Int, columnName: String): KorneaResult<UtfRowData<*>> {
    val columnInfo = get(columnName)
            ?: return KorneaResult.errorAsIllegalArgument(UtfTableInfo.NO_COLUMN_WITH_NAME, UtfTableInfo.NO_COLUMN_WITH_NAME_KEY)
    return context.readRowData(rowIndex, columnInfo)
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun UtfTableInfo.readRowDataUnsafe(context: SpiralContext, rowIndex: Int, columnName: String): UtfRowData<*> = context.readRowData(rowIndex, getColumnUnsafe(columnName)).get()

@ExperimentalStdlibApi
suspend fun UtfTableInfo.dumpTable(context: SpiralContext, indentLevel: Int = 0) {
    val indents = buildString { repeat(indentLevel) { append("\t") } }
    println("$indents==[${name}]==")
    schema.forEach { column ->
        val data = readRowDataUnsafe(context, 0, column)
        if (data is UtfRowData.TypeData) {
//                    println("Data: ${data.data.joinToString(" ") { it.toInt().and(0xFF).toString(16).padStart(2, '0').toUpperCase() }}")
            UtfTableInfo(context, BinaryDataSource(data.data)).get().dumpTable(context, indentLevel + 1)
        } else {
            println("$indents${column.name}: $data")
        }
    }
    println("$indents==[/${name}]==")
}