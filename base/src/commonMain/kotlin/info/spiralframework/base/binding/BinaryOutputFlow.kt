package info.spiralframework.base.binding

import info.spiralframework.base.common.io.OutputFlow

@ExperimentalUnsignedTypes
expect open class BinaryOutputFlow() : OutputFlow {
    fun getDataSize(): ULong
    fun getData(): ByteArray
}