package info.spiralframework.base.common.io

/**
* An interface that loosely defines a destination for data.
 */
@ExperimentalUnsignedTypes
interface DataSink<O: OutputFlow>: DataCloseable {
    fun openOutputFlow(): O?
    fun canOpenOutputFlow(): Boolean
}