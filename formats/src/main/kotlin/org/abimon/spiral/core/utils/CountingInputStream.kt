package org.abimon.spiral.core.utils

import java.io.InputStream

/**
 * Simple little wrapper that just does a count every time a byte is read
 */
open class CountingInputStream(countedInputStream: InputStream) : DelegatedInputStream(countedInputStream) {
    var count = 0L
    open val streamOffset: Long
        get() = count

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

    override fun skip(n: Long): Long {
        val amount = super.skip(n)
        count += amount
        return amount
    }

    override fun reset() {
        super.reset()
        count = 0L
    }
}