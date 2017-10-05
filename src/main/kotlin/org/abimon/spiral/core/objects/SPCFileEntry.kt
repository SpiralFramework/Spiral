package org.abimon.spiral.core.objects

import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.hasBitSet
import org.abimon.spiral.util.OffsetInputStream
import org.abimon.spiral.util.debug
import org.abimon.spiral.util.trace
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readChunked
import org.abimon.visi.lang.exportStackTrace
import org.abimon.visi.security.md5Hash
import java.io.InputStream
import kotlin.system.measureNanoTime

class SPCFileEntry(val cmp_flag: Int, val unk_flag: Int, val cmp_size: Int, val dec_size: Int, val name: String, val offset: Long, val parent: SPC): DataSource {
    override val data: ByteArray
        get() = dataSource.use { it.readBytes() }
    override val inputStream: InputStream
        get() = dataSource.inputStream
    override val location: String
        get() = "SPC File ${parent.dataSource.location}, offset $offset bytes (name $name, flag $cmp_flag)"
    override val seekableInputStream: InputStream
        get() = dataSource.seekableInputStream
    override val size: Long
        get() = dec_size.toLong()

    val rawInputStream: InputStream
        get() = OffsetInputStream(parent.dataSource.inputStream, offset, cmp_size.toLong())

    val dataSource: DataSource by lazy {
        val (data, raf, initialised) = CacheHandler.cacheRandomAccessStream("${location.md5Hash()}.dat")

        if(initialised) {
            val nano = measureNanoTime {
                raf.use { access ->
                    when (cmp_flag) {
                        0x01 -> rawInputStream.use { it.readChunked { chunk -> access.write(chunk) } }
                        0x02 -> {
                            var flag = 1

                            rawInputStream.use { stream ->
                                try {
                                    while (stream.available() > 0) {
                                        if (flag == 1)
                                            flag = 0x100 or ((stream.read() * 0x0202020202 and 0x010884422010) % 1023).toInt()

                                        if (stream.available() == 0)
                                            break

                                        if (flag hasBitSet 1)
                                            access.write(stream.read())
                                        else {
//                            val count = bitpool[6]
//                            val offset = bitpool[10]

                                            val x = stream.read()
                                            val y = stream.read()

                                            val b = (y shl 8) or x
                                            val count = (b shr 10) + 2
                                            val offset = b and 0b1111111111

                                            try {
                                                val r = offset - 1024
                                                if (r > 0) {
                                                    debug(":thonk:")
                                                    access.seek(r.toLong())
                                                    val byte = access.read()
                                                    access.seek(access.length())
                                                    for (j in 0 until count)
                                                        access.write(byte)
                                                } else {
                                                    val buffer = ByteArray(count)
                                                    access.seek(access.length() + r)
                                                    access.read(buffer)
                                                    val remaining = (access.length() - (access.length() + r)).toInt()
                                                    if (remaining < count) {
                                                        for (j in remaining until count)
                                                            buffer[j] = buffer[j % remaining]
                                                    }
                                                    access.seek(access.length())
                                                    access.write(buffer)
                                                }
                                            } catch (oom: OutOfMemoryError) {
                                                println("OOM Error: ${oom.exportStackTrace()}")
                                                println("Adding: $count")
                                                println("Offset: $offset")

                                            }
                                        }

                                        flag = flag shr 1
                                    }
                                } catch (oom: OutOfMemoryError) {
                                    println("OOM Error: ${oom.exportStackTrace()}")
                                }
                            }
                        }
                        0x03 -> {
                            println("Ext. File")
                            rawInputStream.use { it.readChunked { chunk -> access.write(chunk) } }
                        }
                        else -> throw IllegalArgumentException("${parent.dataSource.location} is an invalid/corrupt SPC File! (Unknown cmp flag $cmp_flag)")
                    }
                }
            }

            trace("[SPCFileEntry -> dataSource] $name DS time: $nano")
        }

        return@lazy data
    }

    init {
        trace("Initialising $name")
    }
}