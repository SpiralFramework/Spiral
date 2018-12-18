package info.spiralframework.formats.archives

import info.spiralframework.formats.utils.OffsetInputStream
import info.spiralframework.formats.utils.WindowedInputStream
import java.io.InputStream

data class PakEntry(val index: Int, val size: Int, val offset: Int, val pak: Pak) {
    val inputStream: InputStream
        get() = if(size == -1) OffsetInputStream(pak.dataSource(), offset.toLong()) else WindowedInputStream(pak.dataSource(), offset.toLong(), size.toLong())
}