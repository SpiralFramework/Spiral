package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.OffsetInputStream
import org.abimon.spiral.core.utils.WindowedInputStream
import java.io.InputStream

data class PakEntry(val index: Int, val size: Int, val offset: Int, val pak: Pak) {
    val inputStream: InputStream
        get() = if(size == -1) OffsetInputStream(pak.dataSource(), offset.toLong()) else WindowedInputStream(pak.dataSource(), offset.toLong(), size.toLong())
}