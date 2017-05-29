package org.abimon.util

import java.io.InputStream

open class DelegatedInputStream(val delegatedInputStream: InputStream) : InputStream() {
    override fun read(): Int = delegatedInputStream.read()
    override fun read(b: ByteArray): Int = delegatedInputStream.read(b)
    override fun read(b: ByteArray, off: Int, len: Int): Int = delegatedInputStream.read(b, off, len)
    override fun available(): Int = delegatedInputStream.available()
    override fun close() = delegatedInputStream.close()
    override fun mark(readlimit: Int) = delegatedInputStream.mark(readlimit)
    override fun markSupported(): Boolean = delegatedInputStream.markSupported()
    override fun reset() = delegatedInputStream.reset()
    override fun skip(n: Long): Long = delegatedInputStream.skip(n)
}

/**
 * Simple little wrapper that just does a count every time a byte is read
 */
open class CountingInputStream(countedInputStream: InputStream) : DelegatedInputStream(countedInputStream) {
    var count = 0L

    override fun read(): Int {
        count++
        return super.read()
    }

    override fun read(b: ByteArray): Int {
        val read = super.read(b)
        count += read.coerceAtLeast(0)
        return read
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val read = super.read(b, off, len)
        count += read.coerceAtLeast(0)
        return read
    }
}

class OffsetInputStream(offsetInputStream: InputStream, offset: Long, val overriding: Long = offsetInputStream.available().toLong()) : CountingInputStream(offsetInputStream) {

    override fun read(): Int = if (count < overriding) super.read() else -1
    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (count >= overriding)
            return -1
        return super.read(b, off, len.coerceAtMost((overriding - count).toInt()))
    }

    override fun available(): Int = (overriding - count).toInt()

    init {
        skip(offset)
        count = count.minus(offset).coerceAtLeast(0)
    }
}