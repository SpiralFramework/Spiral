package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
interface DataSource<T: DataStream> {
    companion object {
    }

    val dataSize: ULong?

    fun newStream(): T?
    fun canOpenStream(): Boolean
}

@ExperimentalUnsignedTypes
fun DataSource<*>.copyToSink(sink: DataSink) {
    newStream()?.copyToSink(sink)
}