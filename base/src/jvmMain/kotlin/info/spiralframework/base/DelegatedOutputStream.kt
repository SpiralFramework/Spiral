package info.spiralframework.base

import java.io.OutputStream

@Deprecated("Use InputFlow instead")
open class DelegatedOutputStream(val delegatedOutputStream: OutputStream): OutputStream() {
    override fun write(b: Int) = delegatedOutputStream.write(b)
    override fun write(b: ByteArray?) = delegatedOutputStream.write(b)
    override fun write(b: ByteArray?, off: Int, len: Int) = delegatedOutputStream.write(b, off, len)
    override fun flush() = delegatedOutputStream.flush()
    override fun close() = delegatedOutputStream.close()
}