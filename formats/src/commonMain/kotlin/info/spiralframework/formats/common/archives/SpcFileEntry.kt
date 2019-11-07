package info.spiralframework.formats.common.archives

data class SpcFileEntry @ExperimentalUnsignedTypes constructor(val name: String, val compressionFlag: Int, val unknownFlag: Int, val compressedSize: Long, val decompressedSize: Long, val offset: ULong)