package info.spiralframework.base.binding

import info.spiralframework.base.common.io.flow.CountingOutputFlow

@ExperimentalUnsignedTypes
expect open class BinaryOutputFlow() : CountingOutputFlow {
    fun getDataSize(): ULong
    fun getData(): ByteArray
}