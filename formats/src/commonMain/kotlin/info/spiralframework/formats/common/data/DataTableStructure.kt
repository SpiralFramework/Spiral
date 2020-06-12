package info.spiralframework.formats.common.data

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.erorrs.common.cast
import org.abimon.kornea.erorrs.common.doOnFailure
import org.abimon.kornea.io.common.*

@ExperimentalUnsignedTypes
class DataTableStructure(val variableDetails: Array<DataVariableHeader>, val entries: Array<Array<DataVariable>>, val utf8Strings: Array<String>, val utf16Strings: Array<String>) {
    data class DataVariableHeader(val variableName: String, val variableType: String)
    sealed class DataVariable(variableName: String, data: Any) {
        data class UnsignedByte(val variableName: String, val data: UByte) : DataVariable(variableName, data)
        data class UnsignedShort(val variableName: String, val data: UShort) : DataVariable(variableName, data)
        data class UnsignedInt(val variableName: String, val data: UInt) : DataVariable(variableName, data)
        data class UnsignedLong(val variableName: String, val data: ULong) : DataVariable(variableName, data)

        data class SignedByte(val variableName: String, val data: Byte) : DataVariable(variableName, data)
        data class SignedShort(val variableName: String, val data: Short) : DataVariable(variableName, data)
        data class SignedInt(val variableName: String, val data: Int) : DataVariable(variableName, data)
        data class SignedLong(val variableName: String, val data: Long) : DataVariable(variableName, data)

        data class Float32(val variableName: String, val data: Float) : DataVariable(variableName, data)
        data class Float64(val variableName: String, val data: Double) : DataVariable(variableName, data)

        abstract class UTF8(variableName: String, open val data: Int) : DataVariable(variableName, data)
        data class Label(val variableName: String, override val data: Int) : UTF8(variableName, data)
        data class Refer(val variableName: String, override val data: Int) : UTF8(variableName, data)
        data class Ascii(val variableName: String, override val data: Int) : UTF8(variableName, data)
        data class UTF16(val variableName: String, val data: Int) : DataVariable(variableName, data)

        val dataVariableName: String = variableName
        val genericData: Any = data
    }

    companion object {
        const val INVALID_STRUCTURE_COUNT = 0x0000
        const val INVALID_STRUCTURE_SIZE = 0x0001
        const val INVALID_VARIABLE_COUNT = 0x0002
        const val UNKNOWN_VARIABLE_TYPE = 0x0003

        const val NOT_ENOUGH_DATA_KEY = "formats.data_table.not_enough_data"
        const val INVALID_STRUCTURE_COUNT_KEY = "formats.data_table.invalid_structure_count"
        const val INVALID_STRUCTURE_SIZE_KEY = "formats.data_table.invalid_structure_size"
        const val INVALID_VARIABLE_COUNT_KEY = "formats.data_table.invalid_variable_count"
        const val UNKNOWN_VARIABLE_TYPE_KEY = "formats.data_table.unknown_variable_type"

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<DataTableStructure> {
            withFormats(context) {
                val flow = dataSource.openInputFlow().doOnFailure { return it.cast() }

                use(flow) {
                    val structureCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (structureCount !in 1 .. 1023) {
                        return KorneaResult.Error(INVALID_STRUCTURE_COUNT, localise(INVALID_STRUCTURE_COUNT_KEY, structureCount, 1, 1023))
                    }

                    val structureSize = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (structureSize !in 1 .. 1023) {
                        return KorneaResult.Error(INVALID_STRUCTURE_SIZE, localise(INVALID_STRUCTURE_SIZE_KEY, structureSize, 1, 1023))
                    }

                    val variableCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (variableCount !in 1 .. 1023) {
                        return KorneaResult.Error(INVALID_VARIABLE_COUNT, localise(INVALID_VARIABLE_COUNT_KEY, variableCount, 1, 1023))
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
                            when (variableType.toLowerCase()) {
                                //NOTE: These variables cannot be inlined without crashing the Kotlin compiler.

                                "u8" -> {
                                    val data = flow.read()?.toUByte()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedByte(variableName, data)
                                }
                                "u16" -> {
                                    val data = flow.readInt16LE()?.toUShort()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedShort(variableName, data)
                                }
                                "u32" -> {
                                    val data = flow.readUInt32LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedInt(variableName, data)
                                }
                                "u64" -> {
                                    val data = flow.readUInt64LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.UnsignedLong(variableName, data)
                                }

                                "s8" -> {
                                    val data = flow.read()?.toByte()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.SignedByte(variableName, data)
                                }
                                "s16" -> {
                                    val data = flow.readInt16LE()?.toShort()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.SignedShort(variableName, data)
                                }
                                "s32" -> {
                                    val data = flow.readInt32LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.SignedInt(variableName, data)
                                }
                                "s64" -> {
                                    val data = flow.readInt64LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                    DataVariable.SignedLong(variableName, data)
                                }

                                "f32" -> {
                                    val data = flow.readFloat32LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Float32(variableName, data)
                                }
                                "f64" -> {
                                    val data = flow.readFloat64LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Float64(variableName, data)
                                }

                                "label" -> {
                                    val data = flow.readInt16LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Label(variableName, data)
                                }
                                "refer" -> {
                                    val data = flow.readInt16LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                                    DataVariable.Refer(variableName, data)
                                }
                                "ascii" -> {
                                    val data = flow.readInt16LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                    DataVariable.Ascii(variableName, data)
                                }
                                "utf16" -> {
                                    val data = flow.readInt16LE()
                                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                                    DataVariable.UTF16(variableName, data)
                                }

                                else -> return KorneaResult.Error(UNKNOWN_VARIABLE_TYPE, localise(UNKNOWN_VARIABLE_TYPE_KEY, variableType))
                            }
                        }.toTypedArray()
                    }

                    val utf8Count = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val utf16Count = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val utf8Strings = Array(utf8Count) { flow.readSingleByteNullTerminatedString(encoding = TextCharsets.UTF_8) }
                    val utf16Strings = Array(utf16Count) { flow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16) }

                    return KorneaResult.Success(DataTableStructure(variableDetails, entries, utf8Strings, utf16Strings))
                }
            }
        }
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.DataTableStructure(dataSource: DataSource<*>) = DataTableStructure(this, dataSource)

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDataTableStructure(dataSource: DataSource<*>) = DataTableStructure(this, dataSource).get()