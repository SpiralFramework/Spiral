package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.CountingOutputFlow

@ExperimentalUnsignedTypes
open class CommonBinaryOutputFlow(val buffer: MutableList<Byte>): CountingOutputFlow {
    constructor(): this(ArrayList())

    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()
    override val streamOffset: Long
        get() = buffer.size.toLong()

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

    override suspend fun close() {
        super.close()

        closed = true
    }
}