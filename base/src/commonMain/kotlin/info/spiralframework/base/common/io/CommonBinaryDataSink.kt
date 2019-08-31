package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
open class CommonBinaryDataSink(val buffer: MutableList<Byte> = ArrayList()): DataSink {
    override var onClose: DataSinkEventHandler? = null
    override fun write(byte: Int) {
        buffer.add(byte.toByte())
    }
    override fun write(b: ByteArray) {
        b.forEach { buffer.add(it) }
    }
    override fun write(b: ByteArray, off: Int, len: Int) {
        b.slice(off until off + len).forEach { buffer.add(it) }
    }
    override fun flush() {}
    fun getData(): ByteArray = buffer.toByteArray()
}