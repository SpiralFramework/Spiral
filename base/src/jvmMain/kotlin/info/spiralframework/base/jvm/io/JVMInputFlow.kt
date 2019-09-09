package info.spiralframework.base.jvm.io

import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.InputFlowEventHandler
import info.spiralframework.base.common.io.flow.readResultIsValid
import info.spiralframework.base.common.io.flow.skip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

@ExperimentalUnsignedTypes
open class JVMInputFlow private constructor(val stream: CountingInputStream): InputFlow {
    constructor(stream: InputStream): this(CountingInputStream(stream))
    override var onClose: InputFlowEventHandler? = null

    override suspend fun read(): Int? = withContext(Dispatchers.IO) { stream.read().takeIf(::readResultIsValid) }
    override suspend fun read(b: ByteArray): Int? = withContext(Dispatchers.IO) { stream.read(b).takeIf(::readResultIsValid) }
    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? = withContext(Dispatchers.IO) { stream.read(b, off, len).takeIf(::readResultIsValid) }
    override suspend fun skip(n: ULong): ULong? = withContext(Dispatchers.IO) { stream.skip(n.toLong()).toULong() }
    override suspend fun available(): ULong? = withContext(Dispatchers.IO) { stream.available().toULong() }
    override suspend fun remaining(): ULong? = null
    override suspend fun size(): ULong? = null
    override suspend fun close() {
        super.close()
        stream.close()
    }

    override suspend fun position(): ULong = stream.count.toULong()
    override suspend fun seek(pos: Long, mode: Int): ULong? {
        when (mode) {
            InputFlow.FROM_BEGINNING -> {
                if (stream.markSupported()) {
                    stream.reset()
                    stream.mark(Int.MAX_VALUE)
                    return skip(pos)
                } else {
                    return null
                }
            }
            InputFlow.FROM_END -> return null
            InputFlow.FROM_POSITION -> {
                if (pos > 0) {
                    return skip(pos)
                } else {
                    val currentPosition = position()
                    return seek(currentPosition.toLong() + pos, InputFlow.FROM_BEGINNING)
                }
            }
            else -> return null
        }
    }

    init {
        if (stream.markSupported()) {
            stream.mark(Int.MAX_VALUE)
        }
    }
}