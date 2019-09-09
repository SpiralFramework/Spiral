package info.spiralframework.formats.data

import info.spiralframework.base.jvm.io.CountingInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.*
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.utils.align
import java.io.InputStream

class DataTableV3 private constructor(context: SpiralContext, val dataSource: DataSource) {
    companion object {
        operator fun invoke(context: SpiralContext, dataSource: () -> InputStream): DataTableV3? {
            withFormats(context) {
                try {
                    return DataTableV3(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.data_table_v3.invalid", dataSource, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, dataSource: () -> InputStream): DataTableV3 = withFormats(context) { DataTableV3(this, dataSource) }
    }

    data class DataVariableHeader(val variableName: String, val variableType: String)
    sealed class DataVariable<T>(variableName: String, data: T) {
        data class UnsignedByte(val variableName: String, val data: UByte) : DataVariable<UByte>(variableName, data)
        data class UnsignedShort(val variableName: String, val data: UShort) : DataVariable<UShort>(variableName, data)
        data class UnsignedInt(val variableName: String, val data: UInt) : DataVariable<UInt>(variableName, data)
        data class UnsignedLong(val variableName: String, val data: ULong) : DataVariable<ULong>(variableName, data)

        data class SignedByte(val variableName: String, val data: Byte) : DataVariable<Byte>(variableName, data)
        data class SignedShort(val variableName: String, val data: Short) : DataVariable<Short>(variableName, data)
        data class SignedInt(val variableName: String, val data: Int) : DataVariable<Int>(variableName, data)
        data class SignedLong(val variableName: String, val data: Long) : DataVariable<Long>(variableName, data)

        data class Float32(val variableName: String, val data: Float) : DataVariable<Float>(variableName, data)
        data class Float64(val variableName: String, val data: Double) : DataVariable<Double>(variableName, data)

        abstract class UTF8(variableName: String, open val data: Int) : DataVariable<Int>(variableName, data)
        data class Label(val variableName: String, override val data: Int) : UTF8(variableName, data)
        data class Refer(val variableName: String, override val data: Int) : UTF8(variableName, data)
        data class Ascii(val variableName: String, override val data: Int) : UTF8(variableName, data)
        data class UTF16(val variableName: String, val data: Int) : DataVariable<Int>(variableName, data)

        val dataVariableName: String = variableName
        val dataAsT = data
    }

    val structureCount: Int
    val structureSize: Int
    val variableCount: Int

    val variableDetails: Array<DataVariableHeader>
    val entries: Array<Array<DataVariable<*>>>

    val utf8Strings: Array<String>
    val utf16Strings: Array<String>

    init {
        with(context) {
            val stream = CountingInputStream(dataSource())

            try {
                structureCount = stream.readInt32LE()
                require(structureCount in 1..1023) { localise("formats.data_table_v3.negative_structure_count") }

                structureSize = stream.readInt32LE()
                require(structureSize in 1..1023) { localise("formats.data_table_v3.negative_structure_size") }

                variableCount = stream.readInt32LE()
                require(variableCount in 1..1023) { localise("formats.data_table_v3.negative_variable_count") }

                variableDetails = Array(variableCount) {
                    val variableName = stream.readNullTerminatedUTF8String()
                    val variableType = stream.readNullTerminatedUTF8String()
                    stream.skip(2)

                    return@Array DataVariableHeader(variableName, variableType)
                }

                stream.skip(stream.count.align(0x10).toLong())

                entries = Array(structureCount) {
                    variableDetails.map { (variableName, variableType) ->
                        return@map when (variableType.toLowerCase()) {
                            "u8" -> DataVariable.UnsignedByte(variableName, stream.read().toUByte())
                            "u16" -> DataVariable.UnsignedShort(variableName, stream.readInt16LE().toUShort())
                            "u32" -> DataVariable.UnsignedInt(variableName, stream.readUInt32LE())
                            "u64" -> DataVariable.UnsignedLong(variableName, stream.readUInt64LE())

                            "s8" -> DataVariable.SignedByte(variableName, stream.read().toByte())
                            "s16" -> DataVariable.SignedShort(variableName, stream.readInt16LE().toShort())
                            "s32" -> DataVariable.SignedInt(variableName, stream.readInt32LE())
                            "s64" -> DataVariable.SignedLong(variableName, stream.readInt64LE())

                            "f32" -> DataVariable.Float32(variableName, stream.readFloat32LE())
                            "f64" -> DataVariable.Float64(variableName, stream.readFloat64LE())

                            "label" -> DataVariable.Label(variableName, stream.readInt16LE())
                            "refer" -> DataVariable.Refer(variableName, stream.readInt16LE())
                            "ascii" -> DataVariable.Ascii(variableName, stream.readInt16LE())
                            "utf16" -> DataVariable.UTF16(variableName, stream.readInt16LE())

                            else -> throw IllegalArgumentException("")
                        } as DataVariable<*>
                    }.toTypedArray()
                }

                val utf8Count = stream.readInt16LE()
                val utf16Count = stream.readInt16LE()

                utf8Strings = Array(utf8Count) { stream.readNullTerminatedString(encoding = Charsets.UTF_8, bytesPer = 1) }
                stream.skip(stream.count.align(2).toLong())
                utf16Strings = Array(utf16Count) { stream.readNullTerminatedString(encoding = Charsets.UTF_16, bytesPer = 2) }
            } finally {
                stream.close()
            }
        }
    }
}