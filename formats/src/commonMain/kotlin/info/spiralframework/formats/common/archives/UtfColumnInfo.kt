package info.spiralframework.formats.common.archives

@ExperimentalUnsignedTypes
data class UtfColumnInfo(val name: String, val type: Int, val constantOffset: ULong?, val rowPosition: Int) {
}