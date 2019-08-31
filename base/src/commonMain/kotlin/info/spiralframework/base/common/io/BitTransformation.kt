package info.spiralframework.base.common.io

@Suppress("NOTHING_TO_INLINE")
inline fun toUTF16LE(higherByte: Byte, lowerByte: Byte): Int = ((higherByte.toInt() and 0xFF) shl 8) or (lowerByte.toInt() and 0xFF)
@Suppress("NOTHING_TO_INLINE")
inline fun toUTF16BE(lowerByte: Byte, higherByte: Byte): Int = ((higherByte.toInt() and 0xFF) shl 8) or (lowerByte.toInt() and 0xFF)