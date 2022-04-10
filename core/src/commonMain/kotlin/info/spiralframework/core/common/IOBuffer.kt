package info.spiralframework.core.common

import dev.brella.kornea.io.common.flow.OutputFlow

public const val MAX_BUFFER_SIZE: Int = 65_536
public const val MAX_BUFFER_ALLOCATION: Int = 16_000_000
public const val MAX_BUFFER_OPERATIONS: Int = MAX_BUFFER_ALLOCATION / MAX_BUFFER_SIZE

public const val MAX_MISSING_DATA_COUNT: Int = 8

public sealed class BufferIOOperation {
    public class Open(public val flow: OutputFlow) : BufferIOOperation()
    public class Write(public val buffer: ByteArray) : BufferIOOperation() {
        public constructor(buffer: ByteArray, offset: Int, length: Int) : this(buffer.copyOfRange(offset, offset + length))
    }

    public object Close : BufferIOOperation()
    public class CloseAndPerform(public val perform: suspend () -> Unit) : BufferIOOperation()
}

public class IOBuffer {
    private var out: OutputFlow? = null
    private val outputBuffer: MutableList<BufferIOOperation> = ArrayList()

    public suspend fun buffer(bufferOP: BufferIOOperation) {
        if (outputBuffer.size >= MAX_BUFFER_OPERATIONS) {
            flush()
        }

        outputBuffer.add(bufferOP)
    }

    public suspend fun flush() {
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