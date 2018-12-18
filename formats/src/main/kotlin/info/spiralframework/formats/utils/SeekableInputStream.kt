package info.spiralframework.formats.utils

import java.io.InputStream

class SeekableInputStream(seekable: InputStream): CountingInputStream(seekable) {
    fun seek(offset: Long) {
        reset()
        skip(offset)
    }
}