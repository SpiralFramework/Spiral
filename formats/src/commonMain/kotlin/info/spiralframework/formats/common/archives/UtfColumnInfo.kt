package info.spiralframework.formats.common.archives

@ExperimentalUnsignedTypes
open class UtfColumnSchema(open val name: String, open val type: Int)

@ExperimentalUnsignedTypes
data class UtfColumnInfo(override val name: String, override val type: Int, val constantOffset: ULong?, val rowPosition: Int): UtfColumnSchema(name, type)