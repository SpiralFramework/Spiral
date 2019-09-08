package info.spiralframework.base.jvm.io

import info.spiralframework.base.common.io.OutputFlow
import info.spiralframework.base.common.io.OutputFlowEventHandler
import java.io.OutputStream

@ExperimentalUnsignedTypes
open class JVMOutputFlow(val stream: OutputStream): OutputFlow {
    override var onClose: OutputFlowEventHandler? = null

    override fun write(byte: Int) = stream.write(byte)
    override fun write(b: ByteArray) = stream.write(b)
    override fun write(b: ByteArray, off: Int, len: Int) = stream.write(b, off, len)
    override fun flush() = stream.flush()

    override fun close() {
        super.close()
        stream.close()
    }
}