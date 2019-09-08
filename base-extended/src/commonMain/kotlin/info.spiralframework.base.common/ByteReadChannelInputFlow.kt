package info.spiralframework.base.common

import info.spiralframework.base.common.io.InputFlow
import info.spiralframework.base.common.io.InputFlowEventHandler
import info.spiralframework.base.common.io.readResultIsValid
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.discardExact

@ExperimentalUnsignedTypes
class ByteReadChannelInputFlow(val channel: ByteReadChannel) : InputFlow {
    override var onClose: InputFlowEventHandler? = null

    override suspend fun read(): Int? = channel.readByte().toInt().and(0xFF)
    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? = channel.readAvailable(b, off, len).takeIf(::readResultIsValid)

    override suspend fun skip(n: ULong): ULong {
        channel.discardExact(n.toLong())
        return n
    }

    override suspend fun available(): ULong = channel.availableForRead.toULong()
    override suspend fun remaining(): ULong? = null
    override suspend fun size(): ULong? = null
}