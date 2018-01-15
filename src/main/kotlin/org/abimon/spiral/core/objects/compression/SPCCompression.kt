package org.abimon.spiral.core.objects.compression

import org.abimon.spiral.core.utils.readOrNull
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object SPCCompression {
    fun decompress(compressionFlag: Int, data: ByteArray): ByteArray {
        when (compressionFlag) {
            0x01 -> return data
            0x02 -> {
                var flag = 1
                val result = LinkedList<Byte>()
                var i = 0

                while (i < data.size) {
                    if (flag == 1)
                        flag = 0x100 or (((data[i++].toInt() and 0xFF) * 0x0202020202 and 0x010884422010) % 1023).toInt()

                    if(i == data.size)
                        break

                    if (flag and 1 == 1) {
                        result.add(data[i++])
                    } else {
                        val x = data[i++].toInt() and 0xFF
                        val y = data[i++].toInt() and 0xFF

                        val b = (y shl 8) or x
                        val count = (b shr 10) + 2
                        val offset = ((b and 0b1111111111) - 1024)

                        for (j in 0 until count)
                            result.add(result[result.size + offset])
                    }

                    flag = flag shr 1
                }

                return result.toByteArray()
            }
            0x03 -> return data
            else -> throw IllegalArgumentException()
        }
    }

    fun decompress(compressionFlag: Int, data: InputStream): ByteArray {
        when (compressionFlag) {
            0x01 -> return data.readBytes()
            0x02 -> {
                var flag = 1
                val result = LinkedList<Byte>()

                while (true) {
                    if (flag == 1)
                        flag = 0x100 or ((((data.readOrNull() ?: break) and 0xFF) * 0x0202020202 and 0x010884422010) % 1023).toInt()

                    if (flag and 1 == 1) {
                        result.add(data.readOrNull()?.toByte() ?: break)
                    } else {
                        val x = (data.readOrNull() ?: break) and 0xFF
                        val y = (data.readOrNull() ?: break) and 0xFF

                        val b = (y shl 8) or x
                        val count = (b shr 10) + 2
                        val offset = ((b and 0b1111111111) - 1024)

                        for (j in 0 until count)
                            result.add(result[result.size + offset])
                    }

                    flag = flag shr 1
                }

                return result.toByteArray()
            }
            0x03 -> return data.readBytes()
            else -> throw IllegalArgumentException()
        }
    }

    fun decompressToPipe(compressionFlag: Int, data: InputStream, pipe: OutputStream) {
        when (compressionFlag) {
            0x01 -> data.copyTo(pipe)
            0x02 -> {
                var flag = 1
                val result = LinkedList<Byte>()

                while (true) {
                    if (flag == 1)
                        flag = 0x100 or ((((data.readOrNull() ?: break) and 0xFF) * 0x0202020202 and 0x010884422010) % 1023).toInt()

                    if (flag and 1 == 1) {
                        result.add(data.readOrNull()?.toByte() ?: break)
                    } else {
                        val x = (data.readOrNull() ?: break) and 0xFF
                        val y = (data.readOrNull() ?: break) and 0xFF

                        val b = (y shl 8) or x
                        val count = (b shr 10) + 2
                        val offset = ((b and 0b1111111111) - 1024)

                        for (j in 0 until count)
                            result.add(result[result.size + offset])
                    }

                    if (result.size > 2048)
                        pipe.write(ByteArray(1024) { result.pop() })

                    flag = flag shr 1
                }

                pipe.write(result.toByteArray())
            }
            0x03 -> data.copyTo(pipe)
            else -> throw IllegalArgumentException()
        }
    }
}