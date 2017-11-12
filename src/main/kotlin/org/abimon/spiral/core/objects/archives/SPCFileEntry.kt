package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.util.OffsetInputStream
import org.abimon.spiral.util.trace
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readChunked
import org.abimon.visi.io.readPartialBytes
import org.abimon.visi.lang.exportStackTrace
import org.abimon.visi.security.md5Hash
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.system.measureNanoTime

data class SPCFileEntry(val cmp_flag: Int, val unk_flag: Int, val cmp_size: Long, val dec_size: Long, val name: String, val offset: Long, val parent: SPC) : DataSource {
    override val data: ByteArray
        get() = dataSource.use { it.readBytes() }
    override val inputStream: InputStream
        get() = dataSource.inputStream
    override val location: String
        get() = "SPC File ${parent.dataSource.location}, offset $offset bytes (name $name, flag $cmp_flag)"
    override val seekableInputStream: InputStream
        get() = dataSource.seekableInputStream
    override val size: Long
        get() = dec_size

    val rawInputStream: InputStream
        get() = OffsetInputStream(parent.dataSource.inputStream, offset, cmp_size)

    val dataSource: DataSource by lazy {
        val (data, raf, initialised) = CacheHandler.cacheRandomAccessStream("${"$location-$cmp_size".md5Hash()}.dat")

        if (!initialised) {
            val nano = measureNanoTime {
                raf.use { raw ->
                    raw.channel.use { access ->
                        when (cmp_flag) {
                            0x01 -> rawInputStream.use { it.readChunked { chunk -> raw.write(chunk) } }
                            0x02 -> {
                                var flag = 1
                                rawInputStream.use { stream ->
                                    try {
                                        val buffer = ByteBuffer.allocate(4096)
                                        val tmp = ByteBuffer.allocate(4096)
                                        loop@ while (stream.available() > 0) {
                                            if (flag == 1)
                                                flag = 0x100 or ((stream.read() * 0x0202020202 and 0x010884422010) % 1023).toInt()

                                            if (stream.available() == 0)
                                                break@loop

                                            if (flag and 1 == 1) {
                                                var count = 0
                                                while (flag and 1 == 1 && flag != 1) {
                                                    flag = flag shr 1
                                                    count++
                                                }

                                                buffer.put(stream.readPartialBytes(count))
                                            } else {
                                                val x = stream.read()
                                                val y = stream.read()

                                                val b = (y shl 8) or x
                                                val count = (b shr 10) + 2
                                                val offset = buffer.position() + ((b and 0b1111111111) - 1024)

                                                for (i in 0 until count)
                                                    buffer.put(buffer[i + offset])

                                                flag = flag shr 1
                                            }

                                            if (buffer.position() >= 2048) {
                                                val oldLimit = buffer.position()
                                                buffer.flip()
                                                buffer.limit(1024)
                                                access.write(buffer)

                                                buffer.limit(oldLimit)
                                                tmp.put(buffer)
                                                buffer.clear()
                                                tmp.flip()
                                                buffer.put(tmp)
                                                tmp.clear()
                                            }
                                        }


                                        buffer.flip()
                                        access.write(buffer)
                                    } catch (oom: OutOfMemoryError) {
                                        println("OOM Error: ${oom.exportStackTrace()}")
                                    }
                                }
                            }
                            0x03 -> {
                                println("Ext. File")
                                rawInputStream.use { it.readChunked { chunk -> access.write(ByteBuffer.wrap(chunk)) } }
                            }
                            else -> throw IllegalArgumentException("${parent.dataSource.location} is an invalid/corrupt SPC File! (Unknown cmp flag $cmp_flag)")
                        }
                    }
                }
            }

            trace("[SPCFileEntry -> dataSource] $name DS time: $nano ns")
        }

        return@lazy data
    }

    init {
        trace("Initialising $name")
    }
}