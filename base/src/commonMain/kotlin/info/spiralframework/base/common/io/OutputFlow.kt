@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
typealias OutputFlowEventHandler = (OutputFlow) -> Unit

@ExperimentalUnsignedTypes
interface OutputFlow: DataCloseable {
    var onClose: OutputFlowEventHandler?

    suspend fun write(byte: Int)
    suspend fun write(b: ByteArray)
    suspend fun write(b: ByteArray, off: Int, len: Int)
    suspend fun flush()
    override suspend fun close() {
        onClose?.invoke(this)
    }
}

@ExperimentalUnsignedTypes
open class CountingOutputFlow(val sink: OutputFlow) : OutputFlow by sink {
    var _count = 0L
    val count
        get() = _count

    open val streamOffset: Long
        get() = if (sink is CountingOutputFlow) sink.streamOffset else count

    override suspend fun write(byte: Int) {
        sink.write(byte)
        _count++
    }

    override suspend fun write(b: ByteArray) {
        sink.write(b)
        _count += b.size
    }

    override suspend fun write(b: ByteArray, off: Int, len: Int) {
        require(len >= 0)
        sink.write(b, off, len)
        _count += len
    }
}

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeByte(byte: Number) = write(byte.toInt())