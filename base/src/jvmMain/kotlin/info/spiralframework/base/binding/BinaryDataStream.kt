package info.spiralframework.base.binding

import info.spiralframework.base.common.io.DataStream
import info.spiralframework.base.jvm.io.JVMDataStream
import java.io.ByteArrayInputStream

@ExperimentalUnsignedTypes
actual class BinaryDataStream actual constructor(array: ByteArray) : DataStream by JVMDataStream(ByteArrayInputStream(array))