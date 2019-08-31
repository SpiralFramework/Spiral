package info.spiralframework.base.binding

import info.spiralframework.base.common.io.DataSink

@ExperimentalUnsignedTypes
expect open class BinaryDataSink() : DataSink {
    fun getData(): ByteArray
}