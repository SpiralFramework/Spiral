package info.spiralframework.base.binding

import info.spiralframework.base.common.io.DataSink
import info.spiralframework.base.jvm.io.JVMDataSink
import java.io.ByteArrayOutputStream

@ExperimentalUnsignedTypes
actual open class BinaryDataSink(private val _internal: ByteArrayOutputStream) : DataSink by JVMDataSink(_internal) {
    actual constructor(): this(ByteArrayOutputStream())
    actual fun getData(): ByteArray = _internal.toByteArray()
}