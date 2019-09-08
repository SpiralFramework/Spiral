package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.io.DataSink
import info.spiralframework.base.common.io.OutputFlow
import info.spiralframework.base.common.io.closeAll
import java.io.File

@ExperimentalUnsignedTypes
class FileDataSink(val backing: File): DataSink<FileOutputFlow> {
    private val openInstances: MutableList<FileOutputFlow> = ArrayList(1)
    private var closed: Boolean = false

    override fun openOutputFlow(): FileOutputFlow? {
        if (canOpenOutputFlow()) {
            val stream = FileOutputFlow(backing)
            stream.onClose = this::instanceClosed
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }

    override fun canOpenOutputFlow(): Boolean = !closed && (openInstances.size < 1)

    private suspend fun instanceClosed(flow: OutputFlow) {
        openInstances.remove(flow)
    }

    override suspend fun close() {
        closed = true
        openInstances.closeAll()
        openInstances.clear()
    }
}