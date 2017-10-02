package org.abimon.spiral.core.objects

import org.abimon.spiral.util.OffsetInputStream
import org.abimon.visi.io.DataSource
import java.io.InputStream

data class PakFileEntry(val name: String, val fileSize: Long, val offset: Long, val pak: Pak) : DataSource {
    override val location: String = "PAK File ${pak.dataSource.location}, offset $offset bytes"

    override val data: ByteArray
        get() = use { it.readBytes() }

    override val inputStream: InputStream
        get() = OffsetInputStream(pak.dataSource.inputStream, offset, fileSize)
    override val seekableInputStream: InputStream
        get() = OffsetInputStream(pak.dataSource.seekableInputStream, offset, fileSize)

    override val size: Long = fileSize
}