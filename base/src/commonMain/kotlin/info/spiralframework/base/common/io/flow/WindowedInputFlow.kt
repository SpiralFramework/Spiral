package info.spiralframework.base.common.io.flow

import info.spiralframework.base.common.io.DataCloseableEventHandler

@ExperimentalUnsignedTypes
open class WindowedInputFlow private constructor(val window: InputFlow, override val baseOffset: ULong, val windowSize: ULong) : OffsetInputFlow {
    companion object {
        suspend operator fun invoke(window: InputFlow, offset: ULong, windowSize: ULong): WindowedInputFlow {
            val flow = WindowedInputFlow(window, offset, windowSize)
            flow.initialSkip()
            return flow
        }
    }

    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()
    private var position: ULong = 0uL
    private var closed: Boolean = true
    override val isClosed: Boolean
        get() = closed

    override suspend fun read(): Int? = if (position < windowSize) {
        position++
        window.read()
    } else {
        null
    }
    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? {
        if (len < 0 || off < 0 || len > b.size - off)
            throw IndexOutOfBoundsException()

        val avail = minOf((windowSize - position).toInt(), len)

        if (avail <= 0)
            return null

        window.read(b, off, avail)
        position += avail.toULong()
        return avail
    }

    override suspend fun skip(n: ULong): ULong? {
        val avail = minOf(windowSize - position, n)
        if (avail <= 0u)
            return null

        window.skip(avail)
        position += avail
        return avail
    }

    override suspend fun available(): ULong? {
        val avail = minOf(windowSize - position, window.available() ?: return null)
        if (avail <= 0u)
            return null

        return avail
    }

    override suspend fun remaining(): ULong = windowSize - position
    override suspend fun size(): ULong = windowSize
    override suspend fun position(): ULong = position

    override suspend fun seek(pos: Long, mode: Int): ULong? {
        when (mode) {
            InputFlow.FROM_BEGINNING -> {
                val n = pos.coerceIn(0 until windowSize.toLong())
                this.position = n.toULong()
                window.seek(baseOffset.toLong() + n, mode)
            }
            InputFlow.FROM_POSITION -> {
                val n = (this.position.toLong() + pos).coerceIn(0 until windowSize.toLong())
                this.position = n.toULong()
                window.seek(baseOffset.toLong() + n, InputFlow.FROM_BEGINNING)
            }
            InputFlow.FROM_END -> {
                val n = (this.windowSize.toLong() - pos).coerceIn(0 until windowSize.toLong())
                this.position = n.toULong()
                window.seek(baseOffset.toLong() + n, InputFlow.FROM_BEGINNING)
            }
            else -> return null
        }

        return position()
    }

    suspend fun initialSkip() {
        window.skip(baseOffset)
    }

    override suspend fun close() {
        super.close()

        if (!closed) {
            window.close()
            closed = true
        }
    }
}