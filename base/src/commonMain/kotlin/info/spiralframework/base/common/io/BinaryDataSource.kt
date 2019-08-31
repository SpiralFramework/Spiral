package info.spiralframework.base.common.io

import info.spiralframework.base.binding.BinaryDataStream

@ExperimentalUnsignedTypes
class BinaryDataSource(val byteArray: ByteArray, val maxInstanceCount: Int = -1): DataSource<BinaryDataStream> {
    companion object {}
    override val dataSize: ULong = byteArray.size.toULong()
    private var openInstances: Int = 0 //NOTE: This isn't concurrently safe probably

    override fun newStream(): BinaryDataStream? {
        if (canOpenStream()) {
            val stream = BinaryDataStream(byteArray)
            stream.onClose = this::instanceClosed
            openInstances += 1
            return stream
        } else {
            return null
        }
    }
    override fun canOpenStream(): Boolean = maxInstanceCount == -1 || openInstances < maxInstanceCount

    private fun instanceClosed(ignored: DataStream) {
        openInstances -= 1
    }
}