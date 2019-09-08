package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
interface DataPool<I: InputFlow, O: OutputFlow>: DataSource<I>, DataSink<O>