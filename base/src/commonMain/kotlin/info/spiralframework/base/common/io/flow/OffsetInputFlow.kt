package info.spiralframework.base.common.io.flow

@ExperimentalUnsignedTypes
open class OffsetInputFlow private constructor(val backing: InputFlow, val offset: ULong) : InputFlow {
    companion object {
        suspend operator fun invoke(backing: InputFlow, offset: ULong): OffsetInputFlow {
            val flow = OffsetInputFlow(backing, offset)
            flow.initialSkip()
            return flow
        }
    }

    override var onClose: InputFlowEventHandler? = null
    private var position: ULong = 0uL

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
    override suspend fun size(): ULong? = backing.size()?.minus(offset)
    override suspend fun position(): ULong = position

    override suspend fun seek(pos: Long, mode: Int): ULong? {
        when (mode) {
            InputFlow.FROM_BEGINNING -> {
                this.position = pos.toULong()
                backing.seek(offset.toLong() + pos, mode)
            }
            InputFlow.FROM_POSITION -> {
                val n = this.position.toLong() + pos
                this.position = n.toULong()
                backing.seek(offset.toLong() + n, InputFlow.FROM_BEGINNING)
            }
            InputFlow.FROM_END -> {
                val size = size()
                if (size == null) {
                    val result = backing.seek(pos, mode) ?: return null
                    if (result < offset) {
                        backing.skip(offset - result)
                        this.position = 0u
                    }
                } else {
                    val n = (size.toLong() - pos)
                    this.position = n.toULong()
                    backing.seek(offset.toLong() + n, InputFlow.FROM_BEGINNING)
                }
            }
            else -> return null
        }

        return position()
    }

    suspend fun initialSkip() {
        backing.skip(offset)
    }

    override suspend fun close() {
        super.close()
        backing.close()
    }
}