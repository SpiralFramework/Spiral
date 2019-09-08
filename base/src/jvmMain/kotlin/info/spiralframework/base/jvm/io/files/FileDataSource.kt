package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.DataSourceReproducibility
import info.spiralframework.base.common.io.InputFlow
import info.spiralframework.base.common.io.closeAll
import java.io.File
import kotlin.math.max

@ExperimentalUnsignedTypes
class FileDataSource(val backing: File, val maxInstanceCount: Int = -1): DataSource<FileInputFlow> {
    override val dataSize: ULong = backing.length().toULong()
    private val openInstances: MutableList<FileInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false

    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isStatic = true, isRandomAccess = true)

    override fun openInputFlow(): FileInputFlow? {
        if (canOpenInputFlow()) {
            val stream = FileInputFlow(backing)
            stream.onClose = this::instanceClosed
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }

    override fun canOpenInputFlow(): Boolean = !closed && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

    private suspend fun instanceClosed(flow: InputFlow) {
        openInstances.remove(flow)
    }

    override suspend fun close() {
        closed = true
        openInstances.closeAll()
        openInstances.clear()
    }
}