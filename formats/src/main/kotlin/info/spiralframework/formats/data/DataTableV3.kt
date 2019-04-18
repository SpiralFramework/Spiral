package info.spiralframework.formats.data

import info.spiralframework.base.CountingInputStream
import info.spiralframework.base.util.assertAsLocaleArgument
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readNullTerminatedUTF8String
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.utils.align
import java.io.InputStream

class DataTableV3(val dataSource: DataSource) {
    companion object {
        operator fun invoke(dataSource: () -> InputStream): DataTableV3? {
            try {
                return DataTableV3(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.data_table_v3.invalid", dataSource, iae)

                return null
            }
        }

        fun unsafe(dataSource: () -> InputStream): DataTableV3 = DataTableV3(dataSource)
    }

    data class DataVariableHeader(val variableName: String, val variableType: String)
    sealed class DataVariable<T>(val variableName: String, val data: T) {
        class UnsignedByte(variableName: String, data: UByte): DataVariable<UByte>(variableName, data)
        class UnsignedShort(variableName: String, data: UShort): DataVariable<UShort>(variableName, data)
        class UnsignedInt(variableName: String, data: UInt): DataVariable<UInt>(variableName, data)
        class UnsignedLong(variableName: String, data: ULong): DataVariable<ULong>(variableName, data)

        class SignedByte(variableName: String, data: Byte): DataVariable<Byte>(variableName, data)
        class SignedShort(variableName: String, data: Short): DataVariable<Short>(variableName, data)
        class SignedInt(variableName: String, data: Int): DataVariable<Int>(variableName, data)
        class SignedLong(variableName: String, data: Long): DataVariable<Long>(variableName, data)

        class Float(variableName: String, data: Float): DataVariable<Float>(variableName, data)
        class Double(variableName: String, data: Double): DataVariable<Double>(variableName, data)

        class Label(variableName: String, data: Int): DataVariable<Int>(variableName, data)
        class Refer(variableName: String, data: Int): DataVariable<Int>(variableName, data)
        class Ascii(variableName: String, data: Int): DataVariable<Int>(variableName, data)
        class UTF16(variableName: String, data: Int): DataVariable<Int>(variableName, data)
    }

    val structureCount: Int
    val structureSize: Int
    val variableCount: Int

    val variableDetails: Array<DataVariableHeader>
//    val entries: Array<Array<DataVariable<*>>>

    init {
        val stream = CountingInputStream(dataSource())

        try {
            structureCount = stream.readInt32LE()
            assertAsLocaleArgument(structureCount > 0, "formats.data_table_v3.negative_structure_count")

            structureSize = stream.readInt32LE()
            assertAsLocaleArgument(structureSize > 0, "formats.data_table_v3.negative_structure_size")

            variableCount = stream.readInt32LE()
            assertAsLocaleArgument(variableCount > 0, "formats.data_table_v3.negative_variable_count")

            variableDetails = Array(variableCount) {
                val variableName = stream.readNullTerminatedUTF8String()
                val variableType = stream.readNullTerminatedUTF8String()
                stream.skip(2)

                return@Array DataVariableHeader(variableName, variableType)
            }

            stream.skip(stream.count.align(0x10).toLong())

//            entries = Array(structureCount) {
//                variableDetails.map { (variableName, variableType) ->
//                    return when (variableType.toLowerCase()) {
//                        "u8" -> DataVariable.UnsignedByte(variableName, stream.read().toUByte())
//
//                    }
//                }
//            }
        } finally {
            stream.close()
        }
    }
}