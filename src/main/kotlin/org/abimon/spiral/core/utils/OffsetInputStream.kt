package org.abimon.spiral.core.utils

import java.io.InputStream

class OffsetInputStream(offsetInputStream: InputStream, val offset: Long, val overriding: Long = offsetInputStream.available().toLong()) : CountingInputStream(offsetInputStream) {
    override val streamOffset: Long
        get() = offset + count

    override fun read(): Int = if (count < overriding) super.read() else -1
    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (count >= overriding)
            return -1
        return super.read(b, off, len.coerceAtMost((overriding - count).toInt()))
    }

    override fun reset() {
        super.reset()
        skip(offset)
        count = 0
    }

    override fun available(): Int = (overriding - count).toInt()

    init {
        skip(offset)
        count = count.minus(offset).coerceAtLeast(0)
    }
}