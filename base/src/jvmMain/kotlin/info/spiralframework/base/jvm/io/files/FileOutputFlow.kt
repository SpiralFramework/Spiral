package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.io.OutputFlow
import info.spiralframework.base.common.io.OutputFlowEventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@ExperimentalUnsignedTypes
class FileOutputFlow(val backing: File) : OutputFlow {
    override var onClose: OutputFlowEventHandler? = null
    private val stream = FileOutputStream(backing)

    override suspend fun write(byte: Int) = withContext(Dispatchers.IO) { stream.write(byte) }
    override suspend fun write(b: ByteArray) = write(b, 0, b.size)
    override suspend fun write(b: ByteArray, off: Int, len: Int) = withContext(Dispatchers.IO) { stream.write(b, off, len) }

    override suspend fun flush() = withContext(Dispatchers.IO) { stream.flush() }

    override suspend fun close() {
        super.close()
        withContext(Dispatchers.IO) { stream.close() }
    }
}