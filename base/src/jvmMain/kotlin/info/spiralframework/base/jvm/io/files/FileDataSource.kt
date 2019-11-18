package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.io.*
import java.io.File
import kotlin.math.max

@ExperimentalUnsignedTypes
class FileDataSource(val backing: File, val maxInstanceCount: Int = -1): DataSource<FileInputFlow> {
    override val closeHandlers: MutableList<DataCloseableEventHandler> = ArrayList()
    override val dataSize: ULong
        get() = backing.length().toULong()

    private val openInstances: MutableList<FileInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false
    override val isClosed: Boolean
        get() = closed

    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isStatic = true, isRandomAccess = true)

    override suspend fun openInputFlow(): FileInputFlow? {
        if (canOpenInputFlow()) {
            val stream = FileInputFlow(backing)
            stream.addCloseHandler(this::instanceClosed)
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }

    override suspend fun canOpenInputFlow(): Boolean = !closed && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

    private suspend fun instanceClosed(closeable: DataCloseable) {
        if (closeable is FileInputFlow) {
            openInstances.remove(closeable)
        }
    }

    override suspend fun close() {
        super.close()

        if (!closed) {
            closed = true
            openInstances.toTypedArray().closeAll()
            openInstances.clear()
        }
    }
}