package info.spiralframework.base.jvm.io

import info.spiralframework.base.common.io.DataStream
import info.spiralframework.base.common.io.DataStreamEventHandler
import info.spiralframework.base.common.io.readResultIsValid
import java.io.InputStream

@ExperimentalUnsignedTypes
open class JVMDataStream(val stream: InputStream): DataStream {
    override var onClose: DataStreamEventHandler? = null

    override fun read(): Int? = stream.read().takeIf(::readResultIsValid)
    override fun read(b: ByteArray): Int? = stream.read(b).takeIf(::readResultIsValid)
    override fun read(b: ByteArray, off: Int, len: Int): Int? = stream.read(b, off, len).takeIf(::readResultIsValid)
    override fun skip(n: ULong): ULong? = stream.skip(n.toLong()).toULong()
    override fun remaining(): ULong? = stream.available().toULong()
    override fun close() {
        super.close()
        stream.close()
    }
}