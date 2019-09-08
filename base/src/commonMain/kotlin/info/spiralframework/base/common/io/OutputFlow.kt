@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
typealias OutputFlowEventHandler = (OutputFlow) -> Unit

@ExperimentalUnsignedTypes
interface OutputFlow: DataCloseable {
    var onClose: OutputFlowEventHandler?

    fun write(byte: Int)
    fun write(b: ByteArray)
    fun write(b: ByteArray, off: Int, len: Int)
    fun flush()
    override fun close() {
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

    override fun write(byte: Int) {
        sink.write(byte)
        _count++
    }

    override fun write(b: ByteArray) {
        sink.write(b)
        _count += b.size
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        require(len >= 0)
        sink.write(b, off, len)
        _count += len
    }
}

@ExperimentalUnsignedTypes
fun OutputFlow.writeByte(byte: Number) = write(byte.toInt())