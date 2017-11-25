package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.util.OffsetInputStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import java.io.InputStream

data class WADSubdirectoryEntry(val name: String, val subfiles: List<WADSubfileEntry>)
data class WADSubfileEntry(val name: String, val isDirectory: Boolean)
data class WADFileEntry(val name: String, val fileSize: Long, val offset: Long, val wad: WAD) : DataSource {
    override val location: String = "WAD File ${wad.dataSource.location}, offset ${wad.dataOffset + offset} bytes (name $name)"

    override val data: ByteArray
        get() = use { it.read(size.toInt()) }

    override val inputStream: InputStream
        get() = OffsetInputStream(wad.dataSource.inputStream, wad.dataOffset + offset, fileSize)
    override val seekableInputStream: InputStream
        get() = OffsetInputStream(wad.dataSource.seekableInputStream, wad.dataOffset + offset, fileSize)

    override val size: Long = fileSize
}