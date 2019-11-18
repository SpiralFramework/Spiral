package info.spiralframework.base.common

import info.spiralframework.base.common.io.DataCloseableEventHandler
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.readResultIsValid
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.discardExact

@ExperimentalUnsignedTypes
class ByteReadChannelInputFlow(val channel: ByteReadChannel) : InputFlow {
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()

    private var position: ULong = 0uL
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override suspend fun read(): Int? {
        val result = channel.readByte().toInt().and(0xFF)
        position++
        return result
    }
    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? {
        val result = channel.readAvailable(b, off, len).takeIf(::readResultIsValid) ?: return null
        position += result.toUInt()
        return result
    }

    override suspend fun skip(n: ULong): ULong {
        channel.discardExact(n.toLong())
        position += n
        return n
    }

    override suspend fun available(): ULong = channel.availableForRead.toULong()
    override suspend fun remaining(): ULong? = null
    override suspend fun size(): ULong? = null
    override suspend fun position(): ULong = position

    override suspend fun close() {
        super.close()

        closed = true
    }
}