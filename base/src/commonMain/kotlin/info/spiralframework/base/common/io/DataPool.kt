package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow

@ExperimentalUnsignedTypes
interface DataPool<I: InputFlow, O: OutputFlow>: DataSource<I>, DataSink<O>