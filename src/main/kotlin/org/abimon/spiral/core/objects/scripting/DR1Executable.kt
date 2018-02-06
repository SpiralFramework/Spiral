package org.abimon.spiral.core.objects.scripting

import java.io.File
import java.io.RandomAccessFile

abstract class DR1Executable(val file: File) {
    companion object {

    }

    val raf = RandomAccessFile(file, "rw")

    abstract val pakNames: Map<String, Array<String>>

    fun String.trimToBytes(len: Int): ByteArray {
        val array = toByteArray(Charsets.UTF_8)

        return array.copyOfRange(0, minOf(len, array.size))
    }

    fun RandomAccessFile.readString(len: Int): String = String(ByteArray(len).apply { read(this) }, Charsets.UTF_8)
    fun RandomAccessFile.readZeroString(maxLen: Int = 255): String = buildString {
        for (i in 0 until maxLen) {
            val read = read()
            if(read == 0x00)
                break

            append(read.toChar())
        }
    }
}