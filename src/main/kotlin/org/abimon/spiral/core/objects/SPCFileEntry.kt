package org.abimon.spiral.core.objects

import org.abimon.spiral.core.data.BitPool
import org.abimon.spiral.core.hasBitSet
import org.abimon.spiral.util.OffsetInputStream
import org.abimon.visi.io.DataSource
import java.io.ByteArrayInputStream
import java.io.InputStream

class SPCFileEntry(val cmp_flag: Int, val unk_flag: Int, val cmp_size: Int, val dec_size: Int, val name: String, val offset: Long, val parent: SPC): DataSource {
    override val data: ByteArray
        get() {
            when(cmp_flag) {
                0x01 -> return rawData
                0x02 -> {
                    val result: MutableList<Int> = ArrayList()
                    var flag = 1

                    val bitpool = BitPool(rawData, false)

                    while(!bitpool.isEmpty) {
                        if(flag == 1)
                            flag = 0x100 or ((bitpool[8] * 0x0202020202 and 0x010884422010) % 1023).toInt()

                        if(bitpool.isEmpty)
                            break

                        if(flag hasBitSet 1)
                            result.add(bitpool[8])
                        else {
//                            val count = bitpool[6]
//                            val offset = bitpool[10]

                            val x = bitpool[8]
                            val y = bitpool[8]

                            val b = (y shl 8) or x
                            val count = (b shr 10) + 2
                            val offset = b and 0b1111111111

                            for(j in 0 until count) {
                                val r = offset - 1024
                                try {
                                    result.add(result[if (r < 0) result.size + r else r])
                                }
                                catch(oob: ArrayIndexOutOfBoundsException) {
                                    println(r)
                                    oob.printStackTrace()
                                }
                            }
                        }

                        flag = flag shr 1
                    }

                    return result.map { it.toByte() }.toByteArray()
                }
                0x03 -> {
                    println("Ext. File")
                    return rawData
                }
                else -> throw IllegalArgumentException("${parent.dataSource.location} is an invalid/corrupt SPC File! (Unknown cmp flag $cmp_flag)")
            }
        }
    override val inputStream: InputStream
        get() = if(cmp_flag == 0x01) rawInputStream else ByteArrayInputStream(data)
    override val location: String
        get() = "SPC File ${parent.dataSource.location}, offset $offset bytes (name $name, flag $cmp_flag)"
    override val seekableInputStream: InputStream
        get() = inputStream
    override val size: Long
        get() = dec_size.toLong()

    val rawInputStream: InputStream
        get() = OffsetInputStream(parent.dataSource.inputStream, offset, cmp_size.toLong())

    val rawData: ByteArray by lazy { rawInputStream.use { ByteArray(cmp_size).apply { it.read(this) } } }
}