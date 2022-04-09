package info.spiralframework.formats.common.archives

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.flow.extensions.*
import dev.brella.kornea.io.common.flow.fauxSeekFromStart
import dev.brella.kornea.io.common.flow.offsetPosition
import info.spiralframework.base.common.NULL_TERMINATOR
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats

public open class UtfTableSchema(
    public open val name: String,
    public open val size: UInt,
    public open val schemaOffset: UInt,
    public open val rowsOffset: UInt,
    public open val stringTable: String,
    public open val stringOffset: UInt,
    public open val dataOffset: UInt,
    public open val columnCount: Int,
    public open val rowWidth: Int,
    public open val rowCount: UInt,
    public open val schema: Array<out UtfColumnSchema>
)

public data class UtfTableInfo(
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
) : UtfTableSchema(
    name,
    size,
    schemaOffset,
    rowsOffset,
    stringTable,
    stringOffset,
    dataOffset,
    columnCount,
    rowWidth,
    rowCount,
    schema
) {
    public companion object {
        public const val UTF_MAGIC_NUMBER_LE: Int = 0x46545540

        public const val INVALID_UTF_MAGIC_NUMBER: Int = 0x0000
        public const val UNKNOWN_COLUMN_CONSTANT: Int = 0x0001
        public const val UNKNOWN_STORAGE_CLASS: Int = 0x0002
        public const val NO_COLUMN_WITH_NAME: Int = 0x0010

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.utf_table.not_enough_data"
        public const val NO_COLUMN_WITH_NAME_KEY: String = "formats.utf_table.no_column_with_name"

        public const val COLUMN_STORAGE_MASK: Int = 0xF0
        public const val COLUMN_STORAGE_PERROW: Int = 0x50
        public const val COLUMN_STORAGE_CONSTANT: Int = 0x30
        public const val COLUMN_STORAGE_ZERO: Int = 0x10

        public const val COLUMN_TYPE_MASK: Int = 0x0F
        public const val COLUMN_TYPE_DATA: Int = 0x0B
        public const val COLUMN_TYPE_STRING: Int = 0x0A
        public const val COLUMN_TYPE_FLOAT: Int = 0x08
        public const val COLUMN_TYPE_8BYTE: Int = 0x06
        public const val COLUMN_TYPE_4BYTE2: Int = 0x05
        public const val COLUMN_TYPE_4BYTE: Int = 0x04
        public const val COLUMN_TYPE_2BYTE2: Int = 0x03
        public const val COLUMN_TYPE_2BYTE: Int = 0x02
        public const val COLUMN_TYPE_1BYTE2: Int = 0x01
        public const val COLUMN_TYPE_1BYTE: Int = 0x00

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<UtfTableInfo> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != UTF_MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_UTF_MAGIC_NUMBER,
                            localise(
                                "formats.utf_table.invalid_magic",
                                "0x${magic.toString(16)}",
                                "0x${UTF_MAGIC_NUMBER_LE.toString(16)}",
                                "0x${flow.offsetPosition().minus(4u).toString(16)}"
                            )
                        )
                    }

                    val tableSize = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val schemaOffset = 0x20u
                    val rowsOffset =
                        flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val stringTableOffset =
                        flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val dataOffset =
                        flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val tableNameOffset =
                        flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val columnCount =
                        flow.readInt16BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val rowWidth = flow.readInt16BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val rowCount = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val stringTable =
                        flow.fauxSeekFromStart((8u + stringTableOffset).toULong(), dataSource) { stringFlow ->
                            stringFlow.readString(
                                (dataOffset - stringTableOffset).toInt(),
                                encoding = TextCharsets.UTF_8
                            )
                        }.getOrBreak {
                            return@closeAfter it.cast()
                        } ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val tableName = stringTable.substring(tableNameOffset).substringBefore(NULL_TERMINATOR)

                    var rowPosition = 0
                    val schema = Array(columnCount) {
                        val columnType = flow.read() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        val columnName = stringTable
                            .substring(
                                flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(
                                    NOT_ENOUGH_DATA_KEY
                                )
                            )
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
                                else -> return@closeAfter KorneaResult.errorAsIllegalArgument(
                                    UNKNOWN_COLUMN_CONSTANT,
                                    localise("formats.utf_table.unknown_column_constant", columnType)
                                )
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

                    return@closeAfter KorneaResult.success(
                        UtfTableInfo(
                            tableName,
                            tableSize,
                            schemaOffset,
                            rowsOffset,
                            stringTable,
                            stringTableOffset,
                            dataOffset,
                            columnCount,
                            rowWidth,
                            rowCount,
                            schema,
                            dataSource
                        )
                    )
                }
            }
    }

    public fun stringFromTable(offset: Int): String =
        stringTable.substring(offset.coerceIn(stringTable.indices)).substringBefore(NULL_TERMINATOR)

    public operator fun get(name: String): UtfColumnInfo? = schema.firstOrNull { column -> column.name == name }

    public suspend fun SpiralContext.readRowData(
        rowIndex: Int,
        columnInfo: UtfColumnInfo
    ): KorneaResult<UtfRowData<*>> {
        val rowDataOffset: Int

        when (val storageClass = columnInfo.type and COLUMN_STORAGE_MASK) {
            COLUMN_STORAGE_PERROW -> {
                rowDataOffset = 8 + rowsOffset.toInt() + (rowIndex * rowWidth) + columnInfo.rowPosition
            }
            COLUMN_STORAGE_CONSTANT -> {
                rowDataOffset = columnInfo.constantOffset?.toInt()
                    ?: (8 + rowsOffset.toInt() + (rowIndex * rowWidth) + columnInfo.rowPosition)
            }
            COLUMN_STORAGE_ZERO -> return when (val columnType = columnInfo.type and COLUMN_TYPE_MASK) {
                COLUMN_TYPE_STRING -> KorneaResult.success(
                    UtfRowData.TypeString(
                        columnInfo.name,
                        rowIndex,
                        stringFromTable(0)
                    )
                )
                COLUMN_TYPE_DATA -> KorneaResult.success(UtfRowData.TypeData(columnInfo.name, rowIndex, byteArrayOf()))
                COLUMN_TYPE_8BYTE -> KorneaResult.success(UtfRowData.TypeLong(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_4BYTE2 -> KorneaResult.success(UtfRowData.TypeInt2(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_4BYTE -> KorneaResult.success(UtfRowData.TypeInt(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_2BYTE2 -> KorneaResult.success(UtfRowData.TypeShort2(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_2BYTE -> KorneaResult.success(UtfRowData.TypeShort(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_FLOAT -> KorneaResult.success(UtfRowData.TypeFloat(columnInfo.name, rowIndex, 0f))
                COLUMN_TYPE_1BYTE2 -> KorneaResult.success(UtfRowData.TypeByte2(columnInfo.name, rowIndex, 0))
                COLUMN_TYPE_1BYTE -> KorneaResult.success(UtfRowData.TypeByte(columnInfo.name, rowIndex, 0))
                else -> KorneaResult.errorAsIllegalArgument(
                    UNKNOWN_COLUMN_CONSTANT,
                    localise("formats.cpk.unknown_column_constant", columnType)
                )
            }
            else -> return KorneaResult.errorAsIllegalArgument(
                UNKNOWN_STORAGE_CLASS,
                localise("formats.cpk.unknown_storage_class", storageClass)
            )
        }

        val flow = dataSource.openInputFlow()
            .getOrBreak { return it.cast() }

        return closeAfter(flow) {
            flow.skip(rowDataOffset.toULong())

            when (val columnType = columnInfo.type and COLUMN_TYPE_MASK) {
                COLUMN_TYPE_STRING -> {
                    val readOffset = flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val offset = readOffset.toULong() + stringOffset + 8u
                    flow.fauxSeekFromStart(offset, dataSource) { flow -> flow.readNullTerminatedUTF8String() }
                        .flatMapOrSelf { string ->
                            if (string != stringFromTable(readOffset)) KorneaResult.errorAsIllegalArgument(
                                -1,
                                "Mismatching strings: [$string] vs [${stringFromTable(readOffset)}]"
                            )
                            else null
                        }.map { string -> UtfRowData.TypeString(columnInfo.name, rowIndex, string) }
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
                COLUMN_TYPE_8BYTE -> KorneaResult.success(
                    UtfRowData.TypeLong(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.readInt64BE())
                    )
                )
                COLUMN_TYPE_4BYTE2 -> KorneaResult.success(
                    UtfRowData.TypeInt2(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.readUInt32BE()).toLong()
                    )
                )
                COLUMN_TYPE_4BYTE -> KorneaResult.success(
                    UtfRowData.TypeInt(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.readInt32BE())
                    )
                )
                COLUMN_TYPE_2BYTE2 -> KorneaResult.success(
                    UtfRowData.TypeShort2(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.readUInt16BE())
                    )
                )
                COLUMN_TYPE_2BYTE -> KorneaResult.success(
                    UtfRowData.TypeShort(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.readInt16BE())
                    )
                )
                COLUMN_TYPE_FLOAT -> KorneaResult.success(
                    UtfRowData.TypeFloat(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.readFloatBE())
                    )
                )
                COLUMN_TYPE_1BYTE2 -> KorneaResult.success(
                    UtfRowData.TypeByte2(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.read())
                    )
                )
                COLUMN_TYPE_1BYTE -> KorneaResult.success(
                    UtfRowData.TypeByte(
                        columnInfo.name,
                        rowIndex,
                        requireNotNull(flow.read())
                    )
                )
                else -> throw IllegalArgumentException(localise("formats.cpk.unknown_column_constant", columnType))
            }
        }
    }
}

public class UtfTableSchemaBuilder {
    public var name: String = ""
    public var size: UInt = 1u
    public var schemaOffset: UInt = 0u
    public var rowsOffset: UInt = 0u
    public var stringTable: String = ""
    public var stringOffset: UInt = 0u
    public var dataOffset: UInt = 0u
    public var columnCount: Int = 0
    public var rowWidth: Int = 0
    public var rowCount: UInt = 0u
    public var schema: Array<out UtfColumnSchema> = emptyArray()

    public fun schema(vararg columns: UtfColumnSchema) {
        this.schema = columns
    }

    public fun build(): UtfTableSchema = UtfTableSchema(
        name,
        size,
        schemaOffset,
        rowsOffset,
        stringTable,
        stringOffset,
        dataOffset,
        columnCount,
        rowWidth,
        rowCount,
        schema
    )
}

public fun utfTableSchema(init: UtfTableSchemaBuilder.() -> Unit): UtfTableSchema {
    val builder = UtfTableSchemaBuilder()
    builder.init()
    return builder.build()
}

public fun UtfTableInfo.getColumnUnsafe(name: String): UtfColumnInfo =
    schema.first { utfColumnInfo -> utfColumnInfo.name == name }

public suspend fun UtfTableInfo.readRowData(
    context: SpiralContext,
    rowIndex: Int,
    columnInfo: UtfColumnInfo
): KorneaResult<UtfRowData<*>> = context.readRowData(rowIndex, columnInfo)

public suspend fun UtfTableInfo.readRowDataUnsafe(
    context: SpiralContext,
    rowIndex: Int,
    columnInfo: UtfColumnInfo
): UtfRowData<*> = context.readRowData(rowIndex, columnInfo).getOrThrow()

public suspend fun UtfTableInfo.readRowData(
    context: SpiralContext,
    rowIndex: Int,
    columnName: String
): KorneaResult<UtfRowData<*>> {
    val columnInfo = get(columnName)
        ?: return KorneaResult.errorAsIllegalArgument(
            UtfTableInfo.NO_COLUMN_WITH_NAME,
            UtfTableInfo.NO_COLUMN_WITH_NAME_KEY
        )
    return context.readRowData(rowIndex, columnInfo)
}

public suspend fun UtfTableInfo.readRowDataUnsafe(
    context: SpiralContext,
    rowIndex: Int,
    columnName: String
): UtfRowData<*> =
    context.readRowData(rowIndex, getColumnUnsafe(columnName)).getOrThrow()

public suspend fun UtfTableInfo.dumpTable(context: SpiralContext, indentLevel: Int = 0) {
    val indents = buildString { repeat(indentLevel) { append("\t") } }
    println("$indents==[${name}]==")
    schema.forEach { column ->
        val data = readRowDataUnsafe(context, 0, column)
        if (data is UtfRowData.TypeData) {
//                    println("Data: ${data.data.joinToString(" ") { it.toInt().and(0xFF).toString(16).padStart(2, '0').toUpperCase() }}")
            UtfTableInfo(context, BinaryDataSource(data.data))
                .getOrThrow()
                .dumpTable(context, indentLevel + 1)
        } else {
            println("$indents${column.name}: $data")
        }
    }
    println("$indents==[/${name}]==")
}