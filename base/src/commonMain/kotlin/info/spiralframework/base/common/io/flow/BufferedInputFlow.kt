package info.spiralframework.base.common.io.flow

import info.spiralframework.base.common.io.DataCloseableEventHandler

@ExperimentalUnsignedTypes
open class BufferedInputFlow(val backing: InputFlow) : PeekableInputFlow {
    companion object {
        const val DEFAULT_BUFFER_SIZE = 8192
        const val MAX_BUFFER_SIZE = Int.MAX_VALUE - 8
    }

    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()
    protected var buffer: ByteArray = ByteArray(DEFAULT_BUFFER_SIZE)
    protected var count: Int = 0
    protected var pos: Int = 0
    protected var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    private suspend fun fill() {
        pos = 0
        fillPartial()
    }

    private suspend fun fillPartial() {
        count = 0
        val n = backing.read(buffer, pos, buffer.size - pos)
        if (n ?: 0 > 0)
            count = n!! + pos
    }

    override suspend fun peek(forward: Int): Int? {
        if (pos >= count) {
            fill()
            if (pos >= count) {
                return null
            }
        } else if (pos + forward > count) {
            if ((pos + forward + 1) - count < buffer.size) { /* Shuffle down */
                buffer.copyOfRange(pos, count).copyInto(buffer, 0, 0, count - pos)
                pos = count - pos
                fillPartial()
                pos = 0
            } else if (buffer.size >= MAX_BUFFER_SIZE) {
                throw IllegalStateException("OOM; Required array size too large")
            } else { /* Grow */
                /* grow buffer */
                val nbuf = ByteArray(if (pos <= MAX_BUFFER_SIZE - pos) pos * 2 else MAX_BUFFER_SIZE)
                buffer.copyInto(nbuf, 0, 0, pos)
                buffer = nbuf
                fillPartial()
            }
        }

        return buffer[pos + forward - 1].toInt() and 0xFF
    }

    override suspend fun read(): Int? {
        if (pos >= count) {
            fill()
            if (pos >= count) {
                return null
            }
        }

        return buffer[pos++].toInt() and 0xFF
    }

    private suspend fun read1(b: ByteArray, off: Int, len: Int): Int? {
        var avail = count - pos
        if (avail <= 0) {
            if (len >= buffer.size) {
                return backing.read(b, off, len)
            }

            fill()

            avail = count - pos
            if (avail <= 0) return -1
        }

        val cnt = if (avail < len) avail else len
        buffer.copyInto(b, off, pos, pos + cnt)
        pos += cnt
        return cnt
    }

    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? {
        if ((off or len or (off + len) or (b.size - (off + len))) < 0) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        }

        var n = 0

        while (true) {
            val nread = read1(b, off + n, len - n) ?: 0
            if (nread <= 0)
                return if (n == 0) nread else n
            n += nread
            if (n >= len)
                return n
            if (backing.available() ?: 0u <= 0u)
                return n
        }
    }

    override suspend fun skip(n: ULong): ULong? {
        val avail = count - pos
        if (avail <= 0) {
            return backing.skip(n)
        }

        if (avail < n.toInt()) {
            pos += avail

            return avail.toULong() + (skip(n - avail.toULong()) ?: 0uL)
        } else {
            pos += n.toInt()

            return n
        }
    }

    override suspend fun available(): ULong? = (count - pos).toULong() + (backing.available() ?: 0uL)
    override suspend fun position(): ULong = backing.position() + (pos - count).toULong()
    override suspend fun remaining(): ULong? = available()
    override suspend fun size(): ULong? = backing.size()

    override suspend fun close() {
        super.close()

        if (!closed) {
            closed = true
            backing.close()
        }
    }
}