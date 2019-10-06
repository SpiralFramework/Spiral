package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.base.common.io.flow.OutputFlowEventHandler

@ExperimentalUnsignedTypes
open class CommonBinaryOutputFlow(val buffer: MutableList<Byte>): OutputFlow {
    constructor(): this(ArrayList())

    override var onClose: OutputFlowEventHandler? = null
    override suspend fun write(byte: Int) {
        buffer.add(byte.toByte())
    }
    override suspend fun write(b: ByteArray) {
        b.forEach { buffer.add(it) }
    }
    override suspend fun write(b: ByteArray, off: Int, len: Int) {
        b.slice(off until off + len).forEach { buffer.add(it) }
    }
    override suspend fun flush() {}
    fun getData(): ByteArray = buffer.toByteArray()
    fun getDataSize(): ULong = buffer.size.toULong()
}