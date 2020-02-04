package info.spiralframework.base

import org.abimon.kornea.io.jvm.DelegatedInputStream
import java.io.InputStream

@Deprecated("Use InputFlow instead")
class HeaderInputStream(val header: InputStream, base: InputStream): DelegatedInputStream(base) {
    val shouldReadHeader: Boolean
        get() = header.available() > 0

    override fun read(): Int = if (shouldReadHeader) header.read() else super.read()
    override fun read(b: ByteArray): Int {
        if (shouldReadHeader) {
            val read = header.read(b)
            if (read < b.size)
                return super.read(b, read, b.size - read) + read
            return read
        } else {
            return super.read(b)
        }
    }
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (shouldReadHeader) {
            val read = header.read(b, off, len)
            if (read < len)
                return super.read(b, off + read, len - read)
            return read
        } else {
            return super.read(b, off, len)
        }
    }
    override fun available(): Int = if (shouldReadHeader) header.available() + super.available() else super.available()
    override fun close() {
        header.close()
        super.close()
    }
    override fun mark(readlimit: Int) {
        if (shouldReadHeader) {
            header.mark(readlimit)
        } else {
            super.mark(readlimit)
        }
    }
    override fun markSupported(): Boolean {
        if (shouldReadHeader) {
            return header.markSupported()
        } else {
            return super.markSupported()
        }
    }
    override fun reset() {
        if (shouldReadHeader) {
            header.reset()
        } else {
            super.reset()
        }
    }
    override fun skip(n: Long): Long {
        if (shouldReadHeader) {
            val skipped = header.skip(n)
            if (skipped < n)
                return skipped + super.skip(n - skipped)
            return skipped
        } else {
            return super.skip(n)
        }
    }
}