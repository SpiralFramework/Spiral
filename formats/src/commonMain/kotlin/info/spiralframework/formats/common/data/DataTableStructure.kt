package info.spiralframework.formats.common.data

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrThrow
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.flow.extensions.*
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats

public class DataTableStructure(
    public val variableDetails: Array<DataVariableHeader>,
    public val entries: Array<Array<DataVariable>>,
    public val utf8Strings: Array<String>,
    public val utf16Strings: Array<String>
) {
    public data class DataVariableHeader(val variableName: String, val variableType: String)
    public sealed class DataVariable(variableName: String, data: Any) {
        public data class UnsignedByte(val variableName: String, val data: UByte) : DataVariable(variableName, data)
        public data class UnsignedShort(val variableName: String, val data: UShort) : DataVariable(variableName, data)
        public data class UnsignedInt(val variableName: String, val data: UInt) : DataVariable(variableName, data)
        public data class UnsignedLong(val variableName: String, val data: ULong) : DataVariable(variableName, data)

        public data class SignedByte(val variableName: String, val data: Byte) : DataVariable(variableName, data)
        public data class SignedShort(val variableName: String, val data: Short) : DataVariable(variableName, data)
        public data class SignedInt(val variableName: String, val data: Int) : DataVariable(variableName, data)
        public data class SignedLong(val variableName: String, val data: Long) : DataVariable(variableName, data)

        public data class Float32(val variableName: String, val data: Float) : DataVariable(variableName, data)
        public data class Float64(val variableName: String, val data: Double) : DataVariable(variableName, data)

        public abstract class UTF8(variableName: String, public open val data: Int) : DataVariable(variableName, data)
        public data class Label(val variableName: String, override val data: Int) : UTF8(variableName, data)
        public data class Refer(val variableName: String, override val data: Int) : UTF8(variableName, data)
        public data class Ascii(val variableName: String, override val data: Int) : UTF8(variableName, data)
        public data class UTF16(val variableName: String, val data: Int) : DataVariable(variableName, data)

        public val dataVariableName: String = variableName
        public val genericData: Any = data
    }

    public companion object {
        public const val INVALID_STRUCTURE_COUNT: Int = 0x0000
        public const val INVALID_STRUCTURE_SIZE: Int = 0x0001
        public const val INVALID_VARIABLE_COUNT: Int = 0x0002
        public const val UNKNOWN_VARIABLE_TYPE: Int = 0x0003

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.data_table.not_enough_data"
        public const val INVALID_STRUCTURE_COUNT_KEY: String = "formats.data_table.invalid_structure_count"
        public const val INVALID_STRUCTURE_SIZE_KEY: String = "formats.data_table.invalid_structure_size"
        public const val INVALID_VARIABLE_COUNT_KEY: String = "formats.data_table.invalid_variable_count"
        public const val UNKNOWN_VARIABLE_TYPE_KEY: String = "formats.data_table.unknown_variable_type"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<DataTableStructure> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val structureCount =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (structureCount !in 1..1023) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_STRUCTURE_COUNT,
                            localise(INVALID_STRUCTURE_COUNT_KEY, structureCount, 1, 1023)
                        )
                    }

                    val structureSize =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (structureSize !in 1..1023) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_STRUCTURE_SIZE,
                            localise(INVALID_STRUCTURE_SIZE_KEY, structureSize, 1, 1023)
                        )
                    }

                    val variableCount =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (variableCount !in 1..1023) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            INVALID_VARIABLE_COUNT,
                            localise(INVALID_VARIABLE_COUNT_KEY, variableCount, 1, 1023)
                        )
                    }

                    val variableDetails = Array(variableCount) {
                        val variableName = flow.readNullTerminatedUTF8String()
                        val variableType = flow.readNullTerminatedUTF8String()
                        flow.skip(2u)

                        DataVariableHeader(variableName, variableType)
                    }

                    flow.skip(flow.position().alignmentNeededFor(0x10).toULong())

                    val entries = Array(structureCount) {
                        variableDetails.map { (variableName, variableType) ->
                            when (variableType.lowercase()) {
                                //NOTE: These variables cannot be inlined without crashing the Kotlin compiler.

                                "u8" -> {
                                    val data = flow.read()?.toUByte()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedByte(variableName, data)
                                }
                                "u16" -> {
                                    val data = flow.readInt16LE()?.toUShort()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedShort(variableName, data)
                                }
                                "u32" -> {
                                    val data = flow.readUInt32LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedInt(variableName, data)
                                }
                                "u64" -> {
                                    val data = flow.readUInt64LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedLong(variableName, data)
                                }

                                "s8" -> {
                                    val data = flow.read()?.toByte()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.SignedByte(variableName, data)
                                }
                                "s16" -> {
                                    val data = flow.readInt16LE()?.toShort()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.SignedShort(variableName, data)
                                }
                                "s32" -> {
                                    val data = flow.readInt32LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.SignedInt(variableName, data)
                                }
                                "s64" -> {
                                    val data = flow.readInt64LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                    DataVariable.SignedLong(variableName, data)
                                }

                                "f32" -> {
                                    val data = flow.readFloat32LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Float32(variableName, data)
                                }
                                "f64" -> {
                                    val data = flow.readFloat64LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Float64(variableName, data)
                                }

                                "label" -> {
                                    val data = flow.readInt16LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Label(variableName, data)
                                }
                                "refer" -> {
                                    val data = flow.readInt16LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Refer(variableName, data)
                                }
                                "ascii" -> {
                                    val data = flow.readInt16LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                    DataVariable.Ascii(variableName, data)
                                }
                                "utf16" -> {
                                    val data = flow.readInt16LE()
                                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                    DataVariable.UTF16(variableName, data)
                                }

                                else -> return@closeAfter KorneaResult.errorAsIllegalArgument(
                                    UNKNOWN_VARIABLE_TYPE,
                                    localise(UNKNOWN_VARIABLE_TYPE_KEY, variableType)
                                )
                            }
                        }.toTypedArray()
                    }

                    val utf8Count = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val utf16Count = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val utf8Strings =
                        Array(utf8Count) { flow.readSingleByteNullTerminatedString(encoding = TextCharsets.UTF_8) }
                    val utf16Strings =
                        Array(utf16Count) { flow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16) }

                    return@closeAfter KorneaResult.success(
                        DataTableStructure(
                            variableDetails,
                            entries,
                            utf8Strings,
                            utf16Strings
                        )
                    )
                }
            }
    }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.DataTableStructure(dataSource: DataSource<*>): KorneaResult<DataTableStructure> = DataTableStructure(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeDataTableStructure(dataSource: DataSource<*>): DataTableStructure =
    DataTableStructure(this, dataSource).getOrThrow()