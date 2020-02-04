package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.io.DataCloseableEventHandler
import info.spiralframework.base.common.io.flow.CountingOutputFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@ExperimentalUnsignedTypes
class FileOutputFlow(val backing: File) : CountingOutputFlow {
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()

    private val stream = FileOutputStream(backing)
    private val channel = stream.channel
    override val streamOffset: Long
        get() = channel.position()

    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override suspend fun write(byte: Int) = withContext(Dispatchers.IO) { stream.write(byte) }
    override suspend fun write(b: ByteArray) = write(b, 0, b.size)
    override suspend fun write(b: ByteArray, off: Int, len: Int) = withContext(Dispatchers.IO) { stream.write(b, off, len) }

    override suspend fun flush() = withContext(Dispatchers.IO) { stream.flush() }

    override suspend fun close() {
        super.close()

        if (!closed) {
            withContext(Dispatchers.IO) { stream.close() }
            closed = true
        }
    }
}