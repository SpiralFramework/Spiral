@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
typealias DataStreamEventHandler = (DataStream) -> Unit

@ExperimentalUnsignedTypes
interface DataStream {
    var onClose: DataStreamEventHandler?

    fun read(): Int?
    fun read(b: ByteArray): Int?
    fun read(b: ByteArray, off: Int, len: Int): Int?
    fun skip(n: ULong): ULong?

    fun remaining(): ULong?
    fun close() {
        onClose?.invoke(this)
    }
}

@ExperimentalUnsignedTypes
open class CountingDataStream(val stream: DataStream) : DataStream by stream {
    var _count = 0L
    val count
        get() = _count

    open val streamOffset: Long
        get() = if (stream is CountingDataStream) stream.streamOffset else count
//        set(value) {
//            if (stream is CountingDataStream) stream.streamOffset = value else count = value
//        }

    override fun read(): Int? {
        val byte = stream.read()
        if (byte != null)
            _count++
        return byte
    }

    override fun read(b: ByteArray): Int? {
        val read = stream.read(b)
        if (read != null)
            _count += read.coerceAtLeast(0)
        return read
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int? {
        val read = stream.read(b, off, len)
        if (read != null)
            _count += read.coerceAtLeast(0)
        return read
    }

    override fun skip(n: ULong): ULong? {
        val amount = stream.skip(n)
        if (amount != null)
            _count += amount.toLong()
        return amount
    }

    fun seekForward(n: ULong): ULong? {
        return if (stream is CountingDataStream)
            stream.seekForward(n)
        else
            stream.skip(n)
    }
}

@ExperimentalUnsignedTypes
open class WindowedDataStream(val window: DataStream, val offset: Long, val windowSize: Long) :
    CountingDataStream(window) {
    override val streamOffset: Long
        get() = offset + count

    override fun read(): Int? = if (count < windowSize) window.read() else null
    override fun read(b: ByteArray): Int? = read(b, 0, b.size)
    override fun read(b: ByteArray, off: Int, len: Int): Int? {
        if (count >= windowSize)
            return null
        return window.read(b, off, len.coerceAtMost((windowSize - count).toInt()))
    }

    override fun remaining(): ULong = (windowSize - count).toULong()

    init {
        seekForward(offset.toULong())
    }
}

@ExperimentalUnsignedTypes
public inline fun <T : DataStream?, R> T.use(block: (T) -> R): R {
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
internal fun DataStream?.closeFinally(cause: Throwable?) = when {
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
inline fun DataStream.skip(number: Number): ULong? = skip(number.toLong().toULong())

fun readResultIsValid(byte: Int): Boolean = byte != -1