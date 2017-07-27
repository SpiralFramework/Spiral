package org.abimon.spiral.core.objects

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.formats.PNGFormat
import org.abimon.spiral.core.writeNumber
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FunctionDataSource
import org.abimon.visi.io.readAllBytes
import org.abimon.visi.io.writeTo
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipInputStream

class CustomPak() {
    constructor(dataSource: DataSource): this() {
        if (SpiralFormats.ZIP.isFormat(dataSource)) {
            val zipIn = ZipInputStream(dataSource.inputStream)
            val entries = HashMap<String, DataSource>()

            while (true) {
                entries[zipIn.nextEntry?.name?.substringBeforeLast('.') ?: break] = run {
                    val data = zipIn.readBytes()
                    return@run FunctionDataSource { data }
                }
            }
            entries.filterKeys { key -> key.toIntOrNull() != null }.toSortedMap(Comparator { o1, o2 -> o1.toInt().compareTo(o2.toInt()) }).forEach { _, data -> dataSource(data) }
        } else
            throw IllegalArgumentException("${dataSource.location} is not a ZIP file/stream!")
    }

    val data = LinkedList<DataSource>()

    fun dataSource(dataSource: DataSource): CustomPak {
        data.add(dataSource)
        return this
    }

    fun compile(pak: OutputStream) {
        val modified = LinkedList<DataSource>()

        data.forEach {
            val format = SpiralFormats.formatForData(it)
            if (format != null) {
                when (format) {
                    is PNGFormat -> {
                        val baos = ByteArrayOutputStream()
                        format.convert(SpiralFormats.TGA, it, baos)
                        val data = baos.toByteArray()
                        modified.add(FunctionDataSource { data })
                        baos.close()
                    }
                    else -> modified.add(it)
                }
            } else
                modified.add(it)
        }
        var headerSize = 4 + modified.size.times(4).toLong()

        pak.writeNumber(modified.size.toLong(), unsigned = true)
        modified.forEach {
            pak.writeNumber(headerSize, unsigned = true)
            headerSize += it.size
        }
        modified.forEach { it.use { it.writeTo(pak, closeAfter = true) } }
    }
}