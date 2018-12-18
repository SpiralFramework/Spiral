package org.abimon.spiral.core.utils

import java.io.InputStream

class WindowedInputStream(windowedInputStream: InputStream, val offset: Long, val windowSize: Long = windowedInputStream.available().toLong()) : CountingInputStream(windowedInputStream) {
    override val streamOffset: Long
        get() = offset + count

    override fun read(): Int = if (count < windowSize) super.read() else -1
    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (count >= windowSize)
            return -1
        return super.read(b, off, len.coerceAtMost((windowSize - count).toInt()))
    }

    override fun reset() {
        super.reset()
        skip(offset)
        count = 0
    }

    override fun available(): Int = (windowSize - count).toInt()

    init {
        skip(offset)
        count = count.minus(offset).coerceAtLeast(0)
    }
}