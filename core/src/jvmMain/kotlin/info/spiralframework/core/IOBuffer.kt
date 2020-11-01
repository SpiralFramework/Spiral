package info.spiralframework.core

import dev.brella.kornea.io.common.flow.OutputFlow
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

const val MAX_BUFFER_SIZE = 65_536
const val MAX_BUFFER_ALLOCATION = 16_000_000
const val MAX_BUFFER_OPERATIONS = MAX_BUFFER_ALLOCATION / MAX_BUFFER_SIZE

const val MAX_MISSING_DATA_COUNT = 8

val BUFFERED_IO_DISPATCHER = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

sealed class BufferIOOperation {
    class Open(val flow: OutputFlow) : BufferIOOperation()
    class Write(val buffer: ByteArray) : BufferIOOperation() {
        constructor(buffer: ByteArray, offset: Int, length: Int) : this(buffer.copyOfRange(offset, offset + length))
    }

    object Close : BufferIOOperation()
    class CloseAndPerform(val perform: suspend () -> Unit) : BufferIOOperation()
}

class IOBuffer {
    private var out: OutputFlow? = null
    private val outputBuffer: MutableList<BufferIOOperation> = ArrayList()

    @ExperimentalUnsignedTypes
    suspend fun buffer(bufferOP: BufferIOOperation) {
        if (outputBuffer.size >= MAX_BUFFER_OPERATIONS) {
            flush()
        }

        outputBuffer.add(bufferOP)
    }

    suspend fun flush() {
        outputBuffer.forEach { op ->
            when (op) {
                is BufferIOOperation.Open -> out = op.flow
                is BufferIOOperation.Write -> out?.write(op.buffer)
                is BufferIOOperation.Close -> {
                    out?.close()
                    out = null
                }
                is BufferIOOperation.CloseAndPerform -> {
                    out?.close()
                    out = null

                    op.perform()
                }
            }
        }

        outputBuffer.clear()
    }
}