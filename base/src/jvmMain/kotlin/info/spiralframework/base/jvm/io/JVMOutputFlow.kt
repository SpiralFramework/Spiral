package info.spiralframework.base.jvm.io

import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.base.common.io.flow.OutputFlowEventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

@ExperimentalUnsignedTypes
open class JVMOutputFlow(val stream: OutputStream): OutputFlow {
    override var onClose: OutputFlowEventHandler? = null

    override suspend fun write(byte: Int) = withContext(Dispatchers.IO) { stream.write(byte) }
    override suspend fun write(b: ByteArray) = withContext(Dispatchers.IO) { stream.write(b) }
    override suspend fun write(b: ByteArray, off: Int, len: Int) = withContext(Dispatchers.IO) { stream.write(b, off, len) }
    override suspend fun flush() = withContext(Dispatchers.IO) { stream.flush() }

    override suspend fun close() {
        super.close()
        withContext(Dispatchers.IO) { stream.close() }
    }
}