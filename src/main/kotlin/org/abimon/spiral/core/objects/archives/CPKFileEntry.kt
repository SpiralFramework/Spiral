package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.compression.CRILAYLAFormat
import org.abimon.spiral.util.OffsetInputStream
import org.abimon.spiral.util.bind
import org.abimon.visi.io.DataSource
import java.io.InputStream

data class CPKFileEntry(val fileName: String, val directoryName: String, val fileSize: Long, val extractSize: Long, val offset: Long, val isCompressed: Boolean, val cpk: CPK) : DataSource {
    val name: String = "$directoryName/$fileName"
    override val location: String = "CPK File ${cpk.dataSource.location}, offset $offset bytes (name $name)"

    override val data: ByteArray
        get() = use { it.readBytes() }

    override val inputStream: InputStream
        get() = if (isCompressed) dataSource.inputStream else OffsetInputStream(cpk.dataSource.inputStream, offset, fileSize)
    override val seekableInputStream: InputStream
        get() = if (isCompressed) dataSource.inputStream else OffsetInputStream(cpk.dataSource.seekableInputStream, offset, fileSize)

    override val size: Long = extractSize

    val dataSource: DataSource by lazy {
        if (isCompressed) {
            val (out, data) = CacheHandler.cacheStream()
            out.use { outStream ->
                CRILAYLAFormat.convert(null, SpiralFormat.BinaryFormat, name, ::OffsetInputStream.bind(cpk.dataSource.inputStream, offset, fileSize), outStream, emptyMap())
            }

            return@lazy data
        } else
            return@lazy this
    }
}