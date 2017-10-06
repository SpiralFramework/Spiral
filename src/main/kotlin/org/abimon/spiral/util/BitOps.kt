package org.abimon.spiral.util

fun toFloat(bytes: ByteArray, little: Boolean = true, offset: Int = 0): Float {
    if(bytes.size < 4)
        throw IllegalArgumentException("Byte array too short (Size < 4)")

    return java.lang.Float.intBitsToFloat(toInt(bytes, little, true, offset))
}

fun toInt(bytes: ByteArray, little: Boolean = true, unsigned: Boolean = true, offset: Int = 0): Int {
    if(bytes.size - offset < 4)
        throw IllegalArgumentException("Byte array too short (Size < 4)")

    if(little)
        return ((bytes[offset + 3].toInt() and 0xFF) shl 24) or ((bytes[offset + 2].toInt() and 0xFF) shl 16) or ((bytes[offset + 1].toInt() and 0xFF) shl 8) or ((bytes[offset + 0].toInt() and 0xFF) shl 0)
    else
        return ((bytes[offset + 0].toInt() and 0xFF) shl 24) or ((bytes[offset + 1].toInt() and 0xFF) shl 16) or ((bytes[offset + 2].toInt() and 0xFF) shl 8) or ((bytes[offset + 3].toInt() and 0xFF) shl 0)
}

fun toShort(bytes: ByteArray, little: Boolean = true, unsigned: Boolean = true, offset: Int = 0): Int {
    if(bytes.size - offset < 2)
        throw IllegalArgumentException("Byte array too short (Size < 2)")

    if(little)
        return ((bytes[offset + 1].toInt() and 0xFF) shl 8) or ((bytes[offset + 0].toInt() and 0xFF) shl 0)
    else
        return ((bytes[offset + 0].toInt() and 0xFF) shl 8) or ((bytes[offset + 1].toInt() and 0xFF) shl 0)
}
