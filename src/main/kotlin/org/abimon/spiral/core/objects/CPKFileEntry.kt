package org.abimon.spiral.core.objects

import org.abimon.spiral.util.OffsetInputStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readPartialBytes
import java.io.InputStream

data class CPKFileEntry(val fileName: String, val directoryName: String, val fileSize: Long, val extractSize: Long, val offset: Long, val cpk: CPK) : DataSource {
    val name: String = "$directoryName/$fileName"
    override val location: String = "CPK File ${cpk.dataSource.location}, offset $offset bytes (name $name)"

    override val data: ByteArray
        get() = use { it.readPartialBytes(size.toInt()) }

    override val inputStream: InputStream
        get() = OffsetInputStream(cpk.dataSource.inputStream, offset, fileSize)
    override val seekableInputStream: InputStream
        get() = OffsetInputStream(cpk.dataSource.seekableInputStream, offset, fileSize)

    override val size: Long = fileSize
}