package info.spiralframework.base.jvm.io

import info.spiralframework.base.CountingInputStream
import info.spiralframework.base.common.io.InputFlow
import info.spiralframework.base.common.io.InputFlowEventHandler
import info.spiralframework.base.common.io.readResultIsValid
import info.spiralframework.base.common.io.skip
import java.io.InputStream

@ExperimentalUnsignedTypes
open class JVMInputFlow private constructor(val stream: CountingInputStream): InputFlow {
    constructor(stream: InputStream): this(CountingInputStream(stream))
    override var onClose: InputFlowEventHandler? = null

    override fun read(): Int? = stream.read().takeIf(::readResultIsValid)
    override fun read(b: ByteArray): Int? = stream.read(b).takeIf(::readResultIsValid)
    override fun read(b: ByteArray, off: Int, len: Int): Int? = stream.read(b, off, len).takeIf(::readResultIsValid)
    override fun skip(n: ULong): ULong? = stream.skip(n.toLong()).toULong()
    override fun available(): ULong? = stream.available().toULong()
    override fun remaining(): ULong? = null
    override fun size(): ULong? = null
    override fun close() {
        super.close()
        stream.close()
    }

    override fun position(): ULong = stream.count.toULong()
    override fun seek(pos: Long, mode: Int): ULong? {
        when (mode) {
            InputFlow.FROM_BEGINNING -> {
                if (stream.markSupported()) {
                    stream.reset()
                    stream.mark(Int.MAX_VALUE)
                    return skip(pos)
                } else {
                    return null
                }
            }
            InputFlow.FROM_END -> return null
            InputFlow.FROM_POSITION -> {
                if (pos > 0) {
                    return skip(pos)
                } else {
                    val currentPosition = position() ?: return null
                    return seek(currentPosition.toLong() + pos, InputFlow.FROM_BEGINNING)
                }
            }
            else -> return null
        }
    }

    init {
        if (stream.markSupported()) {
            stream.mark(Int.MAX_VALUE)
        }
    }
}