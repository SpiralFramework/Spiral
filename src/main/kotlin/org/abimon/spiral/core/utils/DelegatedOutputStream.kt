package org.abimon.spiral.core.utils

import java.io.OutputStream

open class DelegatedOutputStream(val delegatedOutputStream: OutputStream): OutputStream() {
    override fun write(b: Int) = delegatedOutputStream.write(b)
    override fun write(b: ByteArray?) = delegatedOutputStream.write(b)
    override fun write(b: ByteArray?, off: Int, len: Int) = delegatedOutputStream.write(b, off, len)
    override fun flush() = delegatedOutputStream.flush()
    override fun close() = delegatedOutputStream.close()
}