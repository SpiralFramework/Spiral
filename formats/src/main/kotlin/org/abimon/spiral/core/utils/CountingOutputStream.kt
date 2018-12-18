package org.abimon.spiral.core.utils

import java.io.OutputStream

/**
 * Simple little wrapper that just does a count every time a byte is written
 */
open class CountingOutputStream(stream: OutputStream): DelegatedOutputStream(stream) {
    var count = 0L
    open val streamOffset: Long
        get() = count

    override fun write(b: Int) {
        count++
        super.write(b)
    }

    override fun write(b: ByteArray?) {
        count += b?.size?.coerceAtLeast(0) ?: 0

        super.write(b)
    }

    override fun write(b: ByteArray?, off: Int, len: Int) {
        count += len

        super.write(b, off, len)
    }
}