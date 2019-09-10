package info.spiralframework.formats.common.archives

sealed class UtfRowData<T>(open val name: String, open val index: Int, open val data: T) {
    data class TypeData(override val name: String, override val index: Int, override val data: ByteArray): UtfRowData<ByteArray>(name, index, data)
    data class TypeString(override val name: String, override val index: Int, override val data: String): UtfRowData<String>(name, index, data)
    data class TypeFloat(override val name: String, override val index: Int, override val data: Float): UtfRowData<Float>(name, index, data)
    data class TypeLong(override val name: String, override val index: Int, override val data: Long): UtfRowData<Long>(name, index, data)
    data class TypeInt2(override val name: String, override val index: Int, override val data: Long): UtfRowData<Long>(name, index, data)
    data class TypeInt(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    data class TypeShort2(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    data class TypeShort(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    data class TypeByte2(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    data class TypeByte(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    data class Unknown(override val name: String, override val index: Int, override val data: Unit = Unit): UtfRowData<Unit>(name, index, data)
}