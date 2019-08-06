package info.spiralframework.formats.archives

import info.spiralframework.base.WindowedInputStream
import java.io.InputStream

data class WADFileEntry(val name: String, val size: Long, val offset: Long, val wad: WAD) {
    val inputStream: InputStream
        get() = WindowedInputStream(wad.dataSource(), wad.dataOffset + offset, size)
}