package info.spiralframework.base.common.io.flow

@ExperimentalUnsignedTypes
open class WindowedInputFlow private constructor(val window: InputFlow, val offset: ULong, val windowSize: ULong) : InputFlow {
    companion object {
        suspend operator fun invoke(window: InputFlow, offset: ULong, windowSize: ULong): WindowedInputFlow {
            val flow = WindowedInputFlow(window, offset, windowSize)
            flow.initialSkip()
            return flow
        }
    }

    override var onClose: InputFlowEventHandler? = null
    private var position: ULong = 0uL

    override suspend fun read(): Int? = if (position < windowSize) {
        position++
        window.read()
    } else {
        null
    }
    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? {
        if (len < 0 || off < 0 || b.size > len - off)
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
                window.seek(n, mode)
            }
            InputFlow.FROM_POSITION -> {
                val n = (this.position.toLong() + pos).coerceIn(0 until windowSize.toLong())
                this.position = n.toULong()
                window.seek(n, mode)
            }
            InputFlow.FROM_END -> {
                val n = (this.windowSize.toLong() - pos).coerceIn(0 until windowSize.toLong())
                this.position = n.toULong()
                window.seek(n, mode)
            }
            else -> return null
        }

        return position()
    }

    suspend fun initialSkip() {
        window.skip(offset)
    }

    override suspend fun close() {
        super.close()
        window.close()
    }
}