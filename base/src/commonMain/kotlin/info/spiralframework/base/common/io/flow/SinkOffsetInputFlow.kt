package info.spiralframework.base.common.io.flow

import info.spiralframework.base.common.io.DataCloseableEventHandler

@ExperimentalUnsignedTypes
interface OffsetInputFlow: InputFlow {
    val baseOffset: ULong
}

@ExperimentalUnsignedTypes
open class SinkOffsetInputFlow private constructor(val backing: InputFlow, override val baseOffset: ULong) : OffsetInputFlow {
    companion object {
        suspend operator fun invoke(backing: InputFlow, offset: ULong): SinkOffsetInputFlow {
            val flow = SinkOffsetInputFlow(backing, offset)
            flow.initialSkip()
            return flow
        }
    }

    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()
    private var position: ULong = 0uL
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override suspend fun read(): Int? {
        position++
        return backing.read()
    }

    override suspend fun read(b: ByteArray, off: Int, len: Int): Int? {
        if (len < 0 || off < 0 || b.size > len - off)
            throw IndexOutOfBoundsException()

        val read = backing.read(b, off, len) ?: return null
        position += read.toULong()
        return read
    }

    override suspend fun skip(n: ULong): ULong? {
        val skipped = backing.skip(n) ?: return null
        position += skipped
        return skipped
    }

    override suspend fun available(): ULong? = backing.available()
    override suspend fun remaining(): ULong? = backing.remaining()
    override suspend fun size(): ULong? = backing.size()?.minus(baseOffset)
    override suspend fun position(): ULong = position

    override suspend fun seek(pos: Long, mode: Int): ULong? {
        when (mode) {
            InputFlow.FROM_BEGINNING -> {
                this.position = pos.toULong()
                backing.seek(baseOffset.toLong() + pos, mode)
            }
            InputFlow.FROM_POSITION -> {
                val n = this.position.toLong() + pos
                this.position = n.toULong()
                backing.seek(baseOffset.toLong() + n, InputFlow.FROM_BEGINNING)
            }
            InputFlow.FROM_END -> {
                val size = size()
                if (size == null) {
                    val result = backing.seek(pos, mode) ?: return null
                    if (result < baseOffset) {
                        backing.skip(baseOffset - result)
                        this.position = 0u
                    }
                } else {
                    val n = (size.toLong() - pos)
                    this.position = n.toULong()
                    backing.seek(baseOffset.toLong() + n, InputFlow.FROM_BEGINNING)
                }
            }
            else -> return null
        }

        return position()
    }

    suspend fun initialSkip() {
        backing.skip(baseOffset)
    }

    override suspend fun close() {
        super.close()

        if (!closed) {
            backing.close()
            closed = true
        }
    }
}