package org.abimon.spiral.core.utils

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