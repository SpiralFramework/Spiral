package info.spiralframework.formats.common.archives

@ExperimentalUnsignedTypes
data class SpcFileEntry(val name: String, val compressionFlag: Int, val unknownFlag: Int, val compressedSize: Long, val decompressedSize: Long, val offset: ULong)