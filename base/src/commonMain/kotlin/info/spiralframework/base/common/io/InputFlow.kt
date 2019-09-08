@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
typealias InputFlowEventHandler = (InputFlow) -> Unit

@ExperimentalUnsignedTypes
interface InputFlow: DataCloseable {
    companion object {
        const val FROM_BEGINNING = 0
        const val FROM_END = 1
        const val FROM_POSITION = 2
    }

    var onClose: InputFlowEventHandler?

    fun read(): Int?
    fun read(b: ByteArray): Int?
    fun read(b: ByteArray, off: Int, len: Int): Int?
    fun skip(n: ULong): ULong?
    fun seek(pos: Long, mode: Int): ULong? = throw IllegalStateException("This flow is not seekable")
    fun position(): ULong? = null

    fun available(): ULong?
    fun remaining(): ULong?
    fun size(): ULong?
    override fun close() {
        onClose?.invoke(this)
    }
}

@ExperimentalUnsignedTypes
open class CountingInputFlow(val stream: InputFlow) : InputFlow by stream {
    var _count = 0L
    val count
        get() = _count

    open val streamOffset: Long
        get() = if (stream is CountingInputFlow) stream.streamOffset else count
//        set(value) {
//            if (stream is CountingInputFlow) stream.streamOffset = value else count = value
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
        return if (stream is CountingInputFlow)
            stream.seekForward(n)
        else
            stream.skip(n)
    }
}

@ExperimentalUnsignedTypes
open class WindowedInputFlow(val window: InputFlow, val offset: Long, val windowSize: Long) :
    CountingInputFlow(window) {
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
inline fun InputFlow.skip(number: Number): ULong? = skip(number.toLong().toULong())

fun readResultIsValid(byte: Int): Boolean = byte != -1