package info.spiralframework.formats.common.archives

@ExperimentalUnsignedTypes
public open class UtfColumnSchema(public open val name: String, public open val type: Int)

@ExperimentalUnsignedTypes
public data class UtfColumnInfo(override val name: String, override val type: Int, val constantOffset: ULong?, val rowPosition: Int): UtfColumnSchema(name, type)