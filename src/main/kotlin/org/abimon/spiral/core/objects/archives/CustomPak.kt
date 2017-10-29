package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.writeInt
import org.abimon.visi.io.DataSource
import java.io.OutputStream
import java.util.*

class CustomPak {
    val data: MutableList<DataSource> = ArrayList<DataSource>()

    fun dataSource(dataSource: DataSource): CustomPak {
        data.add(dataSource)
        return this
    }

    fun compile(pak: OutputStream) {
        var headerSize: Long = (4 + data.size * 4).toLong()

        pak.writeInt(data.size)
        data.forEach {
            pak.writeInt(headerSize)
            headerSize += it.size
        }

        data.forEach { entry -> entry.use { stream -> stream.copyTo(pak) } }
    }
}