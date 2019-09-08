package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
open class CommonBinaryOutputFlow(val buffer: MutableList<Byte> = ArrayList()): OutputFlow {
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
}