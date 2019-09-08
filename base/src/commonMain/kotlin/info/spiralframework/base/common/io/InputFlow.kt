@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
typealias InputFlowEventHandler = suspend (flow: InputFlow) -> Unit

@ExperimentalUnsignedTypes
interface InputFlow : DataCloseable {
    companion object {
        const val FROM_BEGINNING = 0
        const val FROM_END = 1
        const val FROM_POSITION = 2
    }

    var onClose: InputFlowEventHandler?

    suspend fun read(): Int?
    suspend fun read(b: ByteArray): Int?
    suspend fun read(b: ByteArray, off: Int, len: Int): Int?
    suspend fun skip(n: ULong): ULong?
    suspend fun seek(pos: Long, mode: Int): ULong? = throw IllegalStateException("This flow is not seekable")
    suspend fun position(): ULong? = null

    suspend fun available(): ULong?
    suspend fun remaining(): ULong?
    suspend fun size(): ULong?

    override suspend fun close() {
        onClose?.invoke(this)
    }
}

@ExperimentalUnsignedTypes
open class CountingInputFlow(val flow: InputFlow) : InputFlow by flow {
    var _count = 0L
    val count
        get() = _count

    open val flowOffset: Long
        get() = if (flow is CountingInputFlow) flow.flowOffset else count
//        set(value) {
//            if (stream is CountingInputFlow) stream.streamOffset = value else count = value
//        }

    override suspend fun read(): Int? {
        val byte = flow.read()
        if (byte != null)
            _count++
        return byte
    }

    override suspend fun read(b: ByteArray): Int? {
        val read = flow.read(b)
        if (read != null)
            _count += read.coerceAtLeast(0)
        return read
    }

    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? {
        val read = flow.read(b, off, len)
        if (read != null)
            _count += read.coerceAtLeast(0)
        return read
    }

    override suspend fun skip(n: ULong): ULong? {
        val amount = flow.skip(n)
        if (amount != null)
            _count += amount.toLong()
        return amount
    }

    override suspend fun position(): ULong = flowOffset.toULong()

    suspend fun seekForward(n: ULong): ULong? {
        return if (flow is CountingInputFlow)
            flow.seekForward(n)
        else {
            flow.skip(n)
        }
    }
}

@ExperimentalUnsignedTypes
open class WindowedInputFlow(val window: InputFlow, val offset: Long, val windowSize: Long) : CountingInputFlow(window) {
    private var hasSkipped: Boolean = false
    override val flowOffset: Long
        get() = offset + super.flowOffset

    override suspend fun read(): Int? {
        skipIfNeeded()
        return if (count < windowSize) window.read() else null
    }
    override suspend fun read(b: ByteArray): Int? = read(b, 0, b.size)
    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? {
        skipIfNeeded()

        if (count >= windowSize)
            return null
        return window.read(b, off, len.coerceAtMost((windowSize - count).toInt()))
    }

    override suspend fun available(): ULong {
        skipIfNeeded()
        return (windowSize - count).toULong()
    }
    override suspend fun remaining(): ULong {
        skipIfNeeded()
        return (windowSize - count).toULong()
    }

    private suspend fun skipIfNeeded() {
        if (hasSkipped) {
            hasSkipped = true
            seekForward(offset.toULong())
        }
    }
}

@ExperimentalUnsignedTypes
suspend inline fun InputFlow.skip(number: Number): ULong? = skip(number.toLong().toULong())

fun readResultIsValid(byte: Int): Boolean = byte != -1