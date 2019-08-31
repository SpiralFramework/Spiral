package info.spiralframework.base.jvm.io

import info.spiralframework.base.common.io.DataSink
import info.spiralframework.base.common.io.DataSinkEventHandler
import java.io.OutputStream

@ExperimentalUnsignedTypes
open class JVMDataSink(val stream: OutputStream): DataSink {
    override var onClose: DataSinkEventHandler? = null

    override fun write(byte: Int) = stream.write(byte)
    override fun write(b: ByteArray) = stream.write(b)
    override fun write(b: ByteArray, off: Int, len: Int) = stream.write(b, off, len)
    override fun flush() = stream.flush()

    override fun close() {
        super.close()
        stream.close()
    }
}