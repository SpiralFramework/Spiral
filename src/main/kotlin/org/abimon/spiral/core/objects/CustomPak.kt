package org.abimon.spiral.core.objects

import org.abimon.spiral.core.writeNumber
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.writeTo
import java.io.OutputStream
import java.util.*

class CustomPak {
    val data: MutableList<DataSource> = ArrayList<DataSource>()

    fun dataSource(dataSource: DataSource): CustomPak {
        data.add(dataSource)
        return this
    }

    fun compile(pak: OutputStream) {
        var headerSize = 4 + data.size.times(4).toLong()

        pak.writeNumber(data.size.toLong(), unsigned = true)
        data.forEach {
            pak.writeNumber(headerSize, unsigned = true)
            headerSize += it.size
        }
        data.forEach { it.use { it.writeTo(pak, closeAfter = true) } }
    }
}