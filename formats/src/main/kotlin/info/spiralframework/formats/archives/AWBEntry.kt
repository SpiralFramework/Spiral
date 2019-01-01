package info.spiralframework.formats.archives

import info.spiralframework.formats.utils.WindowedInputStream
import java.io.InputStream

data class AWBEntry(val id: Int, val size: Long, val offset: Long, val awb: AWB) {
    val inputStream: InputStream
        get() = WindowedInputStream(awb.dataSource(), offset, size)
}