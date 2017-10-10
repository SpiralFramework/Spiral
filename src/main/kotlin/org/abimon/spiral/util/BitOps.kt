package org.abimon.spiral.util

import org.abimon.spiral.core.byteArrayOfInts

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

fun toInt(bytes: IntArray, little: Boolean = true, unsigned: Boolean = true, offset: Int = 0): Int {
    if(bytes.size - offset < 4)
        throw IllegalArgumentException("Byte array too short (Size < 4)")

    if(little)
        return ((bytes[offset + 3]) shl 24) or ((bytes[offset + 2]) shl 16) or ((bytes[offset + 1]) shl 8) or ((bytes[offset + 0]) shl 0)
    else
        return ((bytes[offset + 0]) shl 24) or ((bytes[offset + 1]) shl 16) or ((bytes[offset + 2]) shl 8) or ((bytes[offset + 3]) shl 0)
}

fun toShort(bytes: IntArray, little: Boolean = true, unsigned: Boolean = true, offset: Int = 0): Int {
    if(bytes.size - offset < 2)
        throw IllegalArgumentException("Byte array too short (Size < 2)")

    if(little)
        return ((bytes[offset + 1]) shl 8) or ((bytes[offset + 0]) shl 0)
    else
        return ((bytes[offset + 0]) shl 8) or ((bytes[offset + 1]) shl 0)
}

fun longToByteArray(num: Number, unsigned: Boolean = false, little: Boolean = true): ByteArray {
    val long = num.toLong()
    if(unsigned && little)
        return byteArrayOf(long.toByte(), (long ushr 8).toByte(), (long ushr 16).toByte(), (long ushr 24).toByte(), (long ushr 32).toByte(), (long ushr 40).toByte(), (long ushr 48).toByte(), (long ushr 56).toByte())
    else if(!unsigned && little)
        return byteArrayOf(long.toByte(), (long shr 8).toByte(), (long shr 16).toByte(), (long shr 24).toByte(), (long shr 32).toByte(), (long shr 40).toByte(), (long shr 48).toByte(), (long shr 56).toByte())
    else if(unsigned && !little)
        return byteArrayOf((long ushr 56).toByte(), (long ushr 48).toByte(), (long ushr 40).toByte(), (long ushr 32).toByte(), (long ushr 24).toByte(), (long ushr 16).toByte(),(long ushr 8).toByte(), long.toByte())
    else
        return byteArrayOf((long shr 56).toByte(), (long shr 48).toByte(), (long shr 40).toByte(), (long shr 32).toByte(), (long shr 24).toByte(), (long shr 16).toByte(),(long shr 8).toByte(), long.toByte())
}

fun intToByteArray(num: Number, unsigned: Boolean = false, little: Boolean = true): ByteArray {
    val int = num.toInt()
    if(unsigned && little)
        return byteArrayOfInts(int, int ushr 8, int ushr 16, int ushr 24)
    else if(!unsigned && little)
        return byteArrayOfInts(int, int shr 8, int shr 16, int shr 24)
    else if(unsigned && !little)
        return byteArrayOfInts(int ushr 24, int ushr 16, int ushr 8, int)
    else
        return byteArrayOfInts(int shr 24, int shr 16, int shr 8, int)
}

fun shortToByteArray(num: Number, unsigned: Boolean = false, little: Boolean = true): ByteArray {
    val int = num.toShort().toInt()
    if (unsigned && little)
        return byteArrayOfInts(int, int ushr 8)
    else if (!unsigned && little)
        return byteArrayOfInts(int, int shr 8)
    else if (unsigned && !little)
        return byteArrayOfInts(int ushr 8, int)
    else
        return byteArrayOfInts(int shr 8, int)
}

fun longToIntArray(num: Number, unsigned: Boolean = false, little: Boolean = true): IntArray {
    val long = num.toLong()
    if(unsigned && little)
        return intArrayOf(long.toInt(), (long ushr 8).toInt(), (long ushr 16).toInt(), (long ushr 24).toInt(), (long ushr 32).toInt(), (long ushr 40).toInt(), (long ushr 48).toInt(), (long ushr 56).toInt())
    else if(!unsigned && little)
        return intArrayOf(long.toInt(), (long shr 8).toInt(), (long shr 16).toInt(), (long shr 24).toInt(), (long shr 32).toInt(), (long shr 40).toInt(), (long shr 48).toInt(), (long shr 56).toInt())
    else if(unsigned && !little)
        return intArrayOf((long ushr 56).toInt(), (long ushr 48).toInt(), (long ushr 40).toInt(), (long ushr 32).toInt(), (long ushr 24).toInt(), (long ushr 16).toInt(),(long ushr 8).toInt(), long.toInt())
    else
        return intArrayOf((long shr 56).toInt(), (long shr 48).toInt(), (long shr 40).toInt(), (long shr 32).toInt(), (long shr 24).toInt(), (long shr 16).toInt(),(long shr 8).toInt(), long.toInt())
}

fun intToIntArray(num: Number, unsigned: Boolean = false, little: Boolean = true): IntArray {
    val int = num.toInt()
    if(unsigned && little)
        return intArrayOf(int, int ushr 8, int ushr 16, int ushr 24)
    else if(!unsigned && little)
        return intArrayOf(int, int shr 8, int shr 16, int shr 24)
    else if(unsigned && !little)
        return intArrayOf(int ushr 24, int ushr 16, int ushr 8, int)
    else
        return intArrayOf(int shr 24, int shr 16, int shr 8, int)
}

fun shortToIntArray(num: Number, unsigned: Boolean = false, little: Boolean = true): IntArray {
    val int = num.toShort().toInt()
    if (unsigned && little)
        return intArrayOf(int, int ushr 8)
    else if (!unsigned && little)
        return intArrayOf(int, int shr 8)
    else if (unsigned && !little)
        return intArrayOf(int ushr 8, int)
    else
        return intArrayOf(int shr 8, int)
}