package info.spiralframework.formats.common.data

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.io.*
import info.spiralframework.formats.common.withFormats
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
        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): DataTableStructure? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.data_table.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): DataTableStructure {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.data_table.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val structureCount = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(structureCount in 1..1023) { localise("formats.data_table.invalid_structure_count", structureCount, 1, 1023) }

                    val structureSize = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(structureSize in 1..1023) { localise("formats.data_table.invalid_structure_size", structureSize, 1, 1023) }

                    val variableCount = requireNotNull(flow.readInt32LE(), notEnoughData)
                    require(variableCount in 1..1023) { localise("formats.data_table.invalid_variable_count", variableCount, 1, 1023) }

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
                                "u8" -> DataVariable.UnsignedByte(variableName, requireNotNull(flow.read(), notEnoughData).toUByte())
                                "u16" -> DataVariable.UnsignedShort(variableName, requireNotNull(flow.readInt16LE(), notEnoughData).toUShort())
                                "u32" -> DataVariable.UnsignedInt(variableName, requireNotNull(flow.readUInt32LE(), notEnoughData))
                                "u64" -> DataVariable.UnsignedLong(variableName, requireNotNull(flow.readUInt64LE(), notEnoughData))

                                "s8" -> DataVariable.SignedByte(variableName, requireNotNull(flow.read(), notEnoughData).toByte())
                                "s16" -> DataVariable.SignedShort(variableName, requireNotNull(flow.readInt16LE(), notEnoughData).toShort())
                                "s32" -> DataVariable.SignedInt(variableName, requireNotNull(flow.readInt32LE(), notEnoughData))
                                "s64" -> DataVariable.SignedLong(variableName, requireNotNull(flow.readInt64LE(), notEnoughData))

                                "f32" -> DataVariable.Float32(variableName, requireNotNull(flow.readFloat32LE(), notEnoughData))
                                "f64" -> DataVariable.Float64(variableName, requireNotNull(flow.readFloat64LE(), notEnoughData))

                                "label" -> DataVariable.Label(variableName, requireNotNull(flow.readInt16LE(), notEnoughData))
                                "refer" -> DataVariable.Refer(variableName, requireNotNull(flow.readInt16LE(), notEnoughData))
                                "ascii" -> DataVariable.Ascii(variableName, requireNotNull(flow.readInt16LE(), notEnoughData))
                                "utf16" -> DataVariable.UTF16(variableName, requireNotNull(flow.readInt16LE(), notEnoughData))

                                else -> throw IllegalArgumentException("")
                            }
                        }.toTypedArray()
                    }

                    val utf8Count = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val utf16Count = requireNotNull(flow.readInt16LE(), notEnoughData)

                    val utf8Strings = Array(utf8Count) { flow.readSingleByteNullTerminatedString(encoding = TextCharsets.UTF_8) }
                    val utf16Strings = Array(utf16Count) { flow.readSingleByteNullTerminatedString(encoding = TextCharsets.UTF_16) }

                    return DataTableStructure(variableDetails, entries, utf8Strings, utf16Strings)
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
suspend fun SpiralContext.UnsafeDataTableStructure(dataSource: DataSource<*>) = DataTableStructure.unsafe(this, dataSource)