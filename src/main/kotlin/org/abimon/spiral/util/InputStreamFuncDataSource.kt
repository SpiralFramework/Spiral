package org.abimon.spiral.util

import org.abimon.visi.io.DataSource
import java.io.InputStream
import kotlin.reflect.jvm.jvmName

class InputStreamFuncDataSource(val dataSource: () -> InputStream) : DataSource {
    override val data: ByteArray by lazy { dataSource().use { stream -> stream.readBytes() } }
    override val inputStream: InputStream
        get() = dataSource()
    override val location: String = dataSource::class.jvmName
    override val seekableInputStream: InputStream
        get() = dataSource()
    override val size: Long by lazy {
        dataSource().use { stream ->
            var size: Long = 0
            val buffer = ByteArray(8192)
            var read: Int

            do {
                read = stream.read(buffer)
                size += read
            } while (read > 0)

            return@use size
        }
    }
}