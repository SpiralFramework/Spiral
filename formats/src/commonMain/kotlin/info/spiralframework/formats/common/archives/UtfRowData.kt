package info.spiralframework.formats.common.archives

public sealed class UtfRowData<T>(public open val name: String, public open val index: Int, public open val data: T) {
    public data class TypeData(override val name: String, override val index: Int, override val data: ByteArray): UtfRowData<ByteArray>(name, index, data)
    public data class TypeString(override val name: String, override val index: Int, override val data: String): UtfRowData<String>(name, index, data)
    public data class TypeFloat(override val name: String, override val index: Int, override val data: Float): UtfRowData<Float>(name, index, data)
    public data class TypeLong(override val name: String, override val index: Int, override val data: Long): UtfRowData<Long>(name, index, data)
    public data class TypeInt2(override val name: String, override val index: Int, override val data: Long): UtfRowData<Long>(name, index, data)
    public data class TypeInt(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    public data class TypeShort2(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    public data class TypeShort(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    public data class TypeByte2(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    public data class TypeByte(override val name: String, override val index: Int, override val data: Int): UtfRowData<Int>(name, index, data)
    public data class Unknown(override val name: String, override val index: Int, override val data: Unit = Unit): UtfRowData<Unit>(name, index, data)
}