@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
typealias DataSinkEventHandler = (DataSink) -> Unit

@ExperimentalUnsignedTypes
interface DataSink {
    var onClose: DataSinkEventHandler?

    fun write(byte: Int)
    fun write(b: ByteArray)
    fun write(b: ByteArray, off: Int, len: Int)
    fun flush()
    fun close() {
        onClose?.invoke(this)
    }
}

@ExperimentalUnsignedTypes
open class CountingDataSink(val sink: DataSink) : DataSink by sink {
    var _count = 0L
    val count
        get() = _count

    open val streamOffset: Long
        get() = if (sink is CountingDataSink) sink.streamOffset else count

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
public inline fun <T : DataSink?, R> T.use(block: (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

@ExperimentalUnsignedTypes
@PublishedApi
internal fun DataSink?.closeFinally(cause: Throwable?) = when {
    this == null -> {
    }
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            //cause.addSuppressed(closeException)
        }
}

@ExperimentalUnsignedTypes
fun DataSink.writeByte(byte: Number) = write(byte.toInt())