package info.spiralframework.base.binding

import info.spiralframework.base.common.io.OutputFlow

@ExperimentalUnsignedTypes
expect open class BinaryOutputFlow() : OutputFlow {
    fun getData(): ByteArray
}