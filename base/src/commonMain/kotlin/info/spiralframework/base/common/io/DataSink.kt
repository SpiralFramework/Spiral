package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.OutputFlow

/**
* An interface that loosely defines a destination for data.
 */
@ExperimentalUnsignedTypes
interface DataSink<O: OutputFlow>: DataCloseable {
    suspend fun openOutputFlow(): O?
    suspend fun canOpenOutputFlow(): Boolean
}